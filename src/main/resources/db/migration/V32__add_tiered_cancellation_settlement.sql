ALTER TABLE delivery_request
    ADD COLUMN cancellation_fee BIGINT NULL
    /*! , ALGORITHM=INPLACE, LOCK=NONE */;

ALTER TABLE delivery_request
    ADD COLUMN refund_amount BIGINT NULL
    /*! , ALGORITHM=INPLACE, LOCK=NONE */;

ALTER TABLE delivery_request
    ADD COLUMN courier_compensation BIGINT NULL
    /*! , ALGORITHM=INPLACE, LOCK=NONE */;

ALTER TABLE delivery_request
    ADD COLUMN cancelled_by_member_id BIGINT NULL
    /*! , ALGORITHM=INPLACE, LOCK=NONE */;

ALTER TABLE delivery_request
    ADD COLUMN compensated_at TIMESTAMP NULL
    /*! , ALGORITHM=INPLACE, LOCK=NONE */;

ALTER TABLE delivery_request
    ADD COLUMN cancellation_previous_status VARCHAR(20) NULL
    /*! , ALGORITHM=INPLACE, LOCK=NONE */;
