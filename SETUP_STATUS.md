# セットアップ状況

**最終更新**: 2025-11-06

## 完了したタスク

### Phase 1: プロジェクト基盤 - 完了 ✅

### Phase 2: データベース設定 - 完了 ✅

#### 1.1 バックエンド基盤 ✅

- [x] Spring Bootプロジェクト初期化
  - [x] Gradle Kotlin DSL設定完了
  - [x] 依存関係設定完了（Spring Boot 3.4.0, Java 25）
  - [x] 必要な依存関係追加:
    - Spring Web, Spring Data JPA, Spring Security
    - PostgreSQL Driver, H2 Database
    - Flyway (データベースマイグレーション)
    - JWT (io.jsonwebtoken 0.12.3)
    - OpenAPI/Swagger (springdoc-openapi 2.3.0)
    - Lombok

- [x] パッケージ構造作成
  - [x] `net.hidearea.core.domain.entity/` パッケージ作成
  - [x] `net.hidearea.core.repository/` パッケージ作成
  - [x] `net.hidearea.core.service/` パッケージ作成
  - [x] `net.hidearea.core.security/` パッケージ作成
  - [x] `net.hidearea.core.api.v1.controller/` パッケージ作成
  - [x] `net.hidearea.core.api.v1.dto/` パッケージ作成
  - [x] `net.hidearea.core.config/` パッケージ作成
  - [x] `net.hidearea.core.exception/` パッケージ作成
  - [x] `net.hidearea.core.common/` パッケージ作成

- [x] application.yml設定
  - [x] データベース接続設定（PostgreSQL, H2）
  - [x] JPA/Hibernate設定
  - [x] ログ設定
  - [x] Spring Profilesの設定（dev, test, prod）
  - [x] JWT設定
  - [x] OpenAPI/Swagger設定

- [x] HideAreaApplication.java作成（メインクラス）
- [x] Flywayマイグレーションディレクトリ作成

#### 1.2 フロントエンド基盤 ✅

- [x] Vite + Reactプロジェクト初期化
  - [x] package.json作成
  - [x] TypeScript設定（tsconfig.json）
  - [x] Vite設定（vite.config.ts）
  - [x] 依存関係設定:
    - React 18.3.1
    - React Router 6.26.2
    - Axios 1.7.7
    - Zustand 4.5.5 (状態管理)
    - Material-UI 5.16.7

- [x] ディレクトリ構造作成
  - [x] `src/components/` ディレクトリ（common, layout, user）
  - [x] `src/pages/` ディレクトリ
  - [x] `src/services/v1/` ディレクトリ（APIバージョニング対応）
  - [x] `src/types/v1/` ディレクトリ（型定義、バージョニング対応）
  - [x] `src/hooks/` ディレクトリ
  - [x] `src/utils/` ディレクトリ
  - [x] `src/context/` ディレクトリ

- [x] 基本ファイル作成
  - [x] index.html
  - [x] main.tsx
  - [x] App.tsx
  - [x] index.css

#### 1.3 Docker設定 ✅

- [x] Docker Compose設定
  - [x] `docker-compose.yml` 作成
    - [x] PostgreSQL サービス定義
    - [x] Backend サービス定義
    - [x] Frontend サービス定義
    - [x] ネットワーク設定
    - [x] ボリューム設定（データ永続化）
    - [x] ヘルスチェック設定
  - [x] `.env.example` 作成

- [x] Dockerfile作成
  - [x] backend/Dockerfile（マルチステージビルド対応）
  - [x] frontend/Dockerfile（開発・本番ステージ分離）
  - [x] frontend/nginx.conf（本番用Nginx設定）

- [x] 起動スクリプト作成
  - [x] scripts/start-dev.sh（開発環境起動）
  - [x] scripts/stop-dev.sh（開発環境停止）
  - [x] scripts/clean.sh（クリーンアップ）

#### 2.1 Flywayマイグレーション設定 ✅

- [x] Flyway依存関係追加
  - [x] build.gradle.ktsに設定済み
  - [x] application.ymlでFlyway設定済み

- [x] マイグレーションファイル作成
  - [x] V1__init_schema.sql作成
    - [x] usersテーブル作成
    - [x] profilesテーブル作成
    - [x] user_profiles中間テーブル作成
    - [x] profile_profile_relations中間テーブル作成
    - [x] インデックス作成
    - [x] 外部キー制約設定
    - [x] コメント追加

