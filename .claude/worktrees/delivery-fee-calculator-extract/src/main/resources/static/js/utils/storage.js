const DRAFT_KEY = 'deliveryDraft';

// 배송 신청 화면 간(address-input/pickup-map/destination-map) 임시 입력값을 sessionStorage로 주고받는다.
function loadDraft() {
    try {
        return JSON.parse(sessionStorage.getItem(DRAFT_KEY)) || {};
    } catch (e) {
        return {};
    }
}

function saveDraft(draft) {
    sessionStorage.setItem(DRAFT_KEY, JSON.stringify(draft));
}
