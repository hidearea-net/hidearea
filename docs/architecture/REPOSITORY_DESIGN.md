# リポジトリ設計書

**プロジェクト名**: HideArea
**ドキュメントバージョン**: 1.0
**作成日**: 2025-11-04
**最終更新日**: 2025-11-04

---

## 目次

1. [リポジトリ構成戦略](#1-リポジトリ構成戦略)
2. [ディレクトリ構造](#2-ディレクトリ構造)
3. [モノレポ管理](#3-モノレポ管理)
4. [Git運用戦略](#4-git運用戦略)
5. [環境別設定管理](#5-環境別設定管理)
6. [ビルドとデプロイ](#6-ビルドとデプロイ)

---

## 1. リポジトリ構成戦略

### 1.1 モノレポ vs マルチレポ

本プロジェクトでは**モノレポ (Monorepo)** を採用します。

#### モノレポ採用の理由

| 観点 | モノレポのメリット |
|-----|------------------|
| **コード共有** | バックエンド・フロントエンド間でAPI型定義やユーティリティを容易に共有可能 |
| **バージョン管理** | 単一のバージョン履歴で一貫性を保持、APIの互換性管理が容易 |
| **統合管理** | Docker Compose、CI/CD設定を一元管理 |
| **開発効率** | 個人開発のため、複雑な依存関係管理が不要 |
| **リファクタリング** | クロスプロジェクトのリファクタリングが容易 |

#### マルチレポとの比較

| 項目 | モノレポ | マルチレポ |
|-----|---------|----------|
| コード共有 | 容易 | 複雑（npm/Maven経由） |
| 独立デプロイ | やや複雑 | 容易 |
| ビルド速度 | 遅くなる可能性 | 速い |
| 適用規模 | 小〜中規模向け | 大規模向け |
| **本プロジェクト適合性** | ✅ **高い** | ❌ 低い |

### 1.2 リポジトリ名

**リポジトリ名**: `hidearea`

**命名理由**:
- プロジェクト名を直接反映
- 短く覚えやすい
- 小文字のみ（Gitリポジトリ命名規約）

---

## 2. ディレクトリ構造

### 2.1 全体構造

```
hidearea/
├── backend/                    # バックエンド (Spring Boot)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/hidearea/core/
│   │   │   │       ├── HideAreaApplication.java
│   │   │   │       ├── api/
│   │   │   │       │   └── v1/
│   │   │   │       │       ├── controller/
│   │   │   │       │       └── dto/
│   │   │   │       ├── service/
│   │   │   │       ├── repository/
│   │   │   │       ├── domain/
│   │   │   │       ├── config/
│   │   │   │       ├── security/
│   │   │   │       ├── exception/
│   │   │   │       └── common/
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-dev.yml
│   │   │       ├── application-test.yml
│   │   │       ├── application-prod.yml
│   │   │       └── db/migration/        # Flyway migrations
│   │   └── test/
│   │       └── java/com/hidearea/core/
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── Dockerfile
│   └── .dockerignore
│
├── frontend/                   # フロントエンド (React + Vite)
│   ├── public/
│   ├── src/
│   │   ├── assets/
│   │   ├── components/
│   │   │   ├── common/
│   │   │   ├── layout/
│   │   │   └── user/
│   │   ├── pages/
│   │   ├── hooks/
│   │   ├── services/
│   │   │   ├── api.ts
│   │   │   └── v1/
│   │   │       ├── authService.ts
│   │   │       ├── userService.ts
│   │   │       └── profileService.ts
│   │   ├── context/
│   │   ├── types/
│   │   │   └── v1/
│   │   │       ├── user.ts
│   │   │       ├── profile.ts
│   │   │       └── auth.ts
│   │   ├── utils/
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── index.html
│   ├── package.json
│   ├── tsconfig.json
│   ├── vite.config.ts
│   ├── .eslintrc.cjs
│   ├── .prettierrc
│   ├── Dockerfile
│   └── .dockerignore
│
├── docker/                     # Docker関連設定
│   ├── docker-compose.dev.yml
│   ├── docker-compose.test.yml
│   ├── docker-compose.prod.yml
│   ├── nginx/
│   │   ├── nginx.conf
│   │   └── Dockerfile
│   └── postgres/
│       └── init.sql            # 初期化スクリプト（オプション）
│
├── docs/                       # ドキュメント
│   ├── architecture/           # アーキテクチャ設計書
│   │   ├── TECH_STACK.md
│   │   ├── ARCHITECTURE.md
│   │   ├── REPOSITORY_DESIGN.md  # 本ドキュメント
│   │   ├── PACKAGE_DESIGN.md
│   │   ├── DATA_DESIGN.md
│   │   ├── DATABASE_COMPARISON.md
│   │   └── DEVELOPER_ONBOARDING_COMPARISON.md
│   ├── api/                    # API仕様書
│   │   └── API_SPECIFICATION.md
│   └── development/            # 開発環境ドキュメント
│       ├── ENVIRONMENT_SETUP.md
│       └── DEVELOPMENT_GUIDE.md
│
├── scripts/                    # 起動・管理スクリプト
│   ├── start-dev.sh            # 開発環境起動
│   ├── start-test.sh           # テスト環境起動
│   ├── start-prod.sh           # 本番環境起動
│   ├── stop-all.sh             # 全環境停止
│   ├── clean-all.sh            # クリーンアップ
│   └── db-migrate.sh           # データベースマイグレーション
│
├── .github/                    # GitHub設定
│   └── workflows/
│       ├── backend-ci.yml      # バックエンドCI
│       ├── frontend-ci.yml     # フロントエンドCI
│       └── deploy.yml          # デプロイ（将来）
│
├── .gitignore
├── .editorconfig
├── README.md
└── PROJECT_PLAN.md             # プロジェクト計画書
```

### 2.2 各ディレクトリの役割

#### `backend/`

Spring Bootアプリケーションのルート。

**主要ファイル**:
- `build.gradle.kts`: Gradle設定（Kotlin DSL）
- `Dockerfile`: バックエンドコンテナイメージ
- `src/main/resources/application*.yml`: 環境別設定

**パッケージ構造**: [PACKAGE_DESIGN.md](./PACKAGE_DESIGN.md) 参照

#### `frontend/`

React + Vite + TypeScriptアプリケーションのルート。

**主要ファイル**:
- `package.json`: npm依存関係
- `vite.config.ts`: Vite設定
- `tsconfig.json`: TypeScript設定
- `Dockerfile`: フロントエンドコンテナイメージ

**ディレクトリ構造**: [PACKAGE_DESIGN.md](./PACKAGE_DESIGN.md) 参照

#### `docker/`

Docker Compose設定とコンテナ関連ファイル。

**環境別設定**:
- `docker-compose.dev.yml`: 開発環境（ホットリロード有効）
- `docker-compose.test.yml`: テスト環境（自動テスト用）
- `docker-compose.prod.yml`: 本番環境（最適化ビルド）

#### `docs/`

プロジェクトドキュメント。

**カテゴリ**:
- `architecture/`: 設計ドキュメント
- `api/`: API仕様書
- `development/`: 開発者向けガイド

#### `scripts/`

プロジェクト管理スクリプト。

**主要スクリプト**:
- `start-dev.sh`: 開発環境起動
- `stop-all.sh`: 全コンテナ停止
- `clean-all.sh`: ビルド成果物削除

#### `.github/workflows/`

GitHub Actions CI/CD設定。

**ワークフロー**:
- `backend-ci.yml`: バックエンドビルド・テスト
- `frontend-ci.yml`: フロントエンドビルド・テスト・Lint

---

## 3. モノレポ管理

### 3.1 依存関係管理

#### バックエンド（Gradle）

**`backend/build.gradle.kts`**:
```kotlin
plugins {
    java
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.5"
}

group = "com.hidearea"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_25
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")

    // Database
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2") // 開発用

    // Flyway
    implementation("org.flywaydb:flyway-core")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}
```

#### フロントエンド（npm/pnpm）

**`frontend/package.json`**:
```json
{
  "name": "hidearea-frontend",
  "version": "0.1.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview",
    "lint": "eslint . --ext ts,tsx --report-unused-disable-directives --max-warnings 0",
    "test": "vitest"
  },
  "dependencies": {
    "react": "^18.3.0",
    "react-dom": "^18.3.0",
    "react-router-dom": "^6.23.0",
    "axios": "^1.7.0"
  },
  "devDependencies": {
    "@types/react": "^18.3.0",
    "@types/react-dom": "^18.3.0",
    "@typescript-eslint/eslint-plugin": "^7.0.0",
    "@typescript-eslint/parser": "^7.0.0",
    "@vitejs/plugin-react": "^4.3.0",
    "eslint": "^8.57.0",
    "eslint-plugin-react-hooks": "^4.6.0",
    "eslint-plugin-react-refresh": "^0.4.6",
    "prettier": "^3.2.5",
    "typescript": "^5.4.0",
    "vite": "^5.2.0",
    "vitest": "^1.6.0"
  }
}
```

### 3.2 共有設定

#### `.editorconfig`

エディタ統一設定:

```ini
root = true

[*]
charset = utf-8
end_of_line = lf
insert_final_newline = true
trim_trailing_whitespace = true

[*.{java,kt,kts}]
indent_style = space
indent_size = 4

[*.{ts,tsx,js,jsx,json,yml,yaml}]
indent_style = space
indent_size = 2

[*.md]
trim_trailing_whitespace = false
```

#### `.gitignore`

```gitignore
# IDE
.idea/
.vscode/
*.iml

# Backend
backend/build/
backend/out/
backend/.gradle/
backend/bin/

# Frontend
frontend/node_modules/
frontend/dist/
frontend/.vite/

# Environment
.env
.env.local
.env.*.local

# OS
.DS_Store
Thumbs.db

# Logs
*.log
logs/

# Docker
*.pid
*.seed
*.pid.lock
```

---

## 4. Git運用戦略

### 4.1 ブランチ戦略

**GitHub Flow** を採用（個人開発に最適）。

#### ブランチ構成

```
main (保護ブランチ)
  ↑
  ├── feature/user-management
  ├── feature/profile-api
  ├── fix/login-error
  └── docs/update-readme
```

#### ブランチ命名規則

| タイプ | プレフィックス | 例 |
|-------|-------------|---|
| 新機能 | `feature/` | `feature/user-registration` |
| バグ修正 | `fix/` | `fix/jwt-expiration` |
| ドキュメント | `docs/` | `docs/api-spec-update` |
| リファクタリング | `refactor/` | `refactor/service-layer` |
| テスト | `test/` | `test/add-integration-tests` |
| 実験的機能 | `experimental/` | `experimental/websocket` |

### 4.2 コミットメッセージ規約

**Conventional Commits** 形式を採用。

#### フォーマット

```
<type>(<scope>): <subject>

<body>

<footer>
```

#### タイプ一覧

| タイプ | 説明 | 例 |
|-------|-----|---|
| `feat` | 新機能 | `feat(auth): add JWT authentication` |
| `fix` | バグ修正 | `fix(profile): resolve null pointer exception` |
| `docs` | ドキュメント | `docs(readme): update setup instructions` |
| `style` | コードスタイル | `style(backend): apply code formatting` |
| `refactor` | リファクタリング | `refactor(service): simplify user service logic` |
| `test` | テスト追加・修正 | `test(auth): add login integration tests` |
| `chore` | ビルド・設定変更 | `chore(deps): update spring boot to 3.3.0` |
| `perf` | パフォーマンス改善 | `perf(db): add index to user table` |

#### コミットメッセージ例

```
feat(auth): add user registration endpoint

- Implement POST /api/v1/auth/register
- Add password validation
- Hash passwords with BCrypt

Closes #123
```

### 4.3 プルリクエスト (PR)

#### PRテンプレート

```markdown
## 概要
<!-- 変更内容の簡潔な説明 -->

## 変更内容
- [ ] 新機能追加
- [ ] バグ修正
- [ ] リファクタリング
- [ ] ドキュメント更新

## テスト
<!-- テスト方法と結果 -->

## チェックリスト
- [ ] コードがビルド可能
- [ ] テストが通過
- [ ] ドキュメント更新済み
- [ ] コミットメッセージが規約に準拠

## 関連Issue
Closes #<issue番号>
```

#### マージ戦略

- **Squash and merge**: 推奨（履歴をクリーンに保つ）
- **Rebase and merge**: 大規模な機能追加時
- **Merge commit**: 避ける（履歴が複雑化）

---

## 5. 環境別設定管理

### 5.1 バックエンド環境設定

#### `application.yml`（共通設定）

```yaml
spring:
  application:
    name: hidearea-core
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

server:
  port: 8080

logging:
  level:
    com.hidearea: INFO
```

#### `application-dev.yml`（開発環境）

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:hidearea
    username: sa
    password:
  h2:
    console:
      enabled: true
  jpa:
    show-sql: true

logging:
  level:
    com.hidearea: DEBUG
```

#### `application-prod.yml`（本番環境）

```yaml
spring:
  datasource:
    url: jdbc:postgresql://db:5432/hidearea
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate

server:
  port: 8080

logging:
  level:
    com.hidearea: WARN
```

### 5.2 フロントエンド環境設定

#### `.env.development`

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_ENV=development
```

#### `.env.production`

```env
VITE_API_BASE_URL=https://api.hidearea.com
VITE_APP_ENV=production
```

### 5.3 Docker環境変数

#### `.env`（Gitに含めない）

```env
# Database
DB_USERNAME=hidearea_user
DB_PASSWORD=secure_password
DB_DATABASE=hidearea

# JWT
JWT_SECRET=your_jwt_secret_key_here

# Environment
ENVIRONMENT=dev
```

---

## 6. ビルドとデプロイ

### 6.1 ローカルビルド

#### バックエンドビルド

```bash
cd backend
./gradlew clean build
```

#### フロントエンドビルド

```bash
cd frontend
npm install
npm run build
```

### 6.2 Docker Composeビルド

#### 開発環境

```bash
cd docker
docker-compose -f docker-compose.dev.yml up --build
```

#### 本番環境

```bash
cd docker
docker-compose -f docker-compose.prod.yml up -d --build
```

### 6.3 CI/CDパイプライン（将来対応）

#### GitHub Actions

**バックエンドCI** (`.github/workflows/backend-ci.yml`):
```yaml
name: Backend CI

on:
  push:
    branches: [ main ]
    paths:
      - 'backend/**'
  pull_request:
    branches: [ main ]
    paths:
      - 'backend/**'

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 25
        uses: actions/setup-java@v4
        with:
          java-version: '25'
          distribution: 'temurin'
      - name: Build with Gradle
        run: |
          cd backend
          ./gradlew clean build
      - name: Run tests
        run: |
          cd backend
          ./gradlew test
```

**フロントエンドCI** (`.github/workflows/frontend-ci.yml`):
```yaml
name: Frontend CI

on:
  push:
    branches: [ main ]
    paths:
      - 'frontend/**'
  pull_request:
    branches: [ main ]
    paths:
      - 'frontend/**'

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
      - name: Install dependencies
        run: |
          cd frontend
          npm ci
      - name: Lint
        run: |
          cd frontend
          npm run lint
      - name: Build
        run: |
          cd frontend
          npm run build
      - name: Test
        run: |
          cd frontend
          npm test
```

---

## まとめ

本リポジトリ設計書では、HideAreaプロジェクトのモノレポ構成、ディレクトリ構造、Git運用戦略、環境別設定管理を定義しました。

**設計のポイント**:
- モノレポによる統一的なコード管理
- 明確なディレクトリ階層とファイル配置
- GitHub Flowによるシンプルなブランチ戦略
- Conventional Commitsによる一貫したコミット履歴
- 環境別設定の適切な分離

この設計により、個人開発でも保守性と拡張性を保ちながら、効率的な開発が可能になります。

---

**次のステップ**: [開発環境セットアップガイド](../development/ENVIRONMENT_SETUP.md) を参照し、環境構築を開始してください。