#### 2.2 ビルドと起動確認 ✅

- [x] バックエンドビルド成功
  - [x] Java 21 LTS使用
  - [x] Gradle clean build実行
  - [x] JAR生成成功

- [x] アプリケーション起動成功
  - [x] testプロファイルでH2データベース使用
  - [x] ポート9090でTomcat起動
  - [x] Hibernateでテーブル自動生成確認
  - [x] Spring Security設定確認

- [x] APIエンドポイントテスト成功
  - [x] /v3/api-docs エンドポイント応答確認
  - [x] /api/v1/health エンドポイントテスト成功
  - [x] OpenAPI仕様取得成功

## 次のステップ

### 必要な環境構築

**✅ Java 21 LTS がインストール済みです**:
- バックエンドのビルドにはJava 21 LTSを使用しています
- Java 25はGradle 8.12のKotlin DSLで互換性の問題があるため、Java 21 LTSにダウングレードしました
- インストール済み: Eclipse Temurin 21+35 LTS

**注意**: 当初の計画ではJava 25を使用する予定でしたが、Gradle Kotlin DSLの互換性の問題により、安定版のJava 21 LTSを使用しています。

### 環境変数の確認

`.env`ファイルが作成されています。必要に応じて以下を変更してください:
- `DB_NAME`: データベース名（デフォルト: hidearea_dev）
- `DB_USERNAME`: データベースユーザー名（デフォルト: hidearea）
- `DB_PASSWORD`: データベースパスワード（デフォルト: hidearea）
- `JWT_SECRET`: JWT秘密鍵（本番環境では必ず変更）
- `VITE_API_BASE_URL`: フロントエンドからのAPI接続先（デフォルト: http://localhost:8080）

### ビルド確認

**✅ バックエンドのビルドが成功しました！**

```bash
# バックエンドのビルド確認（完了済み）
cd backend
./gradlew build -x test
# BUILD SUCCESSFUL in 23s
# 出力: backend/build/libs/hidearea-backend-0.0.1-SNAPSHOT.jar (64MB)

# フロントエンドの依存関係インストール（次のステップ）
cd frontend
npm install

# 開発環境の起動（Docker）
./scripts/start-dev.sh
```

### Phase 3への移行

Phase 1とPhase 2が完了しました。次は**Phase 3: ドメイン層**に進みます:
1. Enum定義（UserRole, ProfileType, RoleInProfile）
2. エンティティ実装（User, Profile, UserProfile, ProfileProfileRelation）
3. JPA Auditing有効化

## ディレクトリ構造

```
hidearea/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/net/hidearea/core/
│   │   │   │   ├── HideAreaApplication.java
│   │   │   │   ├── api/v1/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── domain/entity/
│   │   │   │   ├── config/
│   │   │   │   ├── security/
│   │   │   │   ├── exception/
│   │   │   │   └── common/
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/
│   │   └── test/
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── gradle.properties
│   ├── gradlew
│   └── Dockerfile
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/v1/
│   │   ├── types/v1/
│   │   ├── hooks/
│   │   ├── context/
│   │   ├── utils/
│   │   ├── App.tsx
│   │   ├── main.tsx
│   │   └── index.css
│   ├── package.json
│   ├── tsconfig.json
│   ├── vite.config.ts
│   ├── index.html
│   ├── nginx.conf
│   └── Dockerfile
│
├── scripts/
│   ├── start-dev.sh
│   ├── stop-dev.sh
│   └── clean.sh
│
├── docker-compose.yml
├── .env
├── .env.example
├── .gitignore
├── README.md
├── PROJECT_PLAN.md
└── SETUP_STATUS.md
```

## トラブルシューティング

### Gradleのビルドエラー

- Java 25がインストールされているか確認: `java -version`
- JAVA_HOME環境変数が設定されているか確認: `echo $JAVA_HOME`

### Dockerのエラー

- Dockerが起動しているか確認: `docker ps`
- ポートが使用されていないか確認: `lsof -i :5432,8080,5173`

## 参考ドキュメント

- [プロジェクト計画書](PROJECT_PLAN.md)
- [実装チェックリスト](docs/development/IMPLEMENTATION_CHECKLIST.md)
- [環境構築ガイド](docs/development/ENVIRONMENT_SETUP.md)
- [アーキテクチャ設計書](docs/architecture/ARCHITECTURE.md)
- [パッケージ設計書](docs/architecture/PACKAGE_DESIGN.md)
