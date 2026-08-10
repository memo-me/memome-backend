CREATE TABLE member (
    id BIGINT NOT NULL AUTO_INCREMENT,
    provider_id VARCHAR(255) NOT NULL,
    provider_type enum('GOOGLE', 'KAKAO') NOT NULL,
    nickname VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_member_provider UNIQUE(provider_id, provider_type)
);

CREATE TABLE memo (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    body LONGTEXT,
    author_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY(id)
);

ALTER TABLE memo
    ADD CONSTRAINT fk_memo_member
    FOREIGN KEY(author_id)
    REFERENCES member(id);
