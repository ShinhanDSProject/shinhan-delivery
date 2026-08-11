/**
 * 로그인한 배송원의 차량 오퍼 채널(/topic/vehicles/{vehicleId}/offers)을 구독한다.
 * 사용하는 페이지는 이 스크립트보다 먼저 @stomp/stompjs CDN 스크립트를 로드해야 한다 (realtime-tracking.html 참고).
 */
function subscribeToOffers(onOffer) {
    const token = localStorage.getItem('accessToken');
    const tokenType = localStorage.getItem('tokenType') || 'Bearer';
    if (!token) {
        return;
    }

    const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
    const authHeader = `${tokenType} ${token}`;

    fetchMyVehicleId(authHeader).then((vehicleId) => {
        if (!vehicleId) {
            return;
        }

        const client = new StompJs.Client({
            brokerURL: `${protocol}://${location.host}/ws`,
            connectHeaders: { Authorization: authHeader },
            reconnectDelay: 4000
        });

        client.onConnect = () => {
            client.subscribe(`/topic/vehicles/${vehicleId}/offers`, (message) => {
                onOffer(JSON.parse(message.body));
            });
        };

        client.activate();
    });
}

/**
 * 로그인 회원 본인 소유 차량의 id를 조회한다. 배송원 전용 "내 차량" API가 없어 회원 정보로 memberId를
 * 확인한 뒤 전체 차량 목록에서 찾는다. 네트워크 오류·5xx 응답은 지수 백오프로 재시도하되, 401/403(인증 실패)은
 * 재시도해도 의미가 없어 즉시 포기한다.
 */
async function fetchMyVehicleId(authHeader, attempt = 0) {
    const MAX_RETRIES = 3;
    try {
        const meResponse = await fetch('/api/v1/members/me', { headers: { Authorization: authHeader } });
        if (meResponse.status === 401 || meResponse.status === 403) {
            return null;
        }
        if (!meResponse.ok) {
            throw new Error(`오퍼 채널 구독을 위한 회원 조회 실패: status=${meResponse.status}`);
        }
        const me = await meResponse.json();

        const vehiclesResponse = await fetch('/api/v1/vehicles', { headers: { Authorization: authHeader } });
        if (!vehiclesResponse.ok) {
            throw new Error(`오퍼 채널 구독을 위한 차량 조회 실패: status=${vehiclesResponse.status}`);
        }
        const vehicles = await vehiclesResponse.json();
        const myVehicle = Array.isArray(vehicles) ? vehicles.find((vehicle) => vehicle.memberId === me.id) : null;
        return myVehicle ? myVehicle.id : null;
    } catch (error) {
        if (attempt >= MAX_RETRIES) {
            return null;
        }
        await new Promise((resolve) => setTimeout(resolve, 1000 * 2 ** attempt));
        return fetchMyVehicleId(authHeader, attempt + 1);
    }
}
