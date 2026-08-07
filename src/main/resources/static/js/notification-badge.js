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

    fetch('/api/v1/members/me', { headers: { Authorization: `${tokenType} ${token}` } })
        .then((response) => (response.ok ? response.json() : null))
        .then((me) => {
            if (!me) {
                return;
            }

            const client = new StompJs.Client({
                brokerURL: `${protocol}://${location.host}/ws`,
                connectHeaders: { Authorization: `${tokenType} ${token}` },
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
