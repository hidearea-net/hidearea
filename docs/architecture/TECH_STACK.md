# 技術スタック詳細

**最終更新日**: 2025-11-04

---

## 目次

1. [バックエンド技術](#1-バックエンド技術)
2. [フロントエンド技術](#2-フロントエンド技術)
3. [データベース](#3-データベース)
4. [インフラ・DevOps](#4-インフラdevops)
5. [開発ツール](#5-開発ツール)
6. [決済プラットフォーム](#6-決済プラットフォーム)

---

## 1. バックエンド技術

### 1.1 言語・フレームワーク

#### Java 25 (LTS)

- **選定理由**:
  - 最新の長期サポート版（LTS）
  - パフォーマンス向上、新機能の活用
  - Spring Boot 3.xとの完全互換性

- **主要機能**:
  - Virtual Threads（Project Loom）による軽量スレッド
  - Pattern Matching の拡張
  - Record Patterns
  - Switch 式の改善

#### Spring Boot 3.x

- **選定理由**:
  - エンタープライズグレードのフレームワーク
  - 豊富なエコシステム
  - 自動設定による開発効率向上
  - Spring Framework 6.x ベース

- **主要モジュール**:

| モジュール | 用途 |
|-----------|------|
| Spring Web | REST API実装 |
| Spring Data JPA | データアクセス層 |
| Spring Security | 認証・認可 |
| Spring Validation | 入力バリデーション |
| Spring Boot Actuator | ヘルスチェック、メトリクス |
| Spring Boot DevTools | 開発支援（ホットリロード） |

### 1.2 ビルドツール

#### Gradle 8.x (Kotlin DSL)

- **選定理由**:
  - Mavenより高速なビルド
  - Kotlin DSLによる型安全な設定
  - 柔軟なタスク定義

- **build.gradle.kts サンプル**:

```kotlin
plugins {
    java
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.hidearea"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_25

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")

    // Database
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2")

    // Migration
    implementation("org.flywaydb:flyway-core")

    // OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}
```

### 1.3 通信プロトコル

#### MVPフェーズ: REST API

- **形式**: JSON
- **エンドポイント設計**: RESTful規約に準拠
- **バージョニング**: `/api/v1/...` 形式

**標準レスポンス形式**:

```json
{
  "status": "success",
  "data": {
    "id": 1,
    "username": "johndoe"
  },
  "message": null
}
```

**エラーレスポンス形式**:

```json
{
  "status": "error",
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "User not found",
    "details": null
  },
  "timestamp": "2025-11-04T10:00:00Z"
}
```

#### 将来フェーズ: リアルタイム通信

| プロトコル | 用途 | 実装時期 |
|-----------|------|---------|
| **WebSocket** | 双方向リアルタイム通信（チャット） | 第2フェーズ |
| **SSE** | サーバープッシュ通知 | 第2フェーズ |
| **メッセージング** | 非同期処理（Spring AMQP/Kafka） | 第3フェーズ以降 |

### 1.4 認証・認可

#### Spring Security + JWT

- **認証方式**: JWT（JSON Web Token）
  - アルゴリズム: HS256
  - 有効期限: 24時間
  - リフレッシュトークン: オプション（将来対応）

- **認可方式**: RBAC（Role-Based Access Control）
  - ロール: `USER`, `ADMIN`
  - メソッドレベルセキュリティ: `@PreAuthorize`

**JWT構造例**:

```json
{
  "sub": "johndoe",
  "userId": 1,
  "role": "USER",
  "iat": 1699084800,
  "exp": 1699171200
}
```

### 1.5 データアクセス

#### Spring Data JPA

- **実装**: Hibernate
- **データベース抽象化**: JPA仕様準拠
- **リポジトリパターン**: `JpaRepository` 継承

**リポジトリ例**:

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
}
```

### 1.6 バリデーション

#### Jakarta Bean Validation (Hibernate Validator)

- **アノテーションベース**: `@NotNull`, `@Email`, `@Size` など
- **カスタムバリデーション**: 独自アノテーション定義可能

**DTO例**:

```java
public record UserCreateRequest(
    @NotBlank @Size(min = 3, max = 50) String username,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String password
) {}
```

### 1.7 API仕様書

#### SpringDoc OpenAPI (Swagger UI)

- **自動生成**: アノテーションからAPI仕様を自動生成
- **Swagger UI**: ブラウザでのAPI動作確認
- **アクセス**: `http://localhost:8080/swagger-ui.html`

---

## 2. フロントエンド技術

### 2.1 言語・フレームワーク

#### TypeScript 5.x

- **選定理由**:
  - 型安全性によるバグ削減
  - IDEサポート充実
  - Reactとの相性良好

- **設定**: 厳格モード（`strict: true`）

#### React 18.x

- **選定理由**:
  - 大規模コミュニティ、豊富なライブラリ
  - 関数コンポーネント + Hooks による modern な開発
  - Virtual DOM による高速レンダリング

- **主要機能**:
  - Concurrent Rendering
  - Automatic Batching
  - Suspense

#### Vite 5.x

- **選定理由**:
  - 超高速な開発サーバー（ESM利用）
  - 高速なHMR（Hot Module Replacement）
  - Reactとの統合が容易
  - PWA対応プラグイン充実

- **プラグイン**:
  - `@vitejs/plugin-react`: React Fast Refresh
  - `vite-plugin-pwa`: PWA対応（将来フェーズ）

### 2.2 UIライブラリ

#### Material-UI (MUI) または shadcn/ui

**推奨: shadcn/ui**

- **選定理由**:
  - コンポーネントをプロジェクトにコピー（依存関係が少ない）
  - Tailwind CSSベース
  - カスタマイズ容易
  - TypeScript完全対応

**代替: Material-UI (MUI)**

- **選定理由**:
  - Googleのマテリアルデザイン準拠
  - 豊富なコンポーネント
  - アクセシビリティ対応

### 2.3 状態管理

#### React Context API または Zustand

**MVPフェーズ: React Context API**

- **選定理由**:
  - React標準機能
  - シンプルなグローバル状態管理
  - 追加ライブラリ不要

**将来フェーズ: Zustand**

- **選定理由**:
  - Redux より軽量・シンプル
  - TypeScript サポート充実
  - DevTools対応

### 2.4 HTTPクライアント

#### Axios

- **選定理由**:
  - インターセプター機能（JWT自動付与）
  - レスポンス/リクエスト変換
  - エラーハンドリング統一

**Axios設定例**:

```typescript
import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000,
});

// リクエストインターセプター（JWT付与）
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
```

### 2.5 ルーティング

#### React Router 6.x

- **選定理由**:
  - React標準のルーティングライブラリ
  - ネストルート対応
  - データローディング統合

### 2.6 フォーム管理

#### React Hook Form

- **選定理由**:
  - 高パフォーマンス（再レンダリング最小化）
  - TypeScript完全対応
  - バリデーション統合容易（Zod等）

**使用例**:

```typescript
import { useForm } from 'react-hook-form';

interface LoginForm {
  username: string;
  password: string;
}

const LoginPage = () => {
  const { register, handleSubmit, formState: { errors } } = useForm<LoginForm>();

  const onSubmit = (data: LoginForm) => {
    // API呼び出し
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input {...register('username', { required: true })} />
      {errors.username && <span>Username is required</span>}
      <button type="submit">Login</button>
    </form>
  );
};
```

### 2.7 PWA対応（将来フェーズ）

#### vite-plugin-pwa

- **機能**:
  - Service Worker自動生成
  - オフラインキャッシュ
  - インストール可能（Add to Home Screen）
  - プッシュ通知

---

## 3. データベース

### 3.1 本番環境: PostgreSQL

#### PostgreSQL 15+

- **選定理由**:
  - オープンソース、高性能
  - ACID準拠、トランザクション信頼性
  - JSON/JSONB型サポート（スキーマレス拡張）
  - 豊富な拡張機能

- **主要機能**:
  - MVCC（Multi-Version Concurrency Control）
  - 全文検索
  - パーティショニング
  - レプリケーション

### 3.2 開発環境: H2 Database

- **選定理由**:
  - インメモリDB（高速）
  - Spring Boot標準サポート
  - PostgreSQL互換モード

- **設定例** (`application-dev.yml`):

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:hidearea_dev
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
```

### 3.3 マイグレーション

#### Flyway または Liquibase

**推奨: Flyway**

- **選定理由**:
  - シンプルなSQL/Java ベースマイグレーション
  - バージョン管理
  - ロールバック対応（有償版）

**マイグレーションファイル例** (`V1__init_schema.sql`):

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 3.4 キャッシュ（将来対応）

#### Redis

- **用途**:
  - セッションストア（将来のスケールアウト時）
  - API応答キャッシュ
  - ランキング、カウンター
  - Pub/Sub（リアルタイム通信）

- **プラグイン方式**:
  - Spring Profilesで切り替え
  - `CachePlugin` インターフェース実装（将来）

---

## 4. インフラ・DevOps

### 4.1 コンテナ化

#### Docker

- **Dockerfile (Backend)**:

```dockerfile
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src
RUN ./gradlew build -x test

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- **Dockerfile (Frontend)**:

```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
```

### 4.2 オーケストレーション

#### Docker Compose

- **開発環境**: ホットリロード対応
- **本番環境**: Nginx + リバースプロキシ

詳細は [開発環境ガイド](../development/ENVIRONMENT_SETUP.md) を参照。

### 4.3 リバースプロキシ

#### Nginx

- **用途**:
  - 静的ファイル配信（フロントエンド）
  - リバースプロキシ（バックエンドAPI）
  - HTTPS/SSL終端
  - ロードバランシング（将来）

### 4.4 ログ管理

#### Logback (SLF4J)

- **形式**: JSON形式ログ
- **レベル**: TRACE, DEBUG, INFO, WARN, ERROR
- **出力先**: コンソール、ファイル

### 4.5 監視（将来フェーズ）

#### Prometheus + Grafana

- **Prometheus**: メトリクス収集
- **Grafana**: ダッシュボード可視化
- **Spring Boot Actuator**: メトリクスエンドポイント

---

## 5. 開発ツール

### 5.1 IDE

| 用途 | ツール | バージョン |
|-----|-------|-----------|
| バックエンド開発 | IntelliJ IDEA Ultimate | 2024.3+ |
| フロントエンド開発 | Visual Studio Code | 1.85+ |

### 5.2 コード品質ツール

#### バックエンド

| ツール | 用途 |
|-------|------|
| SonarLint | 静的解析（IntelliJ IDEA プラグイン） |
| Google Java Format | コードフォーマッター |
| Checkstyle | コーディング規約チェック |

#### フロントエンド

| ツール | 用途 |
|-------|------|
| ESLint | 静的解析（Airbnb Style Guide） |
| Prettier | コードフォーマッター |
| TypeScript Compiler | 型チェック |

### 5.3 テストツール

#### バックエンド

| ツール | 用途 |
|-------|------|
| JUnit 5 | 単体テスト |
| Mockito | モックフレームワーク |
| Spring Boot Test | 統合テスト |
| Testcontainers | DBテストコンテナ |
| REST Assured | APIテスト |
| JaCoCo | カバレッジレポート |

#### フロントエンド

| ツール | 用途 |
|-------|------|
| Vitest | 単体テスト |
| React Testing Library | コンポーネントテスト |
| Playwright | E2Eテスト |

### 5.4 その他ツール

| カテゴリ | ツール |
|---------|-------|
| APIテスト | Postman / Bruno |
| DBクライアント | DBeaver / TablePlus |
| Git GUI | GitKraken / Sourcetree |
| コンテナ管理 | Docker Desktop |

---

## 6. 決済プラットフォーム（将来対応）

### 6.1 Stripe

#### 概要

- **対象市場**: グローバル
- **対応決済**: クレジットカード、デビットカード、Apple Pay、Google Pay

#### 主要機能

| 機能 | 説明 |
|-----|------|
| Stripe Checkout | ホスト型決済ページ |
| Stripe Billing | サブスクリプション管理 |
| Stripe Connect | マーケットプレイス対応 |
| Webhooks | イベント通知 |

#### 統合方法

- **バックエンド**: Stripe Java SDK
- **フロントエンド**: Stripe.js / Stripe Elements

### 6.2 fincode

#### 概要

- **対象市場**: 日本
- **対応決済**: クレジットカード、コンビニ決済、銀行振込、キャリア決済

#### 主要機能

| 機能 | 説明 |
|-----|------|
| カード決済 | 3Dセキュア対応 |
| 継続課金 | サブスクリプション |
| セキュリティ | PCI DSS準拠 |

#### 統合方法

- **REST API**: JSON形式
- **Webhook**: イベント通知

---

## まとめ

本技術スタックは、以下の原則に基づいて選定されています：

1. **モダン**: 最新のLTS版、アクティブなコミュニティ
2. **拡張性**: プラグイン方式、マイクロサービス化への対応
3. **開発効率**: 生成AI活用、豊富なエコシステム
4. **保守性**: 型安全性、テスト容易性、ドキュメント充実

MVPフェーズでは、シンプルな構成（REST API、Spring Profiles）から始め、将来フェーズで段階的に拡張（WebSocket、Redis、決済統合）していきます。
