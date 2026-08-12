/** 고객 본인의 과거 배송을 신규 배송 초안으로 변환하고 배송 신청 화면으로 이동한다. */
async function startDeliveryReorder(deliveryRequestId, onError) {
    const token = localStorage.getItem('accessToken');
    const tokenType = localStorage.getItem('tokenType') || 'Bearer';
    if (!token) {
        location.replace('/login');
        return;
    }

    try {
        const response = await fetch(`/api/v1/delivery-requests/${deliveryRequestId}/reorder`, {
            headers: { Authorization: `${tokenType} ${token}` },
            cache: 'no-store'
        });
        if (response.status === 401) {
            location.replace('/login');
            return;
        }
        if (!response.ok) {
            throw new Error('재배송 정보를 불러오지 못했습니다.');
        }

        const source = await response.json();
        saveDraft({
            reorderSourceId: source.sourceDeliveryRequestId,
            pickup: {
                address: source.pickupAddress,
                detail: '',
                lat: source.pickupLatitude,
                lng: source.pickupLongitude
            },
            dropoff: {
                address: source.dropoffAddress,
                detail: '',
                lat: source.dropoffLatitude,
                lng: source.dropoffLongitude
            },
            weight: source.weight,
            itemSize: source.itemSize,
            categoryName: '기타'
        });
        location.href = '/address-input?reorder=success';
    } catch (error) {
        if (typeof onError === 'function') {
            onError('재배송 정보를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.');
        }
    }
}
