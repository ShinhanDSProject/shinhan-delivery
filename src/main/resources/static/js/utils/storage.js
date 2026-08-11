const DRAFT_KEY = 'deliveryDraft';

// 배송 신청 화면 간(address-input/pickup-map/destination-map) 임시 입력값을 sessionStorage로 주고받는다.
function loadDraft() {
    try {
        const draft = JSON.parse(sessionStorage.getItem(DRAFT_KEY)) || {};
        return normalizeLegacyPickupGuide(draft);
    } catch (e) {
        return {};
    }
}

function normalizeLegacyPickupGuide(draft) {
    const guide = draft.pickupGuide;
    if (!guide || guide.deliveryInstructionType) {
        return draft;
    }

    const entranceCode = (guide.entranceCode || guide.doorPassword || '').trim();
    const deliveryNote = (guide.deliveryNote || guide.locationNote || '').trim();
    if (!entranceCode && !deliveryNote) {
        return draft;
    }

    guide.deliveryInstructionType = entranceCode ? 'ENTRANCE_CODE' : 'CUSTOM';
    guide.entranceCode = entranceCode;
    guide.deliveryNote = deliveryNote;
    sessionStorage.setItem(DRAFT_KEY, JSON.stringify(draft));
    return draft;
}

function saveDraft(draft) {
    sessionStorage.setItem(DRAFT_KEY, JSON.stringify(draft));
}
