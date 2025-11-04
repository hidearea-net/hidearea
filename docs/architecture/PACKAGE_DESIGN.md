# パッケージ設計書

**最終更新日**: 2025-11-04

---

## 目次

1. [バックエンド パッケージ構造](#1-バックエンド-パッケージ構造)
2. [APIバージョニング実装](#2-apiバージョニング実装)
3. [フロントエンド ディレクトリ構造](#3-フロントエンド-ディレクトリ構造)
4. [共通規約](#4-共通規約)

---

## 1. バックエンド パッケージ構造

### 1.1 ベースパッケージ

**ベースパッケージ**: `com.hidearea.core`

### 1.2 全体構造

```
com.hidearea.core/
├── HideAreaApplication.java                # Spring Boot エントリーポイント
│
├── api/                                    # API レイヤー（バージョン管理）
│   └── v1/                                 # API v1
│       ├── controller/                     # REST Controller (v1)
│       │   ├── AuthController.java
│       │   ├── UserController.java
│       │   └── ProfileController.java
│       └── dto/                            # Data Transfer Object (v1)
│           ├── request/
│           │   ├── LoginRequest.java
│           │   ├── UserCreateRequest.java
│           │   └── ProfileCreateRequest.java
│           └── response/
│               ├── AuthResponse.java
│               ├── UserResponse.java
│               └── ProfileResponse.java
│
├── service/                                # ビジネスロジック（バージョン非依存）
│   ├── AuthService.java
│   ├── UserService.java
│   ├── ProfileService.java
│   └── impl/
│       ├── AuthServiceImpl.java
│       ├── UserServiceImpl.java
│       └── ProfileServiceImpl.java
│
├── repository/                             # データアクセス（バージョン非依存）
│   ├── UserRepository.java
│   ├── ProfileRepository.java
│   └── UserProfileRepository.java
│
├── domain/                                 # ドメインモデル（バージョン非依存）
│   ├── entity/                             # JPA エンティティ
│   │   ├── User.java
│   │   ├── Profile.java
│   │   └── UserProfile.java
│   └── vo/                                 # Value Object
│       └── Email.java
│
├── config/                                 # 設定クラス
│   ├── SecurityConfig.java
│   ├── JwtConfig.java
│   ├── DatabaseConfig.java
│   ├── OpenApiConfig.java
│   └── WebConfig.java
│
├── security/                               # セキュリティ関連
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── UserDetailsServiceImpl.java
│
├── exception/                              # 例外クラス
│   ├── BusinessException.java
│   ├── ResourceNotFoundException.java
│   ├── UnauthorizedException.java
│   └── ErrorCode.java
│
└── common/                                 # 共通ユーティリティ
    ├── util/
    │   ├── DateUtil.java
    │   └── StringUtil.java
    └── constants/
        └── AppConstants.java
```

---

## 2. APIバージョニング実装

### 2.1 バージョニング戦略

**URL パスベースバージョニング**を採用します。

#### 採用理由

| 方式 | メリット | デメリット | 採用 |
|-----|---------|-----------|-----|
| **URLパス** | 明確、キャッシュ容易、ドキュメント化しやすい | URL変更が必要 | ✅ |
| ヘッダー | URLが変わらない | 可視性が低い、デバッグ困難 | ❌ |
| クエリパラメータ | 柔軟 | RESTful原則に反する | ❌ |
| コンテンツネゴシエーション | RESTful | 複雑、一般的でない | ❌ |

#### エンドポイント形式

```
/api/v1/auth/login
/api/v1/users/me
/api/v1/profiles

/api/v2/...  (将来)
```

### 2.2 パッケージ構造（詳細）

#### 2.2.1 APIバージョン別パッケージ

```
com.hidearea.core.api/
├── v1/
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   └── ProfileController.java
│   └── dto/
│       ├── request/
│       └── response/
│
└── v2/  (将来)
    ├── controller/
    └── dto/
```

**設計原則**:
- **Controller と DTO のみバージョン管理**: API層（入出力）のみバージョンで分離
- **Service, Repository, Domain はバージョン非依存**: ビジネスロジックとデータ層は共通
- **後方互換性**: 旧バージョンAPIは維持、段階的な廃止

#### 2.2.2 コントローラー実装例

**v1 のコントローラー**:

```java
package com.hidearea.core.api.v1.controller;

import com.hidearea.core.api.v1.dto.request.LoginRequest;
import com.hidearea.core.api.v1.dto.response.AuthResponse;
import com.hidearea.core.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // v1 の DTOを使用
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UserCreateRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

**v2 のコントローラー（将来）**:

```java
package com.hidearea.core.api.v2.controller;

import com.hidearea.core.api.v2.dto.request.LoginRequest;
import com.hidearea.core.api.v2.dto.response.AuthResponse;
import com.hidearea.core.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // v2 の DTOを使用（フィールド追加・変更）
        AuthResponse response = authService.loginV2(request);
        return ResponseEntity.ok(response);
    }
}
```

#### 2.2.3 DTO実装例

**v1 DTO**:

```java
package com.hidearea.core.api.v1.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String usernameOrEmail;

    @NotBlank
    private String password;
}
```

```java
package com.hidearea.core.api.v1.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private UserResponse user;
    private String token;
    private Long expiresIn;
}
```

**v2 DTO（将来、フィールド追加例）**:

```java
package com.hidearea.core.api.v2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private UserResponse user;
    private String accessToken;          // 名称変更
    private String refreshToken;         // 新規追加
    private Long expiresIn;
    private String tokenType;            // 新規追加 (Bearer)
}
```

### 2.3 Service層の実装

**Service層はバージョン非依存**:

```java
package com.hidearea.core.service;

