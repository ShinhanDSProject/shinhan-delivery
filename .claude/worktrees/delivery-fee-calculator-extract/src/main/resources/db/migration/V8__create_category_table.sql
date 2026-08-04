CREATE TABLE category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

INSERT INTO category (name) VALUES
    ('전자기기/가전'),
    ('식품/음료'),
    ('의류/패션잡화'),
    ('서류/문서'),
    ('생활용품/잡화'),
    ('가구/인테리어'),
    ('화장품/뷰티'),
    ('도서/음반'),
    ('스포츠/레저'),
    ('반려동물 용품'),
    ('꽃/식물'),
    ('기타');
