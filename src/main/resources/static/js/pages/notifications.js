const CATEGORY_META = {
    DELIVERY: { label: '배송', color: 'var(--cat-delivery)' },
    MATCHING: { label: '매칭', color: 'var(--cat-matching)' },
    POINT: { label: '포인트', color: 'var(--cat-point)' },
    NOTICE: { label: '공지', color: 'var(--cat-notice)' }
};

let currentTab = 'ALL';
let loadInFlight = false;
let pendingLiveNotifications = [];
let loadRequestId = 0;
const noticeDetailCache = new Map();

function authHeader() {
    const token = localStorage.getItem('accessToken');
    const tokenType = localStorage.getItem('tokenType') || 'Bearer';
    if (!token) {
        return null;
    }
    return `${tokenType} ${token}`;
}

function formatRelativeTime(isoString) {
    const diffMs = Date.now() - new Date(isoString).getTime();
    const minutes = Math.floor(diffMs / 60000);
    if (minutes < 1) return '방금 전';
    if (minutes < 60) return `${minutes}분 전`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours}시간 전`;
    const days = Math.floor(hours / 24);
    return `${days}일 전`;
}

async function fetchNotifications(category) {
    const header = authHeader();
    if (!header) {
        return [];
    }
    const query = category ? `?category=${category}&size=30` : '?size=30';
    const response = await fetch(`/api/v1/notifications${query}`, {
        headers: { Authorization: header }
    });
    if (response.status === 401 || response.status === 403) {
        location.replace('/login');
        return [];
    }
    if (!response.ok) {
        return [];
    }
    const page = await response.json();
    return (page.content || []).map((n) => ({
        source: 'notification',
        id: n.id,
        category: n.category,
        title: n.title,
        body: n.message,
        read: n.read,
        createdAt: n.createdAt
    }));
}

async function fetchNotices() {
    const response = await fetch('/api/v1/notices?size=30');
    if (!response.ok) {
        return [];
    }
    const page = await response.json();
    return (page.content || []).map((notice) => ({
        source: 'notice',
        id: notice.id,
        category: 'NOTICE',
        title: notice.title,
        body: null,
        read: true,
        createdAt: notice.createdAt
    }));
}

function renderCard(item) {
    const card = document.createElement('article');
    card.className = `notification-card ${item.read ? 'read' : ''}`;
    if (item.source === 'notification') {
        card.dataset.notificationId = item.id;
    }
    const meta = CATEGORY_META[item.category] || { label: item.category, color: 'var(--text-muted)' };
    card.innerHTML = `
        <div class="meta">
            <span class="category" style="color:${meta.color}">
                ${item.read ? '' : '<span class="dot"></span>'}${meta.label}
            </span>
            <span class="time">${formatRelativeTime(item.createdAt)}</span>
        </div>
        <div class="title">${item.title}</div>
        <div class="body">${item.body || ''}</div>
    `;

    if (item.source === 'notification') {
        card.addEventListener('click', () => markNotificationRead(item.id, card));
    } else {
        card.addEventListener('click', () => toggleNoticeDetail(item.id, card));
    }
    return card;
}

async function markNotificationRead(id, card) {
    if (card.classList.contains('read')) {
        return;
    }
    const header = authHeader();
    const response = await fetch(`/api/v1/notifications/${id}/read`, {
        method: 'PATCH',
        headers: { Authorization: header }
    });
    if (response.ok) {
        card.classList.add('read');
        const dot = card.querySelector('.dot');
        if (dot) dot.remove();
    }
}

async function toggleNoticeDetail(id, card) {
    const bodyEl = card.querySelector('.body');
    if (bodyEl.textContent.trim()) {
        bodyEl.textContent = '';
        return;
    }
    if (noticeDetailCache.has(id)) {
        bodyEl.textContent = noticeDetailCache.get(id);
        return;
    }
    const response = await fetch(`/api/v1/notices/${id}`);
    if (!response.ok) {
        return;
    }
    const detail = await response.json();
    noticeDetailCache.set(id, detail.content);
    bodyEl.textContent = detail.content;
}

async function loadTab(tab) {
    const requestId = ++loadRequestId;
    loadInFlight = true;
    const list = document.getElementById('list');
    list.innerHTML = '<div class="loading-state">불러오는 중...</div>';

    let items = [];
    if (tab === 'ALL') {
        const [notifications, notices] = await Promise.all([fetchNotifications(null), fetchNotices()]);
        items = [...notifications, ...notices];
    } else if (tab === 'NOTICE') {
        items = await fetchNotices();
    } else {
        items = await fetchNotifications(tab);
    }

    if (requestId !== loadRequestId) {
        return;
    }

    items.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

    list.innerHTML = '';
    if (items.length === 0) {
        list.innerHTML = '<div class="empty-state">표시할 알림이 없어요.</div>';
    } else {
        items.forEach((item) => list.appendChild(renderCard(item)));
    }

    loadInFlight = false;
    flushPendingLiveNotifications();
}

function flushPendingLiveNotifications() {
    const toRender = pendingLiveNotifications;
    pendingLiveNotifications = [];
    toRender.forEach((notification) => insertLiveNotification(notification));
}

function insertLiveNotification(notification) {
    const list = document.getElementById('list');
    if (!list || list.querySelector(`[data-notification-id="${notification.id}"]`)) {
        return;
    }
    const item = {
        source: 'notification',
        id: notification.id,
        category: notification.category,
        title: notification.title,
        body: notification.message,
        read: notification.read,
        createdAt: notification.createdAt
    };
    list.querySelector('.empty-state')?.remove();
    list.prepend(renderCard(item));
}

document.addEventListener('DOMContentLoaded', () => {
    const tabs = document.getElementById('tabs');
    if (tabs) {
        tabs.addEventListener('click', (event) => {
            const button = event.target.closest('button[data-tab]');
            if (!button) return;
            document.querySelectorAll('#tabs button').forEach((b) => b.classList.remove('active'));
            button.classList.add('active');
            currentTab = button.dataset.tab;
            loadTab(currentTab);
        });
    }

    if (!authHeader()) {
        location.replace('/login');
    } else {
        loadTab(currentTab);
        if (typeof subscribeToNotifications === 'function') {
            subscribeToNotifications((notification) => {
                if (currentTab !== 'ALL' && currentTab !== 'DELIVERY') {
                    return;
                }
                if (loadInFlight) {
                    pendingLiveNotifications.push(notification);
                    return;
                }
                insertLiveNotification(notification);
            });
        }
    }
});
