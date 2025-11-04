# API仕様書

**最終更新日**: 2025-11-04
**APIバージョン**: v1

---

## 目次

1. [概要](#1-概要)
2. [エンドポイント一覧](#2-エンドポイント一覧)
3. [認証API](#3-認証api)
4. [ユーザーAPI](#4-ユーザーapi)
5. [プロフィールAPI](#5-プロフィールapi)
6. [エラーレスポンス](#6-エラーレスポンス)
7. [ステータスコード](#7-ステータスコード)

---

## 1. 概要

### 1.1 ベースURL

- **開発環境**: `http://localhost:8080/api/v1`
- **本番環境**: `https://api.hidearea.com/v1`

### 1.2 認証方式

**JWT (JSON Web Token) Bearer認証**

リクエストヘッダーに以下を含める：

```
Authorization: Bearer {token}
```

### 1.3 リクエスト・レスポンス形式

- **Content-Type**: `application/json`
- **文字コード**: UTF-8

### 1.4 共通レスポンス形式

#### 成功レスポンス

```json
{
  "status": "success",
  "data": { ... },
  "message": null
}
```

#### エラーレスポンス

```json
{
  "status": "error",
  "error": {
    "code": "ERROR_CODE",
    "message": "エラーメッセージ",
    "details": null
  },
  "timestamp": "2025-11-04T10:00:00Z"
}
```

---

## 2. エンドポイント一覧

### 2.1 全エンドポイント一覧表

| カテゴリ | メソッド | エンドポイント | 認証 | 説明 |
|---------|---------|--------------|------|------|
| **認証** | POST | `/auth/register` | - | ユーザー登録 |
| **認証** | POST | `/auth/login` | - | ログイン（JWT取得） |
| **認証** | POST | `/auth/logout` | ✓ | ログアウト |
| **認証** | GET | `/auth/me` | ✓ | 現在のユーザー情報取得 |
| **ユーザー** | GET | `/users/{id}` | ✓ | ユーザー詳細取得 |
| **ユーザー** | PUT | `/users/{id}` | ✓ | ユーザー情報更新 |
| **ユーザー** | DELETE | `/users/{id}` | ✓ | ユーザー削除 |
| **ユーザー** | PUT | `/users/{id}/password` | ✓ | パスワード変更 |
| **ユーザー** | GET | `/users/{id}/profiles` | ✓ | ユーザーが所属するプロフィール一覧 |
| **プロフィール** | POST | `/profiles` | ✓ | プロフィール作成（ルート） |
| **プロフィール** | GET | `/profiles` | ✓ | プロフィール一覧取得 |
| **プロフィール** | GET | `/profiles/{id}` | ✓ | プロフィール詳細取得 |
| **プロフィール** | PUT | `/profiles/{id}` | ✓ | プロフィール更新 |
| **プロフィール** | DELETE | `/profiles/{id}` | ✓ | プロフィール削除 |
| **プロフィール階層** | POST | `/profiles/{id}/children` | ✓ | 子プロフィール作成 |
| **プロフィール階層** | GET | `/profiles/{id}/children` | ✓ | 直接の子プロフィール一覧取得 |
| **プロフィール階層** | GET | `/profiles/{id}/tree` | ✓ | プロフィール階層ツリー取得 |
| **プロフィール階層** | GET | `/profiles/{id}/parent` | ✓ | 親プロフィール取得 |
| **プロフィール階層** | GET | `/profiles/{id}/root` | ✓ | ルートプロフィール取得 |
| **プロフィール階層** | PUT | `/profiles/{id}/parent` | ✓ | 親プロフィール変更（付け替え） |
| **プロフィール階層** | DELETE | `/profiles/{id}/parent` | ✓ | 親子関係削除（ルート化） |
| **プロフィールメンバー** | POST | `/profiles/{id}/members` | ✓ | メンバー追加 |
| **プロフィールメンバー** | GET | `/profiles/{id}/members` | ✓ | メンバー一覧取得 |
| **プロフィールメンバー** | DELETE | `/profiles/{id}/members/{userId}` | ✓ | メンバー削除 |
| **プロフィールメンバー** | PUT | `/profiles/{id}/members/{userId}/role` | ✓ | メンバーの役割変更 |

### 2.2 カテゴリ別エンドポイント数

| カテゴリ | エンドポイント数 | 備考 |
|---------|----------------|------|
| 認証 | 4 | 登録、ログイン、ログアウト、現在のユーザー情報 |
| ユーザー | 5 | CRUD + パスワード変更 + 所属プロフィール一覧 |
| プロフィール | 5 | CRUD + 一覧取得 |
| プロフィール階層 | 7 | 子作成、子一覧、ツリー、親取得、ルート取得、親変更、親削除 |
| プロフィールメンバー | 4 | メンバー追加、一覧、削除、役割変更 |
| **合計** | **25** | MVPフェーズ |

### 2.3 認証要否の内訳

| 認証 | エンドポイント数 | 割合 |
|------|----------------|------|
| 認証不要 | 2 | 8.0% (register, login) |
| 認証必須 | 23 | 92.0% |
| **合計** | **25** | 100% |

### 2.4 HTTPメソッド別の内訳

| メソッド | エンドポイント数 | 用途 |
|---------|----------------|------|
| GET | 10 | リソースの取得 |
| POST | 5 | リソースの作成 |
| PUT | 6 | リソースの更新 |
| DELETE | 4 | リソースの削除 |
| **合計** | **25** | |

### 2.5 エンドポイント設計の確認ポイント

#### ✅ 実装済み機能

- [x] ユーザー登録・認証
- [x] プロフィールCRUD
- [x] **プロフィール階層管理**（案C対応）
  - [x] 子プロフィール作成
  - [x] 階層ツリー取得（再帰クエリ）
  - [x] 親・ルート取得
- [x] プロフィールメンバー管理
  - [x] ユーザー・プロフィール関連（多対多）
  - [x] 役割管理（OWNER, MEMBER）

#### 🔜 将来追加予定（MVPフェーズ外）

- [ ] プロフィール検索（キーワード、タイプ、公開設定）
- [ ] ページネーション対応（一覧取得API）
- [ ] ソート・フィルタリング
- [ ] プロフィール画像アップロード
- [ ] リフレッシュトークン（認証）
- [ ] パスワードリセット（メール送信）
- [ ] ユーザー検索
- [ ] 管理者API（ADMIN権限）

#### ⚠️ 不足している可能性のあるエンドポイント

現在のMVP仕様では以下は対象外ですが、必要に応じて追加可能：

1. **プロフィール検索**: `GET /profiles/search?q={keyword}`
2. **公開プロフィール取得**: `GET /public/profiles/{id}` (認証不要)
3. **プロフィールタイプフィルタ**: `GET /profiles?type={PERSONAL|BUSINESS}`
4. **階層の深さ取得**: `GET /profiles/{id}/depth`
5. **循環参照チェック**: `GET /profiles/{id}/can-add-child/{childId}`

---

## 3. 認証API

### 3.1 ユーザー登録

新規ユーザーアカウントを作成します。

**エンドポイント**: `POST /auth/register`

**認証**: 不要

#### リクエスト

```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|---|------|------|
| username | string | ✓ | ユーザー名（3-50文字、英数字とアンダースコアのみ） |
| email | string | ✓ | メールアドレス（RFC 5322準拠） |
| password | string | ✓ | パスワード（8文字以上、英大小文字・数字を含む） |

#### レスポンス (201 Created)

```json
{
  "status": "success",
  "data": {
    "user": {
      "id": 1,
      "username": "johndoe",
      "email": "john@example.com",
      "role": "USER",
      "enabled": true,
      "createdAt": "2025-11-04T10:00:00Z"
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "message": "User registered successfully"
}
```

#### エラーレスポンス

| ステータス | コード | 説明 |
|-----------|--------|------|
| 400 | VALIDATION_ERROR | 入力値が不正 |
| 409 | USERNAME_ALREADY_EXISTS | ユーザー名が既に使用されている |
| 409 | EMAIL_ALREADY_EXISTS | メールアドレスが既に使用されている |

---

### 3.2 ログイン

ユーザー認証を行い、JWTトークンを発行します。

**エンドポイント**: `POST /auth/login`

**認証**: 不要

#### リクエスト

```json
{
  "usernameOrEmail": "johndoe",
  "password": "SecurePass123!"
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|---|------|------|
| usernameOrEmail | string | ✓ | ユーザー名またはメールアドレス |
| password | string | ✓ | パスワード |

#### レスポンス (200 OK)

```json
{
  "status": "success",
  "data": {
    "user": {
      "id": 1,
      "username": "johndoe",
      "email": "john@example.com",
      "role": "USER",
      "enabled": true
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400
  },
  "message": "Login successful"
}
```

| フィールド | 型 | 説明 |
|-----------|---|------|
| token | string | JWTアクセストークン |
| expiresIn | number | トークン有効期限（秒） |

#### エラーレスポンス

| ステータス | コード | 説明 |
|-----------|--------|------|
| 401 | INVALID_CREDENTIALS | ユーザー名/メールアドレスまたはパスワードが不正 |
| 403 | ACCOUNT_DISABLED | アカウントが無効化されている |

---

### 3.3 ログアウト

現在のセッションを終了します（クライアント側でトークン破棄）。

**エンドポイント**: `POST /auth/logout`

**認証**: 必要

#### リクエスト

リクエストボディなし

#### レスポンス (200 OK)

```json
{
  "status": "success",
  "data": null,
  "message": "Logout successful"
}
```

---

### 3.4 トークンリフレッシュ（将来対応）

アクセストークンを更新します。

**エンドポイント**: `POST /auth/refresh`

**認証**: 必要（リフレッシュトークン）

---

## 4. ユーザーAPI

### 4.1 自分の情報取得

現在ログイン中のユーザー情報を取得します。

**エンドポイント**: `GET /users/me`

**認証**: 必要

#### レスポンス (200 OK)

```json
{
  "status": "success",
  "data": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "role": "USER",
    "enabled": true,
    "createdAt": "2025-11-04T10:00:00Z",
    "updatedAt": "2025-11-04T12:00:00Z"
  }
}
```

---

### 4.2 自分の情報更新

現在ログイン中のユーザー情報を更新します。

**エンドポイント**: `PUT /users/me`

**認証**: 必要

#### リクエスト

```json
{
  "email": "newemail@example.com"
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|---|------|------|
| email | string | - | 新しいメールアドレス |

#### レスポンス (200 OK)

```json
{
  "status": "success",
  "data": {
    "id": 1,
    "username": "johndoe",
    "email": "newemail@example.com",
    "role": "USER",
    "enabled": true,
    "updatedAt": "2025-11-04T13:00:00Z"
  },
  "message": "User updated successfully"
}
```

#### エラーレスポンス

| ステータス | コード | 説明 |
|-----------|--------|------|
| 409 | EMAIL_ALREADY_EXISTS | メールアドレスが既に使用されている |

---

### 4.3 パスワード変更

現在ログイン中のユーザーのパスワードを変更します。

**エンドポイント**: `PUT /users/me/password`

**認証**: 必要

#### リクエスト

```json
{
  "currentPassword": "SecurePass123!",
  "newPassword": "NewSecurePass456!"
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|---|------|------|
| currentPassword | string | ✓ | 現在のパスワード |
| newPassword | string | ✓ | 新しいパスワード（8文字以上） |

#### レスポンス (200 OK)

```json
{
  "status": "success",
  "data": null,
  "message": "Password updated successfully"
}
```

#### エラーレスポンス

| ステータス | コード | 説明 |
|-----------|--------|------|
| 401 | INVALID_PASSWORD | 現在のパスワードが不正 |
| 400 | WEAK_PASSWORD | 新しいパスワードが弱い |

---

### 4.4 アカウント削除

現在ログイン中のユーザーアカウントを削除します（論理削除）。

**エンドポイント**: `DELETE /users/me`

**認証**: 必要

#### リクエスト

```json
{
  "password": "SecurePass123!"
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|---|------|------|
| password | string | ✓ | 確認用パスワード |

#### レスポンス (200 OK)

```json
{
  "status": "success",
  "data": null,
  "message": "Account deleted successfully"
}
```

#### エラーレスポンス

| ステータス | コード | 説明 |
|-----------|--------|------|
| 401 | INVALID_PASSWORD | パスワードが不正 |

---

## 5. プロフィールAPI

### 5.1 プロフィール作成

新しいプロフィールを作成し、作成者を自動的にOWNERとして関連付けます。

**エンドポイント**: `POST /profiles`

**認証**: 必要

#### リクエスト

```json
{
  "profileName": "My Personal Profile",
  "displayName": "John Doe",
  "bio": "Software Engineer passionate about AI",
  "avatarUrl": "https://example.com/avatar.jpg",
  "profileType": "PERSONAL",
  "isPublic": true
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|---|------|------|
| profileName | string | ✓ | プロフィール名（1-100文字） |
| displayName | string | - | 表示名 |
| bio | string | - | 自己紹介 |
| avatarUrl | string | - | アバター画像URL |
| profileType | string | ✓ | プロフィールタイプ（PERSONAL, BUSINESS） |
| isPublic | boolean | - | 公開フラグ（デフォルト: true） |

#### レスポンス (201 Created)

```json
{
  "status": "success",
  "data": {
    "id": 1,
    "profileName": "My Personal Profile",
    "displayName": "John Doe",
    "bio": "Software Engineer passionate about AI",
    "avatarUrl": "https://example.com/avatar.jpg",
    "profileType": "PERSONAL",
    "isPublic": true,
    "createdAt": "2025-11-04T10:00:00Z",
    "updatedAt": "2025-11-04T10:00:00Z",
    "role": "OWNER"
  },
  "message": "Profile created successfully"
}
```

---

### 5.2 自分のプロフィール一覧取得

現在ログイン中のユーザーが所属する全プロフィールを取得します。

**エンドポイント**: `GET /profiles`

**認証**: 必要

#### クエリパラメータ

| パラメータ | 型 | 必須 | 説明 |
|-----------|---|------|------|
| profileType | string | - | フィルター: PERSONAL, BUSINESS |
| page | number | - | ページ番号（デフォルト: 0） |
| size | number | - | ページサイズ（デフォルト: 20） |

#### レスポンス (200 OK)

```json
{
  "status": "success",
  "data": {
    "profiles": [
      {
        "id": 1,
        "profileName": "My Personal Profile",
        "displayName": "John Doe",
        "profileType": "PERSONAL",
        "isPublic": true,
        "role": "OWNER",
        "joinedAt": "2025-11-04T10:00:00Z"
      },
      {
        "id": 2,
        "profileName": "ACME Corporation",
        "displayName": "ACME Corp",
        "profileType": "BUSINESS",
        "isPublic": true,
        "role": "MEMBER",
        "joinedAt": "2025-11-05T09:00:00Z"
      }
    ],
    "pagination": {
      "page": 0,
      "size": 20,
      "totalElements": 2,
      "totalPages": 1
    }
  }
}
```

---

### 5.3 プロフィール詳細取得

指定されたプロフィールの詳細情報を取得します。

**エンドポイント**: `GET /profiles/{id}`

**認証**: 必要

#### パスパラメータ

| パラメータ | 型 | 説明 |
|-----------|---|------|
| id | number | プロフィールID |

#### レスポンス (200 OK)

```json
{
  "status": "success",
  "data": {
    "id": 1,
    "profileName": "My Personal Profile",
    "displayName": "John Doe",
    "bio": "Software Engineer passionate about AI",
    "avatarUrl": "https://example.com/avatar.jpg",
    "profileType": "PERSONAL",
    "isPublic": true,
    "createdAt": "2025-11-04T10:00:00Z",
    "updatedAt": "2025-11-04T12:00:00Z",
    "memberCount": 1,
    "role": "OWNER"
  }
}
```

#### エラーレスポンス

| ステータス | コード | 説明 |
|-----------|--------|------|
| 404 | PROFILE_NOT_FOUND | プロフィールが見つからない |
| 403 | ACCESS_DENIED | 非公開プロフィールへのアクセス権限なし |

---

### 5.4 プロフィール更新

指定されたプロフィールの情報を更新します（OWNER権限必要）。

**エンドポイント**: `PUT /profiles/{id}`

**認証**: 必要（OWNER権限）

#### パスパラメータ

| パラメータ | 型 | 説明 |
|-----------|---|------|
| id | number | プロフィールID |

#### リクエスト

```json
{
  "displayName": "John Doe (Updated)",
  "bio": "Updated bio",
  "isPublic": false
}
```

#### レスポンス (200 OK)

```json
{
  "status": "success",
  "data": {
    "id": 1,
    "profileName": "My Personal Profile",
    "displayName": "John Doe (Updated)",
    "bio": "Updated bio",
    "profileType": "PERSONAL",
    "isPublic": false,
    "updatedAt": "2025-11-04T13:00:00Z"
  },
  "message": "Profile updated successfully"
}
```

#### エラーレスポンス

| ステータス | コード | 説明 |
|-----------|--------|------|
| 404 | PROFILE_NOT_FOUND | プロフィールが見つからない |
| 403 | INSUFFICIENT_PERMISSIONS | OWNER権限がない |

---

### 5.5 プロフィール削除

指定されたプロフィールを削除します（OWNER権限必要）。

**エンドポイント**: `DELETE /profiles/{id}`

**認証**: 必要（OWNER権限）

#### パスパラメータ

| パラメータ | 型 | 説明 |
|-----------|---|------|
| id | number | プロフィールID |

#### レスポンス (200 OK)

```json
{
  "status": "success",
  "data": null,
  "message": "Profile deleted successfully"
}
```

#### エラーレスポンス

| ステータス | コード | 説明 |
|-----------|--------|------|
| 404 | PROFILE_NOT_FOUND | プロフィールが見つからない |
| 403 | INSUFFICIENT_PERMISSIONS | OWNER権限がない |

---

### 5.6 プロフィールメンバー一覧取得

指定されたプロフィールに所属するメンバー一覧を取得します。

**エンドポイント**: `GET /profiles/{id}/members`

**認証**: 必要

#### パスパラメータ

| パラメータ | 型 | 説明 |
|-----------|---|------|
| id | number | プロフィールID |

#### クエリパラメータ

| パラメータ | 型 | 必須 | 説明 |
|-----------|---|------|------|
| role | string | - | フィルター: OWNER, MEMBER |
| page | number | - | ページ番号（デフォルト: 0） |
| size | number | - | ページサイズ（デフォルト: 20） |

#### レスポンス (200 OK)

```json
{
  "status": "success",
  "data": {
    "members": [
      {
        "userId": 1,
        "username": "johndoe",
        "email": "john@example.com",
        "role": "OWNER",
        "joinedAt": "2025-11-04T10:00:00Z"
      },
      {
        "userId": 2,
        "username": "janesmith",
        "email": "jane@example.com",
        "role": "MEMBER",
        "joinedAt": "2025-11-05T09:00:00Z"
      }
    ],
    "pagination": {
      "page": 0,
      "size": 20,
      "totalElements": 2,
      "totalPages": 1
    }
  }
}
```

---

### 5.7 プロフィールメンバー追加

指定されたプロフィールに新しいメンバーを追加します（OWNER権限必要）。

**エンドポイント**: `POST /profiles/{id}/members`

**認証**: 必要（OWNER権限）

#### パスパラメータ

| パラメータ | 型 | 説明 |
|-----------|---|------|
| id | number | プロフィールID |

#### リクエスト

```json
{
  "userId": 3,
  "role": "MEMBER"
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|---|------|------|
| userId | number | ✓ | 追加するユーザーID |
| role | string | - | 役割（MEMBER, OWNER）（デフォルト: MEMBER） |

#### レスポンス (201 Created)

```json
{
  "status": "success",
  "data": {
    "userId": 3,
    "username": "newuser",
    "role": "MEMBER",
    "joinedAt": "2025-11-06T10:00:00Z"
  },
  "message": "Member added successfully"
}
```

#### エラーレスポンス

| ステータス | コード | 説明 |
|-----------|--------|------|
| 404 | PROFILE_NOT_FOUND | プロフィールが見つからない |
| 404 | USER_NOT_FOUND | ユーザーが見つからない |
| 409 | MEMBER_ALREADY_EXISTS | ユーザーは既にメンバー |
| 403 | INSUFFICIENT_PERMISSIONS | OWNER権限がない |

---

### 5.8 プロフィールメンバー削除

指定されたプロフィールからメンバーを削除します（OWNER権限必要）。

**エンドポイント**: `DELETE /profiles/{id}/members/{userId}`

**認証**: 必要（OWNER権限）

#### パスパラメータ

| パラメータ | 型 | 説明 |
|-----------|---|------|
| id | number | プロフィールID |
| userId | number | 削除するユーザーID |

#### レスポンス (200 OK)

```json
{
  "status": "success",
  "data": null,
  "message": "Member removed successfully"
}
```

#### エラーレスポンス

| ステータス | コード | 説明 |
|-----------|--------|------|
| 404 | PROFILE_NOT_FOUND | プロフィールが見つからない |
| 404 | MEMBER_NOT_FOUND | メンバーが見つからない |
| 403 | INSUFFICIENT_PERMISSIONS | OWNER権限がない |
| 400 | CANNOT_REMOVE_OWNER | 最後のOWNERは削除不可 |

---

### 5.9 子プロフィール作成

指定されたプロフィールの子プロフィールを作成します（OWNER権限必要）。

**エンドポイント**: `POST /profiles/{id}/children`

**認証**: 必要（親プロフィールのOWNER権限）

#### パスパラメータ

| パラメータ | 型 | 説明 |
|-----------|---|------|
| id | number | 親プロフィールID |

#### リクエスト

```json
{
  "profileName": "ACME Mascot Alpha",
  "displayName": "Alpha the Mascot",
  "bio": "Official mascot character",
  "profileType": "PERSONAL",
  "isPublic": true
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|---|------|------|
| profileName | string | ✓ | プロフィール名（1-100文字） |
| displayName | string | - | 表示名 |
| bio | string | - | 自己紹介 |
| profileType | string | ✓ | プロフィールタイプ（PERSONAL, BUSINESS） |
| isPublic | boolean | - | 公開フラグ（デフォルト: true） |

#### レスポンス (201 Created)

```json
{
  "status": "success",
  "data": {
    "id": 10,
    "parentProfileId": 2,
    "profileName": "ACME Mascot Alpha",
    "displayName": "Alpha the Mascot",
    "bio": "Official mascot character",
    "profileType": "PERSONAL",
    "isPublic": true,
    "createdAt": "2025-11-04T14:00:00Z",
    "updatedAt": "2025-11-04T14:00:00Z",
    "role": "OWNER"
  },
  "message": "Child profile created successfully"
}
```

#### エラーレスポンス

| ステータス | コード | 説明 |
|-----------|--------|------|
| 404 | PROFILE_NOT_FOUND | 親プロフィールが見つからない |
| 403 | INSUFFICIENT_PERMISSIONS | 親プロフィールのOWNER権限がない |
| 400 | MAX_DEPTH_EXCEEDED | プロフィール階層の最大深度を超過 |

---

### 5.10 子プロフィール一覧取得

指定されたプロフィールの直属の子プロフィール一覧を取得します。

**エンドポイント**: `GET /profiles/{id}/children`

**認証**: 必要

#### パスパラメータ

| パラメータ | 型 | 説明 |
|-----------|---|------|
| id | number | 親プロフィールID |

#### クエリパラメータ

| パラメータ | 型 | 必須 | 説明 |
|-----------|---|------|------|
| profileType | string | - | フィルター: PERSONAL, BUSINESS |
| page | number | - | ページ番号（デフォルト: 0） |
| size | number | - | ページサイズ（デフォルト: 20） |

#### レスポンス (200 OK)

```json
{
  "status": "success",
  "data": {
    "parentProfile": {
      "id": 2,
      "profileName": "ACME Corporation",
      "displayName": "ACME Corp"
    },
    "children": [
      {
        "id": 10,
        "profileName": "ACME Mascot Alpha",
        "displayName": "Alpha the Mascot",
        "profileType": "PERSONAL",
        "isPublic": true,
        "createdAt": "2025-11-04T14:00:00Z",
        "childrenCount": 1
      },
      {
        "id": 11,
        "profileName": "ACME Mascot Beta",
        "displayName": "Beta the Mascot",
        "profileType": "PERSONAL",
        "isPublic": true,
        "createdAt": "2025-11-04T14:05:00Z",
        "childrenCount": 0
      }
    ],
    "pagination": {
      "page": 0,
      "size": 20,
      "totalElements": 2,
      "totalPages": 1
    }
  }
}
```

#### エラーレスポンス

| ステータス | コード | 説明 |
|-----------|--------|------|
| 404 | PROFILE_NOT_FOUND | プロフィールが見つからない |
| 403 | ACCESS_DENIED | 非公開プロフィールへのアクセス権限なし |

---

### 5.11 プロフィール階層全体取得

指定されたプロフィールを起点とした階層全体（子孫すべて）を取得します。

**エンドポイント**: `GET /profiles/{id}/tree`

**認証**: 必要

#### パスパラメータ

| パラメータ | 型 | 説明 |
|-----------|---|------|
| id | number | プロフィールID |

#### クエリパラメータ

| パラメータ | 型 | 必須 | 説明 |
|-----------|---|------|------|
| maxDepth | number | - | 取得する最大階層深度（デフォルト: 無制限） |

#### レスポンス (200 OK)

```json
{
  "status": "success",
  "data": {
    "id": 2,
    "profileName": "ACME Corporation",
    "displayName": "ACME Corp",
    "profileType": "BUSINESS",
    "level": 0,
    "children": [
      {
        "id": 10,
        "profileName": "ACME Mascot Alpha",
        "displayName": "Alpha the Mascot",
        "profileType": "PERSONAL",
        "level": 1,
        "children": [
          {
            "id": 15,
            "profileName": "Alpha Mini",
            "displayName": "Mini Alpha",
            "profileType": "PERSONAL",
            "level": 2,
            "children": []
          }
        ]
      },
      {
        "id": 11,
        "profileName": "ACME Mascot Beta",
        "displayName": "Beta the Mascot",
        "profileType": "PERSONAL",
        "level": 1,
        "children": []
      }
    ]
  }
}
```

#### エラーレスポンス

| ステータス | コード | 説明 |
|-----------|--------|------|
| 404 | PROFILE_NOT_FOUND | プロフィールが見つからない |
| 403 | ACCESS_DENIED | 非公開プロフィールへのアクセス権限なし |

---

### 5.12 親プロフィール取得

指定されたプロフィールの親プロフィールを取得します。

**エンドポイント**: `GET /profiles/{id}/parent`

**認証**: 必要

#### パスパラメータ

| パラメータ | 型 | 説明 |
|-----------|---|------|
| id | number | プロフィールID |

#### レスポンス (200 OK)

```json
{
  "status": "success",
  "data": {
    "id": 2,
    "profileName": "ACME Corporation",
    "displayName": "ACME Corp",
    "bio": "Building the future",
    "profileType": "BUSINESS",
    "isPublic": true,
    "createdAt": "2025-11-01T10:00:00Z",
    "updatedAt": "2025-11-04T10:00:00Z"
  }
}
```

#### エラーレスポンス

| ステータス | コード | 説明 |
|-----------|--------|------|
| 404 | PROFILE_NOT_FOUND | プロフィールが見つからない |
| 404 | PARENT_NOT_FOUND | 親プロフィールが存在しない（ルートプロフィール） |
| 403 | ACCESS_DENIED | 非公開プロフィールへのアクセス権限なし |

---

### 5.13 ルートプロフィール取得

指定されたプロフィールが属する階層のルート（最上位の親）を取得します。

**エンドポイント**: `GET /profiles/{id}/root`

**認証**: 必要

#### パスパラメータ

| パラメータ | 型 | 説明 |
|-----------|---|------|
| id | number | プロフィールID |

#### レスポンス (200 OK)

```json
{
  "status": "success",
  "data": {
    "id": 2,
    "profileName": "ACME Corporation",
    "displayName": "ACME Corp",
    "bio": "Building the future",
    "profileType": "BUSINESS",
    "isPublic": true,
    "depth": 2,
    "createdAt": "2025-11-01T10:00:00Z"
  }
}
```

| フィールド | 説明 |
|-----------|------|
| depth | 指定されたプロフィールからルートまでの階層深度 |

#### エラーレスポンス

| ステータス | コード | 説明 |
|-----------|--------|------|
| 404 | PROFILE_NOT_FOUND | プロフィールが見つからない |
| 403 | ACCESS_DENIED | 非公開プロフィールへのアクセス権限なし |

---

### 5.14 親プロフィール変更（付け替え）

子プロフィールの親を変更します。ルートプロフィールを子プロフィールにすることも可能です。

**エンドポイント**: `PUT /profiles/{id}/parent`

**認証**: 必要

**権限**: 対象プロフィールのOWNER、かつ新しい親プロフィールのOWNER権限が必要

#### パスパラメータ

| パラメータ | 型 | 説明 |
|-----------|---|------|
| id | number | 変更対象のプロフィールID（子プロフィール） |

#### リクエストボディ

```json
{
  "parentProfileId": 5
}
```

| フィールド | 型 | 必須 | 説明 |
|-----------|---|------|------|
| parentProfileId | number | ✓ | 新しい親プロフィールのID |

#### レスポンス (200 OK)

```json
{
  "status": "success",
  "data": {
    "id": 4,
    "profileName": "ACME Mascot Alpha",
    "displayName": "Alpha",
    "parentProfileId": 5,
    "profileType": "PERSONAL",
    "isPublic": true,
    "updatedAt": "2025-11-04T12:00:00Z"
  },
  "message": "親プロフィールを変更しました"
}
```

#### バリデーション

| 検証項目 | エラーコード | 説明 |
|---------|-------------|------|
| 循環参照チェック | CIRCULAR_REFERENCE | 循環参照が発生する場合（A → B → C → A） |
| 自己参照チェック | SELF_REFERENCE | 自分自身を親にすることはできない |
| 権限チェック | ACCESS_DENIED | 対象プロフィールまたは新しい親プロフィールのOWNER権限がない |
| 親プロフィール存在チェック | PROFILE_NOT_FOUND | 指定された親プロフィールが存在しない |

#### 実装ロジック（案C）

```sql
-- 1. 既存の親子関係を削除
DELETE FROM profile_profile_relations
WHERE child_profile_id = {id};

-- 2. 新しい親子関係を作成
INSERT INTO profile_profile_relations (parent_profile_id, child_profile_id)
VALUES ({parentProfileId}, {id});

-- 3. 循環参照チェック（再帰CTE）
WITH RECURSIVE check_cycle AS (
    SELECT child_profile_id as id, parent_profile_id, ARRAY[child_profile_id] as path
    FROM profile_profile_relations
    WHERE child_profile_id = {id}

    UNION ALL

    SELECT ppr.child_profile_id, ppr.parent_profile_id, cc.path || ppr.child_profile_id
    FROM profile_profile_relations ppr
    INNER JOIN check_cycle cc ON ppr.parent_profile_id = cc.id
    WHERE NOT (ppr.child_profile_id = ANY(cc.path))
)
SELECT COUNT(*) FROM check_cycle WHERE id = {parentProfileId};
-- 結果が0より大きい場合は循環参照
```

#### エラーレスポンス

| ステータス | コード | 説明 |
|-----------|--------|------|
| 400 | CIRCULAR_REFERENCE | 循環参照が発生する |
| 400 | SELF_REFERENCE | 自分自身を親に指定 |
| 403 | ACCESS_DENIED | 権限なし |
| 404 | PROFILE_NOT_FOUND | プロフィールが見つからない |

#### 使用例

**ケース1: 子プロフィールを別の親に移動**
```
変更前: 企業A → マスコット1
変更後: 企業B → マスコット1

PUT /profiles/3/parent
{ "parentProfileId": 2 }
```

**ケース2: ルートプロフィールを子プロフィールにする**
```
変更前: マスコット1（ルート）
変更後: 企業A → マスコット1

PUT /profiles/3/parent
{ "parentProfileId": 1 }
```

---

### 5.15 親子関係削除（ルート化）

子プロフィールを親から切り離し、ルートプロフィールにします。

**エンドポイント**: `DELETE /profiles/{id}/parent`

**認証**: 必要

**権限**: 対象プロフィールのOWNER、かつ元の親プロフィールのOWNER権限が必要

#### パスパラメータ

| パラメータ | 型 | 説明 |
|-----------|---|------|
| id | number | 独立させるプロフィールID（子プロフィール） |

#### レスポンス (200 OK)

```json
{
  "status": "success",
  "data": {
    "id": 4,
    "profileName": "ACME Mascot Alpha",
    "displayName": "Alpha",
    "parentProfileId": null,
    "profileType": "PERSONAL",
    "isPublic": true,
    "updatedAt": "2025-11-04T12:00:00Z"
  },
  "message": "プロフィールをルート化しました"
}
```

| フィールド | 説明 |
|-----------|------|
| parentProfileId | ルート化後はnull |

#### バリデーション

| 検証項目 | エラーコード | 説明 |
|---------|-------------|------|
| ルートチェック | ALREADY_ROOT | 既にルートプロフィールの場合 |
| 権限チェック | ACCESS_DENIED | 対象プロフィールまたは親プロフィールのOWNER権限がない |

#### 実装ロジック（案C）

```sql
-- profile_profile_relations から該当レコードを削除
DELETE FROM profile_profile_relations
WHERE child_profile_id = {id};
```

#### エラーレスポンス

| ステータス | コード | 説明 |
|-----------|--------|------|
| 400 | ALREADY_ROOT | 既にルートプロフィール |
| 403 | ACCESS_DENIED | 権限なし |
| 404 | PROFILE_NOT_FOUND | プロフィールが見つからない |

#### 使用例

**ケース: 子プロフィールを独立させる**
```
変更前: 企業A → マスコット1
変更後: マスコット1（ルート）

DELETE /profiles/3/parent
```

---

## 6. エラーレスポンス

### 6.1 エラーコード一覧

| コード | HTTP | 説明 |
|-------|------|------|
| VALIDATION_ERROR | 400 | 入力値が不正 |
| WEAK_PASSWORD | 400 | パスワードが弱い |
| CANNOT_REMOVE_OWNER | 400 | 最後のOWNERは削除不可 |
| MAX_DEPTH_EXCEEDED | 400 | プロフィール階層の最大深度を超過 |
| CIRCULAR_REFERENCE | 400 | 循環参照が発生 |
| SELF_REFERENCE | 400 | 自分自身を親に指定 |
| ALREADY_ROOT | 400 | 既にルートプロフィール |
| INVALID_CREDENTIALS | 401 | 認証情報が不正 |
| INVALID_PASSWORD | 401 | パスワードが不正 |
| UNAUTHORIZED | 401 | 認証が必要 |
| ACCESS_DENIED | 403 | アクセス権限なし |
| ACCOUNT_DISABLED | 403 | アカウントが無効 |
| INSUFFICIENT_PERMISSIONS | 403 | 権限不足 |
| USER_NOT_FOUND | 404 | ユーザーが見つからない |
| PROFILE_NOT_FOUND | 404 | プロフィールが見つからない |
| PARENT_NOT_FOUND | 404 | 親プロフィールが見つからない |
| MEMBER_NOT_FOUND | 404 | メンバーが見つからない |
| USERNAME_ALREADY_EXISTS | 409 | ユーザー名が既に存在 |
| EMAIL_ALREADY_EXISTS | 409 | メールアドレスが既に存在 |
| MEMBER_ALREADY_EXISTS | 409 | メンバーが既に存在 |
| INTERNAL_SERVER_ERROR | 500 | サーバー内部エラー |

### 5.2 バリデーションエラーの詳細

バリデーションエラー時は、`details` フィールドに詳細情報が含まれます。

```json
{
  "status": "error",
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      {
        "field": "username",
        "message": "Username must be between 3 and 50 characters"
      },
      {
        "field": "password",
        "message": "Password must be at least 8 characters"
      }
    ]
  },
  "timestamp": "2025-11-04T10:00:00Z"
}
```

---

## 7. ステータスコード

### 6.1 HTTPステータスコード一覧

| コード | 説明 | 使用例 |
|-------|------|-------|
| 200 | OK | GET, PUT, DELETE成功 |
| 201 | Created | POST成功（リソース作成） |
| 204 | No Content | DELETE成功（レスポンスボディなし） |
| 400 | Bad Request | バリデーションエラー |
| 401 | Unauthorized | 認証エラー |
| 403 | Forbidden | 認可エラー |
| 404 | Not Found | リソースが見つからない |
| 409 | Conflict | リソース重複 |
| 500 | Internal Server Error | サーバー内部エラー |

---

## まとめ

本API仕様書は、HideArea MVPフェーズのREST API仕様を定義しています。

**主要な特徴**:
- RESTful設計原則に準拠
- JWT Bearer認証
- 統一的なレスポンス形式
- 詳細なエラーハンドリング
- ページネーション対応

**次のステップ**:
- SpringDoc OpenAPIによる自動生成（Swagger UI）
- Postman/Bruno用コレクション作成
- E2Eテスト実装

**Swagger UIアクセス**:
- 開発環境: `http://localhost:8080/swagger-ui.html`