import com.hidearea.core.api.v1.dto.request.LoginRequest;
import com.hidearea.core.api.v1.dto.response.AuthResponse;

public interface AuthService {
    // v1 用メソッド
    AuthResponse login(LoginRequest request);

    // v2 用メソッド（将来追加）
    AuthResponse loginV2(com.hidearea.core.api.v2.dto.request.LoginRequest request);
}
```

**実装クラス**:

```java
package com.hidearea.core.service.impl;

import com.hidearea.core.domain.entity.User;
import com.hidearea.core.repository.UserRepository;
import com.hidearea.core.security.JwtTokenProvider;
import com.hidearea.core.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public AuthResponse login(LoginRequest request) {
        // ビジネスロジックはバージョン非依存
        User user = findUserByUsernameOrEmail(request.getUsernameOrEmail());
        validatePassword(user, request.getPassword());

        String token = jwtTokenProvider.generateToken(user);

        return AuthResponse.builder()
            .user(UserMapper.toResponse(user))
            .token(token)
            .expiresIn(jwtTokenProvider.getExpirationTime())
            .build();
    }

    // 共通ロジック
    private User findUserByUsernameOrEmail(String usernameOrEmail) {
        // 実装...
    }
}
```

### 2.4 バージョン間の差異管理

#### 2.4.1 Mapper パターン

DTO間の変換や、エンティティとDTOの変換にMapperパターンを使用：

```java
package com.hidearea.core.api.v1.mapper;

import com.hidearea.core.domain.entity.User;
import com.hidearea.core.api.v1.dto.response.UserResponse;

public class UserMapper {

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole())
            .enabled(user.getEnabled())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
```

```java
package com.hidearea.core.api.v2.mapper;

import com.hidearea.core.domain.entity.User;
import com.hidearea.core.api.v2.dto.response.UserResponse;

public class UserMapper {

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole())
            .enabled(user.getEnabled())
            .createdAt(user.getCreatedAt())
            .profileCount(user.getUserProfiles().size())  // v2で追加
            .build();
    }
}
```

#### 2.4.2 バージョン廃止戦略

```java
@RestController
@RequestMapping("/api/v1/auth")
@Deprecated  // v1廃止予定を示す
public class AuthController {

    @PostMapping("/login")
    @Deprecated(since = "v2.0", forRemoval = true)
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // レスポンスヘッダーで廃止予定を通知
        return ResponseEntity.ok()
            .header("X-API-Warn", "This API version is deprecated. Please use /api/v2/auth/login")
            .header("X-API-Sunset", "2026-01-01")  // 廃止予定日
            .body(response);
    }
}
```

### 2.5 OpenAPI (Swagger) 設定

**バージョン別のAPI仕様書生成**:

```java
package com.hidearea.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("HideArea API")
                .version("1.0")
                .description("HideArea REST API Documentation"));
    }

    @Bean
    public GroupedOpenApi apiV1() {
        return GroupedOpenApi.builder()
            .group("v1")
            .pathsToMatch("/api/v1/**")
            .build();
    }

    @Bean
    public GroupedOpenApi apiV2() {
        return GroupedOpenApi.builder()
            .group("v2")
            .pathsToMatch("/api/v2/**")
            .build();
    }
}
```

**Swagger UIでの確認**:
- v1: `http://localhost:8080/swagger-ui.html?urls.primaryName=v1`
- v2: `http://localhost:8080/swagger-ui.html?urls.primaryName=v2`

