# 開発環境セットアップガイド

**最終更新日**: 2025-11-04

---

## 目次

1. [前提条件](#1-前提条件)
2. [環境構築手順](#2-環境構築手順)
3. [Docker Compose構成](#3-docker-compose構成)
4. [起動スクリプト](#4-起動スクリプト)
5. [トラブルシューティング](#5-トラブルシューティング)

---

## 1. 前提条件

### 1.1 必要なソフトウェア

以下のソフトウェアをインストールしてください：

| ソフトウェア | バージョン | インストール方法 |
|------------|-----------|----------------|
| Java | 25 (LTS) | [OpenJDK](https://jdk.java.net/25/) |
| Gradle | 8.x | 自動（Gradle Wrapper使用） |
| Node.js | 20.x LTS | [nodejs.org](https://nodejs.org/) |
| Docker Desktop | 最新版 | [docker.com](https://www.docker.com/) |
| Git | 最新版 | [git-scm.com](https://git-scm.com/) |

### 1.2 推奨開発ツール

| ツール | 用途 |
|-------|------|
| IntelliJ IDEA Ultimate | バックエンド開発 |
| Visual Studio Code | フロントエンド開発 |
| DBeaver / TablePlus | PostgreSQLクライアント |
| Postman / Bruno | API動作確認 |

---

## 2. 環境構築手順

### 2.1 リポジトリのクローン

```bash
git clone https://github.com/yourusername/hidearea.git
cd hidearea
```

### 2.2 環境変数設定

プロジェクトルートに `.env` ファイルを作成：

```bash
# データベース設定
DB_USER=hidearea_user
DB_PASSWORD=hidearea_pass
DB_NAME=hidearea_dev

# JWT設定
JWT_SECRET=your-super-secret-key-change-this-in-production-min-32-chars

# フロントエンド設定
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

**重要**: `.env` ファイルは `.gitignore` に含まれています。本番環境では別途設定してください。

### 2.3 バックエンドセットアップ

#### 2.3.1 Spring Bootプロジェクト初期化

Spring Initializrを使用してプロジェクトを生成：

```bash
cd backend
```

または、既存のプロジェクトの場合：

```bash
# 依存関係のダウンロード
./gradlew build -x test
```

#### 2.3.2 application.yml 設定

`backend/src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: hidearea-core

  profiles:
    active: dev

  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true

  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 8080
  error:
    include-message: always
    include-binding-errors: always

logging:
  level:
    com.hidearea.core: DEBUG
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
```

`backend/src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/${DB_NAME:hidearea_dev}
    username: ${DB_USER:hidearea_user}
    password: ${DB_PASSWORD:hidearea_pass}
    driver-class-name: org.postgresql.Driver

  h2:
    console:
      enabled: false

  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    show-sql: true

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000  # 24時間（ミリ秒）
```

### 2.4 フロントエンドセットアップ

```bash
cd frontend

# 依存関係のインストール
npm install

# または
pnpm install
```

#### 2.4.1 vite.config.ts 設定

```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

### 2.5 データベースセットアップ

#### 2.5.1 PostgreSQLコンテナ起動

```bash
docker run -d \
  --name hidearea-postgres \
  -e POSTGRES_DB=hidearea_dev \
  -e POSTGRES_USER=hidearea_user \
  -e POSTGRES_PASSWORD=hidearea_pass \
  -p 5432:5432 \
  postgres:15
```

#### 2.5.2 マイグレーション実行

Spring Bootアプリケーション起動時にFlywayが自動実行されます。

手動実行する場合：

```bash
cd backend
./gradlew flywayMigrate
```

---

## 3. Docker Compose構成

### 3.1 開発環境用 Docker Compose

`docker/docker-compose.dev.yml`:

```yaml
version: '3.8'

services:
  # PostgreSQL Database
  postgres:
    image: postgres:15
    container_name: hidearea-postgres-dev
    environment:
      POSTGRES_DB: ${DB_NAME:-hidearea_dev}
      POSTGRES_USER: ${DB_USER:-hidearea_user}
      POSTGRES_PASSWORD: ${DB_PASSWORD:-hidearea_pass}
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - hidearea-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER:-hidearea_user}"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Spring Boot Backend
  backend:
    build:
      context: ../backend
      dockerfile: Dockerfile.dev
    container_name: hidearea-backend-dev
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/${DB_NAME:-hidearea_dev}
      SPRING_DATASOURCE_USERNAME: ${DB_USER:-hidearea_user}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD:-hidearea_pass}
      JWT_SECRET: ${JWT_SECRET}
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    volumes:
      - ../backend:/app
      - gradle-cache:/root/.gradle
    networks:
      - hidearea-network

  # React Frontend (Development Server)
  frontend:
    build:
      context: ../frontend
      dockerfile: Dockerfile.dev
    container_name: hidearea-frontend-dev
    environment:
      VITE_API_BASE_URL: ${VITE_API_BASE_URL:-http://localhost:8080/api/v1}
    ports:
      - "5173:5173"
    volumes:
      - ../frontend:/app
      - /app/node_modules
    networks:
      - hidearea-network

volumes:
  postgres-data:
  gradle-cache:

networks:
  hidearea-network:
    driver: bridge
```

### 3.2 Dockerfile（開発環境）

#### backend/Dockerfile.dev

```dockerfile
FROM eclipse-temurin:25-jdk-alpine

WORKDIR /app

# Gradle Wrapper のコピー
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

# 依存関係のダウンロード（キャッシュ活用）
RUN ./gradlew dependencies --no-daemon

# ソースコードのコピー
COPY src ./src

# アプリケーション起動
CMD ["./gradlew", "bootRun", "--no-daemon"]

EXPOSE 8080
```

#### frontend/Dockerfile.dev

```dockerfile
FROM node:20-alpine

WORKDIR /app

# package.json のコピー（キャッシュ活用）
COPY package*.json ./

# 依存関係のインストール
RUN npm ci

# ソースコードのコピー
COPY . .

# 開発サーバー起動
CMD ["npm", "run", "dev", "--", "--host"]

EXPOSE 5173
```

### 3.3 本番環境用 Docker Compose

`docker/docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    container_name: hidearea-postgres-prod
    environment:
      POSTGRES_DB: ${DB_NAME}
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - hidearea-network
    restart: unless-stopped

  backend:
    build:
      context: ../backend
      dockerfile: Dockerfile
    container_name: hidearea-backend-prod
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/${DB_NAME}
      SPRING_DATASOURCE_USERNAME: ${DB_USER}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
    depends_on:
      - postgres
    networks:
      - hidearea-network
    restart: unless-stopped

  frontend:
    build:
      context: ../frontend
      dockerfile: Dockerfile
    container_name: hidearea-frontend-prod
    networks:
      - hidearea-network
    restart: unless-stopped

  nginx:
    image: nginx:alpine
    container_name: hidearea-nginx-prod
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/ssl:/etc/nginx/ssl:ro
    depends_on:
      - backend
      - frontend
    networks:
      - hidearea-network
    restart: unless-stopped

volumes:
  postgres-data:

networks:
  hidearea-network:
    driver: bridge
```

### 3.4 Nginx設定

`docker/nginx/nginx.conf`:

```nginx
upstream backend {
    server backend:8080;
}

upstream frontend {
    server frontend:80;
}

server {
    listen 80;
    server_name localhost;

    # フロントエンド（静的ファイル）
    location / {
        proxy_pass http://frontend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # バックエンドAPI
    location /api/ {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # CORS設定（必要に応じて）
        add_header 'Access-Control-Allow-Origin' '*' always;
        add_header 'Access-Control-Allow-Methods' 'GET, POST, PUT, DELETE, OPTIONS' always;
        add_header 'Access-Control-Allow-Headers' 'Authorization, Content-Type' always;

        if ($request_method = 'OPTIONS') {
            return 204;
        }
    }

    # Swagger UI
    location /swagger-ui/ {
        proxy_pass http://backend;
        proxy_set_header Host $host;
    }
}
```

---

## 4. 起動スクリプト

### 4.1 開発環境起動

`scripts/start-dev.sh`:

```bash
#!/bin/bash

echo "🚀 Starting HideArea Development Environment..."

# .env ファイルの確認
if [ ! -f .env ]; then
    echo "⚠️  .env file not found. Creating from example..."
    cat > .env << EOF
DB_USER=hidearea_user
DB_PASSWORD=hidearea_pass
DB_NAME=hidearea_dev
JWT_SECRET=$(openssl rand -base64 32)
VITE_API_BASE_URL=http://localhost:8080/api/v1
EOF
    echo "✅ .env file created"
fi

# Docker Composeで起動
docker-compose -f docker/docker-compose.dev.yml up -d

echo ""
echo "✅ Development environment started!"
echo ""
echo "📍 Services:"
echo "   - Backend API:     http://localhost:8080"
echo "   - Frontend:        http://localhost:5173"
echo "   - PostgreSQL:      localhost:5432"
echo "   - Swagger UI:      http://localhost:8080/swagger-ui.html"
echo ""
echo "📝 Logs:"
echo "   docker-compose -f docker/docker-compose.dev.yml logs -f"
echo ""
echo "🛑 Stop:"
echo "   ./scripts/stop.sh"
```

### 4.2 ローカル起動（Docker不使用）

`scripts/start-local.sh`:

```bash
#!/bin/bash

echo "🚀 Starting HideArea Locally (without Docker)..."

# PostgreSQLが起動しているか確認
if ! pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
    echo "❌ PostgreSQL is not running. Please start PostgreSQL first."
    exit 1
fi

# バックエンド起動（バックグラウンド）
echo "📦 Starting Backend..."
cd backend
./gradlew bootRun > ../logs/backend.log 2>&1 &
BACKEND_PID=$!
cd ..

# フロントエンド起動（バックグラウンド）
echo "📦 Starting Frontend..."
cd frontend
npm run dev > ../logs/frontend.log 2>&1 &
FRONTEND_PID=$!
cd ..

echo ""
echo "✅ Services started!"
echo ""
echo "📍 Backend PID:  $BACKEND_PID"
echo "📍 Frontend PID: $FRONTEND_PID"
echo ""
echo "📝 Logs:"
echo "   tail -f logs/backend.log"
echo "   tail -f logs/frontend.log"
echo ""
echo "🛑 Stop:"
echo "   kill $BACKEND_PID $FRONTEND_PID"

# PIDをファイルに保存
mkdir -p .pids
echo $BACKEND_PID > .pids/backend.pid
echo $FRONTEND_PID > .pids/frontend.pid
```

### 4.3 停止スクリプト

`scripts/stop.sh`:

```bash
#!/bin/bash

echo "🛑 Stopping HideArea..."

# Docker Compose停止
if [ -f docker/docker-compose.dev.yml ]; then
    docker-compose -f docker/docker-compose.dev.yml down
fi

if [ -f docker/docker-compose.prod.yml ]; then
    docker-compose -f docker/docker-compose.prod.yml down
fi

# ローカルプロセス停止
if [ -d .pids ]; then
    if [ -f .pids/backend.pid ]; then
        kill $(cat .pids/backend.pid) 2>/dev/null
        rm .pids/backend.pid
    fi
    if [ -f .pids/frontend.pid ]; then
        kill $(cat .pids/frontend.pid) 2>/dev/null
        rm .pids/frontend.pid
    fi
    rmdir .pids 2>/dev/null
fi

echo "✅ Stopped!"
```

### 4.4 本番環境起動

`scripts/start-prod.sh`:

```bash
#!/bin/bash

echo "🚀 Starting HideArea Production Environment..."

# .env ファイルの確認
if [ ! -f .env ]; then
    echo "❌ Error: .env file not found!"
    echo "Please create .env file with production settings."
    exit 1
fi

# JWT_SECRETの確認
source .env
if [ -z "$JWT_SECRET" ] || [ ${#JWT_SECRET} -lt 32 ]; then
    echo "❌ Error: JWT_SECRET must be at least 32 characters!"
    exit 1
fi

# Docker Composeで起動
docker-compose -f docker/docker-compose.prod.yml up -d --build

echo ""
echo "✅ Production environment started!"
echo ""
echo "📍 Application: http://localhost"
echo ""
echo "📝 Logs:"
echo "   docker-compose -f docker/docker-compose.prod.yml logs -f"
```

### 4.5 スクリプトの実行権限付与

```bash
chmod +x scripts/*.sh
```

---

## 5. トラブルシューティング

### 5.1 よくある問題

#### 問題: Dockerコンテナが起動しない

**解決策**:

```bash
# Dockerサービスの確認
docker info

# コンテナのログ確認
docker-compose -f docker/docker-compose.dev.yml logs

# コンテナの再起動
docker-compose -f docker/docker-compose.dev.yml restart
```

#### 問題: PostgreSQLに接続できない

**解決策**:

```bash
# PostgreSQLの起動確認
docker ps | grep postgres

# PostgreSQLコンテナ内で接続確認
docker exec -it hidearea-postgres-dev psql -U hidearea_user -d hidearea_dev

# ポート競合の確認
lsof -i :5432
```

#### 問題: バックエンドが起動しない

**解決策**:

```bash
# Java バージョン確認
java -version  # Should be 25

# Gradleビルドエラー確認
cd backend
./gradlew clean build

# 依存関係の再取得
./gradlew build --refresh-dependencies
```

#### 問題: フロントエンドが起動しない

**解決策**:

```bash
# Node.js バージョン確認
node -v  # Should be 20.x

# node_modules削除と再インストール
cd frontend
rm -rf node_modules package-lock.json
npm install

# キャッシュクリア
npm cache clean --force
```

#### 問題: CORS エラーが発生する

**解決策**:

バックエンドの `WebConfig.java` で CORS 設定を確認：

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

### 5.2 ログの確認

#### Docker Compose環境

```bash
# 全サービスのログ
docker-compose -f docker/docker-compose.dev.yml logs -f

# 特定サービスのログ
docker-compose -f docker/docker-compose.dev.yml logs -f backend
docker-compose -f docker/docker-compose.dev.yml logs -f frontend
docker-compose -f docker/docker-compose.dev.yml logs -f postgres
```

#### ローカル環境

```bash
# バックエンドログ
tail -f logs/backend.log

# フロントエンドログ
tail -f logs/frontend.log
```

### 5.3 データベースのリセット

```bash
# 開発環境のデータベースをリセット
docker-compose -f docker/docker-compose.dev.yml down -v
docker-compose -f docker/docker-compose.dev.yml up -d postgres

# Flywayマイグレーションを再実行
cd backend
./gradlew flywayClean flywayMigrate
```

### 5.4 完全クリーンアップ

```bash
# すべてのコンテナ・ボリューム・イメージを削除
docker-compose -f docker/docker-compose.dev.yml down -v --rmi all

# ビルドキャッシュのクリア
cd backend
./gradlew clean

cd ../frontend
rm -rf node_modules dist

# 再ビルド
cd ..
./scripts/start-dev.sh
```

---

## まとめ

本ドキュメントでは、HideAreaの開発環境セットアップ手順を詳細に説明しました。

**開発環境の起動**:
```bash
./scripts/start-dev.sh
```

**アクセス先**:
- フロントエンド: http://localhost:5173
- バックエンドAPI: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- PostgreSQL: localhost:5432

**次のステップ**:
- [開発ガイドライン](DEVELOPMENT_GUIDE.md) を参照
- [API仕様書](../api/API_SPECIFICATION.md) でエンドポイントを確認
- コーディングを開始！
