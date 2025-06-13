# Book Management System

Spring Boot + Kotlin + jOOQを使用した書籍管理システム

## 開発環境セットアップ

### 前提条件

このプロジェクトを実行するために必要な環境を以下にまとめています。

#### 必須環境
- **JDK 21** 
  - JAVA_HOMEが適切に設定されていること

- **Docker** 
  - PostgreSQLコンテナを起動するため
  - Docker Composeも使用します (`compose.yaml`参照)

### データベース準備

このプロジェクトでは、Docker Composeを使用してPostgreSQLコンテナを起動します：

```bash
# PostgreSQLコンテナを起動
docker compose up -d postgres

# コンテナの状態確認
docker compose ps

# ログ確認（必要に応じて）
docker compose logs postgres
```

**データベース接続情報**:
- ホスト: `localhost`
- ポート: `5432`
- データベース名: `bookdb`
- ユーザー名: `postgres`
- パスワード: `postgres`

```bash
# PostgreSQLコンテナを停止する場合
docker compose down

# データも含めて完全に削除する場合
docker compose down -v
```

### ビルド & 実行手順

```bash
# 1. PostgreSQLコンテナ起動
docker compose up -d postgres

# 2. データベースマイグレーション＆jOOQクラス生成
./gradlew migrateAndGenerateJooq

# 3. アプリケーションビルド（テスト含む）
./gradlew build

# 4. 開発サーバー起動
./gradlew bootRun
```

## プロジェクト構成

### 技術スタック
- **言語**: Kotlin
- **フレームワーク**: Spring Boot 3.x
- **データベース**: PostgreSQL
- **ORM**: jOOQ
- **マイグレーション**: Flyway
- **テスト**: JUnit 5 + Testcontainers
- **ビルドツール**: Gradle

### ディレクトリ構成

```
src/
└── main/kotlin/io/github/t_suguru/book_management/
    ├── controller/
    │   ├── AuthorController.kt      // 著者API
    │   └── BookController.kt        // 書籍API
    ├── dto/
    │   ├── AuthorCreateRequest.kt   // 著者作成リクエスト
    │   ├── AuthorUpdateRequest.kt   // 著者更新リクエスト
    │   ├── BookCreateRequest.kt     // 書籍作成リクエスト
    │   └── BookUpdateRequest.kt     // 書籍更新リクエスト
    ├── domain/
    │   ├── model/
    │   │   ├── Author.kt            // 著者ドメインモデル
    │   │   ├── Book.kt              // 書籍ドメインモデル
    │   │   └── PublicationStatus.kt // 出版状況enum
    │   └── repository/
    │       ├── AuthorRepository.kt  // 著者用リポジトリインターフェース
    │       └── BookRepository.kt    // 書籍用リポジトリインターフェース
    ├── infrastructure/
    │   └── repository/
    │       ├── AuthorRepositoryImpl.kt  // 著者リポジトリ実装
    │       └── BookRepositoryImpl.kt    // 書籍リポジトリ実装
    └── service/
        ├── AuthorService.kt         // 著者ビジネスロジック
        └── BookService.kt           // 書籍ビジネスロジック
```

### 主要なGradleタスク
```bash
./gradlew build                     # 全ビルド（マイグレーション→jOOQ生成→コンパイル→テスト）
./gradlew migrateAndGenerateJooq    # DBマイグレーション→jOOQクラス生成
./gradlew flywayMigrate            # DBマイグレーション実行
./gradlew jooqCodegen              # jOOQクラス生成（※マイグレーション後に実行）
./gradlew test                     # テスト実行
./gradlew bootRun                  # アプリケーション起動
./gradlew flywayInfo               # マイグレーション状態確認
```

**重要**: jOOQはデータベーススキーマからKotlinクラスを生成するため、`jooqCodegen`の前に必ず`flywayMigrate`でスキーマを作成する必要があります。`./gradlew build`を実行すると、この依存関係は自動的に解決されます。

## API エンドポイント

### 著者 API

#### 著者作成
```bash
curl -X POST http://localhost:8080/api/authors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "夏目漱石",
    "birthdate": "1867-02-09"
  }'
```

#### 著者更新
```bash
# 先に著者を作成してIDを取得後、以下のコマンドでIDを置き換えてください
curl -X PUT http://localhost:8080/api/authors/{author-id} \
  -H "Content-Type: application/json" \
  -d '{
    "name": "夏目漱石（ペンネーム）",
    "birthdate": "1867-02-09"
  }'
```

#### 著者の書籍一覧取得
```bash
# 先に著者と書籍を作成してIDを取得後、以下のコマンドでIDを置き換えてください
curl -X GET http://localhost:8080/api/authors/{author-id}/books
```