### 2.6 バージョニングのベストプラクティス

#### DO（推奨）

✅ **Controller と DTO のみバージョン管理**
```
api/v1/controller/
api/v1/dto/
```

✅ **Service, Repository, Domain は共通化**
```
service/
repository/
domain/
```

✅ **後方互換性を保つ**
- フィールド追加は OK
- フィールド名変更は新バージョンで対応

✅ **廃止予定の明示**
```java
@Deprecated(since = "v2.0", forRemoval = true)
```

✅ **バージョン情報のレスポンスヘッダー**
```java
.header("X-API-Version", "v1")
.header("X-API-Warn", "Deprecated")
```

#### DON'T（非推奨）

❌ **全レイヤーをバージョン管理しない**
```
// 避けるべき
v1/service/
v1/repository/
v1/domain/
```

❌ **既存APIの破壊的変更**
- v1のAPIは維持し、v2で新機能を追加

❌ **無制限にバージョンを増やす**
- 計画的な廃止（例: v1サポートは2年間）

### 2.7 マイグレーション戦略

#### v1 → v2 への移行例

**段階的移行**:

1. **v2リリース**: v1とv2を並行提供
2. **移行期間**: 6ヶ月〜1年
3. **廃止予告**: レスポンスヘッダーで通知
4. **v1廃止**: 予告期間後にv1を停止

**クライアント側の対応**:

```typescript
// フロントエンド (TypeScript)
const API_VERSION = process.env.VITE_API_VERSION || 'v1';
const API_BASE_URL = `http://localhost:8080/api/${API_VERSION}`;

// v1からv2への切り替えは環境変数で制御
```

---

## 3. フロントエンド ディレクトリ構造

### 3.1 全体構造

```
frontend/src/
├── main.tsx                                # エントリーポイント
├── App.tsx                                 # ルートコンポーネント
├── vite-env.d.ts
│
├── components/                             # 再利用可能コンポーネント
│   ├── common/                             # 汎用コンポーネント
│   │   ├── Button.tsx
│   │   ├── Input.tsx
│   │   ├── Modal.tsx
│   │   ├── Loading.tsx
│   │   └── ErrorBoundary.tsx
│   ├── layout/                             # レイアウトコンポーネント
│   │   ├── Header.tsx
│   │   ├── Footer.tsx
│   │   ├── Sidebar.tsx
│   │   └── Layout.tsx
│   └── user/                               # ユーザー関連コンポーネント
│       ├── UserCard.tsx
│       ├── UserList.tsx
│       └── ProfileCard.tsx
│
├── pages/                                  # ページコンポーネント
│   ├── Login.tsx
│   ├── Register.tsx
│   ├── Dashboard.tsx
│   ├── Profile/
│   │   ├── ProfileList.tsx
│   │   ├── ProfileDetail.tsx
│   │   └── ProfileEdit.tsx
│   └── NotFound.tsx
│
├── hooks/                                  # カスタムHooks
│   ├── useAuth.ts
│   ├── useUser.ts
│   ├── useProfile.ts
│   └── useApi.ts
│
├── services/                               # API通信（バージョン管理）
│   ├── api.ts                              # Axios設定
│   ├── v1/                                 # API v1クライアント
│   │   ├── authService.ts
│   │   ├── userService.ts
│   │   └── profileService.ts
│   └── v2/                                 # API v2クライアント（将来）
│       ├── authService.ts
│       ├── userService.ts
│       └── profileService.ts
│
├── context/                                # React Context
│   ├── AuthContext.tsx
│   └── UserContext.tsx
│
├── types/                                  # TypeScript型定義（バージョン別）
│   ├── common.ts
│   ├── v1/                                 # API v1の型
│   │   ├── user.ts
│   │   ├── profile.ts
│   │   └── auth.ts
│   └── v2/                                 # API v2の型（将来）
│       ├── user.ts
│       ├── profile.ts
│       └── auth.ts
│
├── utils/                                  # ユーティリティ関数
│   ├── dateFormat.ts
│   ├── validation.ts
│   └── storage.ts
│
├── styles/                                 # グローバルスタイル
│   ├── global.css
│   └── theme.ts
│
└── routes/                                 # ルーティング設定
    └── AppRoutes.tsx
