CREATE TABLE notice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

-- linter:ignore-online-ddl
CREATE INDEX idx_notice_pinned_created ON notice (is_pinned DESC, created_at DESC);

INSERT INTO notice (title, content, category, is_pinned, created_at, updated_at) VALUES
('[안내] 딜리버리 해피니스 서비스 정기 점검 안내', '안녕하세요. 딜리버리 해피니스 서비스의 안정적인 운영을 위한 정기 점검이 진행될 예정입니다.\n\n■ 점검 일시: 2026년 8월 1일 02:00 ~ 06:00 (4시간)\n■ 영향 범위: 서비스 전체 이용 및 배송 요청 일시 중단\n\n이용에 불편을 드려 죄송합니다.', 'SYSTEM', true, NOW(), NOW()),
('[이벤트] 신규 가입 회원 대상 배송 할인 쿠폰 지급', '신규 회원가입 고객님들을 위한 첫 배송 3,000원 할인 쿠폰이 일괄 발급되었습니다.\n마이페이지 > 쿠폰함에서 확인해 보세요!', 'EVENT', false, NOW(), NOW()),
('[안내] 실시간 위치 추적 기능 업데이트 안내', '배송 상태 및 이동 경로를 실시간 지도 화면에서 확인할 수 있는 위치 추적 기능이 업데이트되었습니다.', 'SERVICE', false, NOW(), NOW());