### 書籍 API

#### 書籍作成
```bash
# 先に著者を作成してIDを取得後、以下のコマンドでauthorIdsを置き換えてください
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "吾輩は猫である",
    "price": 1500,
    "status": "UNPUBLISHED",
    "authorIds": ["{author-id}"]
  }'
```

#### 書籍更新
```bash
# 先に書籍を作成してIDを取得後、以下のコマンドで{book-id}と{author-id}を置き換えてください
curl -X PUT http://localhost:8080/api/books/{book-id} \
  -H "Content-Type: application/json" \
  -d '{
    "title": "吾輩は猫である（改訂版）",
    "price": 1800,
    "status": "PUBLISHED",
    "authorIds": ["{author-id}"]
  }'
```

### 使用例（実際のフロー）

以下は実際にデータを作成から更新まで行う例です：

#### 方法1: jqを使用する場合
```bash
# 1. 著者を作成
AUTHOR_RESPONSE=$(curl -s -X POST http://localhost:8080/api/authors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "夏目漱石",
    "birthdate": "1867-02-09"
  }')

# 著者IDを抽出（jqが必要）
AUTHOR_ID=$(echo $AUTHOR_RESPONSE | jq -r '.id')

# 2. 書籍を作成
BOOK_RESPONSE=$(curl -s -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"吾輩は猫である\",
    \"price\": 1500,
    \"status\": \"UNPUBLISHED\",
    \"authorIds\": [\"$AUTHOR_ID\"]
  }")

# 書籍IDを抽出
BOOK_ID=$(echo $BOOK_RESPONSE | jq -r '.id')

# 3. 著者の書籍一覧を取得
curl -X GET http://localhost:8080/api/authors/$AUTHOR_ID/books

# 4. 書籍を更新（出版状況を変更）
curl -X PUT http://localhost:8080/api/books/$BOOK_ID \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"吾輩は猫である\",
    \"price\": 1500,
    \"status\": \"PUBLISHED\",
    \"authorIds\": [\"$AUTHOR_ID\"]
  }"
```

#### 方法2: jqなしでsedを使用する場合
```bash
# 1. 著者を作成
AUTHOR_RESPONSE=$(curl -s -X POST http://localhost:8080/api/authors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "夏目漱石",
    "birthdate": "1867-02-09"
  }')

# 著者IDを抽出（JSONの先頭のidフィールドを取得）
AUTHOR_ID=$(echo $AUTHOR_RESPONSE | sed 's/^{"id":"\([^"]*\)".*/\1/')

# 2. 書籍を作成
BOOK_RESPONSE=$(curl -s -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"吾輩は猫である\",
    \"price\": 1500,
    \"status\": \"UNPUBLISHED\",
    \"authorIds\": [\"$AUTHOR_ID\"]
  }")

# 書籍IDを抽出（JSONの先頭のidフィールドを取得）
BOOK_ID=$(echo $BOOK_RESPONSE | sed 's/^{"id":"\([^"]*\)".*/\1/')

# 3. 著者の書籍一覧を取得
curl -X GET http://localhost:8080/api/authors/$AUTHOR_ID/books

# 4. 書籍を更新（出版状況を変更）
curl -X PUT http://localhost:8080/api/books/$BOOK_ID \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"吾輩は猫である\",
    \"price\": 1500,
    \"status\": \"PUBLISHED\",
    \"authorIds\": [\"$AUTHOR_ID\"]
  }"
```

### エラーレスポンス例

#### バリデーションエラー（400）
```bash
# 空の名前で著者作成を試行
curl -X POST http://localhost:8080/api/authors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "birthdate": "1867-02-09"
  }'
```

#### リソースが見つからない（404）
```bash
# 存在しない著者IDで更新を試行
curl -X PUT http://localhost:8080/api/authors/00000000-0000-0000-0000-000000000000 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "存在しない著者",
    "birthdate": "1867-02-09"
  }'
```

#### ビジネスルール違反（400）
```bash
# 出版済み書籍を未出版に変更しようとする
curl -X PUT http://localhost:8080/api/books/{published-book-id} \
  -H "Content-Type: application/json" \
  -d '{
    "title": "出版済み書籍",
    "price": 1500,
    "status": "UNPUBLISHED",
    "authorIds": ["{author-id}"]
  }'
```

### 出版状況の値

書籍作成・更新時の`status`フィールドには以下の値を使用してください：
- `UNPUBLISHED`: 未出版
- `PUBLISHED`: 出版済み

**注意**: 出版済み（`PUBLISHED`）の書籍を未出版（`UNPUBLISHED`）に変更することはできません。