```

### 3.2 APIバージョニング（フロントエンド）

#### 3.2.1 API設定

**api.ts**:

```typescript
import axios, { AxiosInstance } from 'axios';

const API_VERSION = import.meta.env.VITE_API_VERSION || 'v1';
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

export const apiClient: AxiosInstance = axios.create({
  baseURL: `${API_BASE_URL}/${API_VERSION}`,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// リクエストインターセプター（JWT付与）
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// レスポンスインターセプター（エラーハンドリング）
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // エラーハンドリング
    return Promise.reject(error);
  }
);
```

#### 3.2.2 サービスクライアント例

**v1/authService.ts**:

```typescript
import { apiClient } from '../api';
import { LoginRequest, AuthResponse } from '@/types/v1/auth';

export const authService = {
  login: async (request: LoginRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/auth/login', request);
    return response.data;
  },

  register: async (request: RegisterRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/auth/register', request);
    return response.data;
  },

  logout: async (): Promise<void> => {
    await apiClient.post('/auth/logout');
  },
};
```

#### 3.2.3 型定義例

**types/v1/auth.ts**:

```typescript
export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface AuthResponse {
  user: UserResponse;
  token: string;
  expiresIn: number;
}

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  role: string;
  enabled: boolean;
  createdAt: string;
}
```

---

## 4. 共通規約

### 4.1 命名規約

#### バックエンド（Java）

| 要素 | 規約 | 例 |
|-----|------|-----|
| パッケージ | 小文字、単語区切りなし | `com.hidearea.core.api.v1` |
| クラス | PascalCase | `UserController` |
| メソッド | camelCase | `findUserById()` |
| 定数 | UPPER_SNAKE_CASE | `MAX_LOGIN_ATTEMPTS` |
| 変数 | camelCase | `userId` |

#### フロントエンド（TypeScript）

| 要素 | 規約 | 例 |
|-----|------|-----|
| ファイル | PascalCase (コンポーネント), camelCase (その他) | `UserCard.tsx`, `authService.ts` |
| コンポーネント | PascalCase | `UserCard` |
| 関数 | camelCase | `fetchUserData()` |
| 定数 | UPPER_SNAKE_CASE | `API_BASE_URL` |
| 変数 | camelCase | `userId` |
| 型・インターフェース | PascalCase | `UserResponse` |

### 4.2 ファイル配置ルール

#### バックエンド

- **Controller**: `api/v{n}/controller/` - API層のみバージョン管理
- **DTO**: `api/v{n}/dto/` - API層のみバージョン管理
- **Service**: `service/` - バージョン非依存
- **Repository**: `repository/` - バージョン非依存
- **Domain**: `domain/` - バージョン非依存

#### フロントエンド

- **Services**: `services/v{n}/` - APIクライアントのみバージョン管理
- **Types**: `types/v{n}/` - API型定義のみバージョン管理
- **Components**: `components/` - バージョン非依存
- **Hooks**: `hooks/` - バージョン非依存

---

## まとめ

本パッケージ設計書では、以下の原則に基づいて設計されています：

### 主要原則

1. **URLパスベースバージョニング**: `/api/v1/`, `/api/v2/` 形式
2. **API層のみバージョン管理**: Controller と DTO のみ分離
3. **ビジネスロジックは共通化**: Service, Repository, Domain は共通
4. **後方互換性の維持**: 旧バージョンAPIは段階的に廃止
5. **明確な廃止戦略**: ヘッダーでの通知、計画的な停止

### バージョン管理対象

**バックエンド**:
- ✅ `api/v{n}/controller/`
- ✅ `api/v{n}/dto/`
- ❌ `service/`（共通）
- ❌ `repository/`（共通）
- ❌ `domain/`（共通）

**フロントエンド**:
- ✅ `services/v{n}/`
- ✅ `types/v{n}/`
- ❌ `components/`（共通）
- ❌ `hooks/`（共通）

これにより、APIの進化と後方互換性のバランスを取りながら、保守性の高いシステムを構築できます。
