# HideArea

**マイクロカーネルアーキテクチャ マルチデバイス対応Webサービスプラットフォーム**

---

## 概要

HideAreaは、マイクロカーネルアーキテクチャを採用した拡張性の高いWebサービスプラットフォームです。  
ユーザー管理、プロフィール管理を中心としたコア機能から始まり、コミュニティ、プロジェクト管理、決済プラットフォームなど、多様なサービスを段階的に展開していきます。

### 主な特徴

- **マイクロカーネルアーキテクチャ**: プラグイン方式による柔軟な機能拡張
- **モダンな技術スタック**: Java 25、Spring Boot、React、TypeScript
- **マルチデバイス対応**: レスポンシブデザイン、将来的にPWA対応
- **スケーラブル**: 将来的なマイクロサービス化を見据えた設計

---

## 技術スタック

### バックエンド
- **言語**: Java 25
- **フレームワーク**: Spring Boot 3.x
- **データベース**: PostgreSQL 15+
- **認証**: Spring Security + JWT
- **ビルドツール**: Gradle 8.x (Kotlin DSL)

### フロントエンド
- **言語**: TypeScript 5.x
- **フレームワーク**: React 18.x
- **ビルドツール**: Vite 5.x
- **UIライブラリ**: Material-UI / shadcn/ui
- **状態管理**: React Context API / Zustand

### インフラ
- **コンテナ**: Docker
- **オーケストレーション**: Docker Compose
- **リバースプロキシ**: Nginx

---

## クイックスタート

### 前提条件

- Java 25
- Node.js 20.x LTS
- Docker Desktop
- Git

### 1. リポジトリのクローン

```bash
git clone https://github.com/yourusername/hidearea.git
cd hidearea
```

### 2. 環境変数の設定

```bash
cp .env.example .env
# .envファイルを編集して必要な値を設定
```

### 3. 開発環境の起動

```bash
./scripts/start-dev.sh
```

### 4. アクセス

- **フロントエンド**: http://localhost:5173
- **バックエンドAPI**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html

---

## プロジェクト構成

```
hidearea/
├── backend/                # Spring Bootアプリケーション
├── frontend/               # React + Vite アプリケーション
├── docker/                 # Docker Compose設定
├── docs/                   # ドキュメント
│   ├── architecture/       # アーキテクチャ設計書
│   ├── api/                # API仕様書
│   └── development/        # 開発ガイド
├── scripts/                # 起動スクリプト
├── PROJECT_PLAN.md         # プロジェクト計画書
└── README.md               # 本ドキュメント
```

---

## ドキュメント

### プロジェクト計画
- [プロジェクト計画書](PROJECT_PLAN.md) - プロジェクトの全体像

### アーキテクチャ
- [技術スタック詳細](docs/architecture/TECH_STACK.md)
- [パッケージ設計書](docs/architecture/PACKAGE_DESIGN.md) - APIバージョニング実装を含む
- [データ設計書](docs/architecture/DATA_DESIGN.md)

### API
- [API仕様書](docs/api/API_SPECIFICATION.md)

### 開発
- [開発環境セットアップ](docs/development/ENVIRONMENT_SETUP.md)

---

## MVPフェーズ（第1フェーズ）

現在のフェーズでは、以下の機能を実装します：

### ユーザー管理
- ユーザー登録・ログイン
- JWT認証・認可
- パスワード変更
- アカウント管理

### プロフィール管理
- プロフィール作成・編集・削除
- ユーザーとプロフィールの多対多関係
- プロフィールメンバー管理
- 役割管理（OWNER, MEMBER）

---

## 将来の展開

### 第2フェーズ: コミュニティ管理
- リアルタイムチャット（WebSocket）
- 通知機能（SSE）
- 掲示板・スレッド機能

### 第3フェーズ: プロジェクト管理
- タスク管理
- カンバンボード
- ガントチャート

### 第4フェーズ: 組織管理
- 組織階層管理
- 部署・チーム管理
- 組織レベルの権限管理

### 第5フェーズ: コラボレーション
- ファイル共有
- リアルタイム共同編集
- カレンダー統合

### 第6フェーズ: 決済プラットフォーム
- Stripe / fincode統合
- サブスクリプション管理
- 請求書発行

### 第7フェーズ: PWA対応
- オフライン対応
- インストール可能
- プッシュ通知

---

## 開発

### 開発環境での起動

```bash
# Docker Composeを使用
./scripts/start-dev.sh

# ローカル環境（Docker不使用）
./scripts/start-local.sh
```

### テスト実行

```bash
# バックエンドテスト
cd backend
./gradlew test

# フロントエンドテスト
cd frontend
npm test
```

### ビルド

```bash
# バックエンドビルド
cd backend
./gradlew build

# フロントエンドビルド
cd frontend
npm run build
```

---

## コントリビューション

本プロジェクトは個人開発プロジェクトですが、フィードバックや提案は歓迎します。

---

## ライセンス

MIT License

---

## 作者

- **開発者**: [Your Name]
- **連絡先**: [your.email@example.com]
- **GitHub**: [https://github.com/yourusername](https://github.com/yourusername)

---

## 謝辞

本プロジェクトは、生成AI（Claude）を活用して開発されています。
