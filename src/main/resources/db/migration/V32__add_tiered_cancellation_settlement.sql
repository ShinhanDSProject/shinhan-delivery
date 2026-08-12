-- linter:ignore-online-ddl (기존 배송은 취소 정산 값이 없으므로 nullable로 추가한다)
ALTER TABLE delivery_request ADD COLUMN cancellation_fee BIGINT NULL;

-- linter:ignore-online-ddl (기존 배송은 환불 정산 값이 없으므로 nullable로 추가한다)
ALTER TABLE delivery_request ADD COLUMN refund_amount BIGINT NULL;

-- linter:ignore-online-ddl (배정 전 취소는 배송원 보상이 없으므로 nullable로 추가한다)
ALTER TABLE delivery_request ADD COLUMN courier_compensation BIGINT NULL;

-- linter:ignore-online-ddl (취소를 요청한 고객 member id 감사 값)
ALTER TABLE delivery_request ADD COLUMN cancelled_by_member_id BIGINT NULL;

-- linter:ignore-online-ddl (배송원 보상 완료 시각)
ALTER TABLE delivery_request ADD COLUMN compensated_at TIMESTAMP NULL;

-- linter:ignore-online-ddl (멱등 재요청에도 최초 취소 직전 상태를 반환하기 위한 감사 값)
ALTER TABLE delivery_request ADD COLUMN cancellation_previous_status VARCHAR(20) NULL;
