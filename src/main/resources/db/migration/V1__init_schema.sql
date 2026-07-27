CREATE TABLE diary (
    id VARCHAR(36) PRIMARY KEY,
    date BIGINT NOT NULL,
    content VARCHAR(2000) NOT NULL,
    emotion_id INT NOT NULL,
    created_at DATETIME,
    updated_at DATETIME
);
