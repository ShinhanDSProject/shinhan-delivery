function greetingByHour() {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 18) return 'Good afternoon';
    return 'Good evening';
}

function shortLocation(address) {
    const tokens = address.split(' ');
    const district = tokens.find((token) => token.endsWith('구') || token.endsWith('군'));
    return district || tokens[1] || address;
}

function formatRoute(deliveryRequest) {
    return `${shortLocation(deliveryRequest.pickupAddress)} → ${shortLocation(deliveryRequest.dropoffAddress)}`;
}

// TODO: 백엔드에 실제 예상도착/매칭시간 필드가 추가되면 이 임시 추정치를 대체한다.
function placeholderEtaLabel() {
    const eta = new Date(Date.now() + 90 * 60 * 1000);
    const hours24 = eta.getHours();
    const period = hours24 < 12 ? '오전' : '오후';
    const hours12 = ((hours24 + 11) % 12) + 1;
    const minutes = String(eta.getMinutes()).padStart(2, '0');
    return `예상 도착: ${period} ${hours12}:${minutes}`;
}

function placeholderMatchingWaitLabel() {
    return '예상 매칭 시간: 약 1시간';
}

function renderActiveDeliveries(deliveries) {
    const matched = deliveries.filter((d) => d.status === 'MATCHED');
    const requested = deliveries.filter((d) => d.status === 'REQUESTED');

    const activeSection = document.getElementById('activeDeliverySection');
    const activeList = document.getElementById('activeDeliveryList');
    const emptySection = document.getElementById('emptyDeliverySection');
    const matchingSection = document.getElementById('matchingSection');
    const matchingList = document.getElementById('matchingList');

    activeList.innerHTML = '';
    matchingList.innerHTML = '';

    if (matched.length === 0 && requested.length === 0) {
        emptySection.hidden = false;
        activeSection.hidden = true;
        matchingSection.hidden = true;
        return;
    }

    emptySection.hidden = true;

    if (matched.length > 0) {
        activeSection.hidden = false;
        matched.forEach((delivery) => {
            const card = document.createElement('div');
            card.className = 'delivery-card';
            card.innerHTML = `
                <span class="status-badge">배송 중</span>
                <div class="route">${formatRoute(delivery)}</div>
                <div class="eta">${placeholderEtaLabel()}</div>
                <div class="actions">
                    <button type="button" data-action="track">실시간 위치 확인</button>
                    <button type="button" data-action="detail">배송 상세내역</button>
                </div>
            `;
            card.querySelector('[data-action="track"]').addEventListener('click', () => {
                location.href = `/realtime-tracking?id=${delivery.id}`;
            });
            card.querySelector('[data-action="detail"]').addEventListener('click', () => {
                location.href = `/delivery-detail?id=${delivery.id}`;
            });
            activeList.appendChild(card);
        });
    } else {
        activeSection.hidden = true;
    }

    if (requested.length > 0) {
        matchingSection.hidden = false;
        requested.forEach((delivery) => {
            const card = document.createElement('div');
            card.className = 'waiting-card';
            card.innerHTML = `
                <div class="title">배달원을 찾고 있어요...</div>
                <div class="route">${placeholderMatchingWaitLabel()}</div>
                <div class="loading-dots"><span></span><span></span><span></span></div>
            `;
            matchingList.appendChild(card);
        });
    } else {
        matchingSection.hidden = true;
    }
}

async function loadHome() {
    const header = typeof authHeader === 'function' ? authHeader() : null;
    if (!header) {
        location.replace('/login');
        return;
    }

    document.getElementById('greetingTime').textContent = greetingByHour();

    try {
        const meResponse = await fetch('/api/v1/members/me', {
            headers: { Authorization: header }
        });
        if (meResponse.status === 401 || meResponse.status === 403) {
            location.replace('/login');
            return;
        }
        const me = await meResponse.json();
        if (!me.hasPaymentPin) {
            location.replace('/payment-pin-settings?required=1&returnUrl=%2Fhome');
            return;
        }
        document.getElementById('greetingName').textContent = `${me.name} 님`;

        const activeStatuses = ['REQUESTED', 'MATCHED'];
        const activeResponses = await Promise.all(
            activeStatuses.map((status) =>
                fetch(`/api/v1/delivery-requests?status=${status}&size=100`, {
                    headers: { Authorization: header }
                }).then((res) => res.json())
            )
        );
        const myActiveDeliveries = activeResponses.flatMap((page) => page.content || []);
        renderActiveDeliveries(myActiveDeliveries);

        const notificationsResponse = await fetch('/api/v1/notifications?size=20', {
            headers: { Authorization: header }
        });
        if (notificationsResponse.ok) {
            const page = await notificationsResponse.json();
            const hasUnread = (page.content || []).some((n) => !n.read);
            document.getElementById('unreadBadge').style.display = hasUnread ? 'block' : 'none';
        }
    } catch (error) {
        document.getElementById('emptyDeliverySection').hidden = false;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    loadHome();
    if (typeof subscribeToNotifications === 'function') {
        subscribeToNotifications(() => {
            const unreadBadge = document.getElementById('unreadBadge');
            if (unreadBadge) unreadBadge.style.display = 'block';
        });
    }
});
