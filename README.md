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

# 2. 依存関係ダウンロード＆jOOQクラス生成
./gradlew build

# 3. テスト実行
./gradlew test

# 4. 開発サーバー起動
./gradlew bootRun
```

アプリケーションが起動したら、`http://localhost:8080` でアクセスできます。

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
./gradlew build                  # 全ビルド（テスト含む）
./gradlew test                   # テスト実行
./gradlew bootRun               # アプリケーション起動
./gradlew jooqCodegen           # jOOQクラス生成
./gradlew flywayMigrate         # DBマイグレーション実行
./gradlew flywayInfo            # マイグレーション状態確認
```
