-- 著者テーブルの作成
CREATE TABLE authors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    birthdate DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_birthdate_past CHECK (birthdate <= CURRENT_DATE)
);

-- 出版状況テーブルの作成
CREATE TABLE publication_statuses (
    id INTEGER PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 初期出版状況データの挿入（明示的にIDを指定）
INSERT INTO publication_statuses (id, name) VALUES
    (1, '未出版'),
    (2, '出版済み');

-- 書籍テーブルの作成
CREATE TABLE books (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    price INTEGER NOT NULL,
    status_id INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_price_positive CHECK (price >= 0),
    CONSTRAINT fk_book_status FOREIGN KEY (status_id) REFERENCES publication_statuses (id)
);

-- 書籍と著者の著作権関係を表すテーブルの作成
CREATE TABLE authorships (
    book_id UUID NOT NULL,
    author_id UUID NOT NULL,
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES authors (id) ON DELETE CASCADE
);

-- 更新時のタイムスタンプを自動更新するための関数
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = CURRENT_TIMESTAMP;
   RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 著者テーブルの更新トリガー
CREATE TRIGGER update_authors_timestamp
BEFORE UPDATE ON authors
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

-- 書籍テーブルの更新トリガー
CREATE TRIGGER update_books_timestamp
BEFORE UPDATE ON books
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

-- 出版状況テーブルの更新トリガー
CREATE TRIGGER update_publication_statuses_timestamp
BEFORE UPDATE ON publication_statuses
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();
