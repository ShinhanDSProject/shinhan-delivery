const PAGE_SIZE = 10;
const IN_PROGRESS_STATUSES = ['REQUESTED', 'MATCHED', 'PICKED_UP'];

let currentTab = 'ALL';
let currentPage = 0;
let isLastPage = false;
let isLoading = false;
let requestGeneration = 0;

function authHeader() {
    const token = localStorage.getItem('accessToken');
    const tokenType = localStorage.getItem('tokenType') || 'Bearer';
    if (!token) return null;
    return `${tokenType} ${token}`;
}

function formatDate(isoString) {
    const date = new Date(isoString);
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const dd = String(date.getDate()).padStart(2, '0');
    return `${yyyy}.${mm}.${dd}`;
}

function badgeFor(status) {
    if (status === 'COMPLETED') return { className: 'completed', label: '완료' };
    if (status === 'CANCELLED') return { className: 'cancelled', label: '취소' };
    return { className: 'in-progress', label: '진행중' };
}

async function fetchPage(tab, page) {
    const header = authHeader();
    if (!header) return null;
    const statusParam = tab === 'COMPLETED' || tab === 'CANCELLED' ? `&status=${tab}` : '';
    const response = await fetch(`/api/v1/delivery-requests?page=${page}&size=${PAGE_SIZE}${statusParam}`, {
        headers: { Authorization: header }
    });
    if (response.status === 401 || response.status === 403) {
        location.replace('/login');
        return null;
    }
    if (!response.ok) return null;
    return response.json();
}

function renderCard(item) {
    const badge = badgeFor(item.status);
    const card = document.createElement('article');
    card.className = 'history-card';
    card.tabIndex = 0;
    card.setAttribute('role', 'link');
    const detailUrl = (item.status === 'CANCELLED' ? '/cancel-detail?id=' : '/delivery-detail?id=')
        + encodeURIComponent(item.id);
    card.addEventListener('click', () => { location.href = detailUrl; });
    card.addEventListener('keydown', (event) => {
        if (event.key === 'Enter' && event.target === card) location.href = detailUrl;
    });
    card.innerHTML = `
        <div class="card-top">
            <span class="date">${formatDate(item.createdAt)}</span>
            <span class="status-badge ${badge.className}">${badge.label}</span>
        </div>
        <div class="card-mid">
            <div class="route" data-field="route"></div>
        </div>
        <div class="card-bottom">
            <span class="fee">${item.feePoint.toLocaleString()} P</span>
            <button class="reorder-button" type="button">이 배송 다시 보내기</button>
        </div>
    `;
    card.querySelector('[data-field="route"]').textContent = `${item.pickupAddress} → ${item.dropoffAddress}`;
    card.querySelector('.reorder-button').addEventListener('click', (event) => {
        event.stopPropagation();
        if (typeof startDeliveryReorder === 'function') {
            startDeliveryReorder(item.id, showReorderError);
        }
    });
    return card;
}

function showReorderError(message) {
    const existing = document.getElementById('reorderError');
    if (existing) existing.remove();
    const error = document.createElement('div');
    error.id = 'reorderError';
    error.className = 'history-error';
    error.textContent = message;
    document.body.appendChild(error);
    setTimeout(() => error.remove(), 3000);
}

async function loadNextPage() {
    if (isLoading || isLastPage) return;
    isLoading = true;
    const generation = requestGeneration;
    const tab = currentTab;
    const page = currentPage;

    const list = document.getElementById('list');
    if (page === 0) {
        list.innerHTML = '<div class="loading-state">불러오는 중...</div>';
    }

    try {
        const response = await fetchPage(tab, page);

        if (generation !== requestGeneration) {
            return;
        }
        if (!response) return;

        let items = response.content || [];
        if (tab === 'IN_PROGRESS') {
            items = items.filter((item) => IN_PROGRESS_STATUSES.includes(item.status));
        }

        if (page === 0) {
            list.innerHTML = '';
        }

        if (page === 0 && items.length === 0 && response.last) {
            list.innerHTML = '<div class="empty-state">표시할 배송 내역이 없어요.</div>';
        } else {
            items.forEach((item) => list.appendChild(renderCard(item)));
        }

        isLastPage = response.last;
        currentPage += 1;
    } catch (error) {
        if (generation === requestGeneration && page === 0) {
            list.innerHTML = '<div class="empty-state">배송 내역을 불러오지 못했어요.</div>';
        }
    } finally {
        if (generation === requestGeneration) {
            isLoading = false;
        }
    }
}

function resetAndLoad(tab) {
    requestGeneration += 1;
    currentTab = tab;
    currentPage = 0;
    isLastPage = false;
    isLoading = false;
    loadNextPage();
}

document.addEventListener('DOMContentLoaded', () => {
    const tabs = document.getElementById('tabs');
    if (tabs) {
        tabs.addEventListener('click', (event) => {
            const button = event.target.closest('button[data-tab]');
            if (!button) return;
            document.querySelectorAll('#tabs button').forEach((b) => b.classList.remove('active'));
            button.classList.add('active');
            resetAndLoad(button.dataset.tab);
        });
    }

    window.addEventListener('scroll', () => {
        const scrolledToBottom = window.innerHeight + window.scrollY >= document.body.offsetHeight - 200;
        if (scrolledToBottom) loadNextPage();
    });

    if (!authHeader()) {
        location.replace('/login');
    } else {
        loadNextPage();
    }
});
