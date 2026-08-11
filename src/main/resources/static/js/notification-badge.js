/**
 * 로그인 회원의 개인 알림 채널(/topic/members/{memberId}/notifications)을 구독한다.
 * 사용하는 페이지는 이 스크립트보다 먼저 @stomp/stompjs CDN 스크립트를 로드해야 한다 (realtime-tracking.html 참고).
 */
function subscribeToNotifications(onNotification) {
    const token = localStorage.getItem('accessToken');
    const tokenType = localStorage.getItem('tokenType') || 'Bearer';
    if (!token) {
        return;
    }

    const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
    const authHeader = `${tokenType} ${token}`;

    fetchCurrentMember(authHeader).then((me) => {
        if (!me) {
            return;
        }

        const client = new StompJs.Client({
            brokerURL: `${protocol}://${location.host}/ws`,
            connectHeaders: { Authorization: authHeader },
            reconnectDelay: 4000
        });

        client.onConnect = () => {
            client.subscribe(`/topic/members/${me.id}/notifications`, (message) => {
                onNotification(JSON.parse(message.body));
            });
        };

        client.activate();
    });
}

/**
 * 로그인 회원 정보를 조회한다. 네트워크 오류·5xx 응답은 지수 백오프로 재시도하되(reconnectDelay는 소켓이 붙은 뒤에만
 * 동작해 이 최초 조회 실패는 못 복구하므로), 401/403(인증 실패)은 재시도해도 의미가 없어 즉시 포기한다.
 */
async function fetchCurrentMember(authHeader, attempt = 0) {
    const MAX_RETRIES = 3;
    try {
        const response = await fetch('/api/v1/members/me', { headers: { Authorization: authHeader } });
        if (response.status === 401 || response.status === 403) {
            return null;
        }
        if (!response.ok) {
            throw new Error(`알림 채널 구독을 위한 회원 조회 실패: status=${response.status}`);
        }
        return await response.json();
    } catch (error) {
        if (attempt >= MAX_RETRIES) {
            return null;
        }
        await new Promise((resolve) => setTimeout(resolve, 1000 * 2 ** attempt));
        return fetchCurrentMember(authHeader, attempt + 1);
    }
}
