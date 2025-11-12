# 実装チェックリスト（MVPフェーズ）

**HideArea - MVPフェーズ実装タスク管理**

**バージョン**: 1.1
**最終更新日**: 2025-11-12

---

## 目次

1. [概要](#1-概要)
2. [実装フェーズ一覧](#2-実装フェーズ一覧)
3. [詳細タスクリスト](#3-詳細タスクリスト)
4. [進捗管理](#4-進捗管理)

---

## 1. 概要

### 1.1 MVPフェーズの目標

**実装する機能**:
- ユーザー管理（登録、ログイン、JWT認証）
- プロフィール管理（CRUD、階層構造、メンバー管理）

**推定工数**: 合計 24日（約1ヶ月）

### 1.2 チェックリストの使い方

- [ ] 未着手タスク
- [x] 完了タスク
- [~] スキップ（オプション機能）

**推奨**: タスクを上から順に進めてください。依存関係を考慮した順序になっています。

---

## 2. 実装フェーズ一覧

| フェーズ | 内容 | 推定工数 | 状態 |
|--------|------|---------|------|
| **Phase 1** | プロジェクト基盤 | 1日 | [x] 完了 |
| **Phase 2** | データベース設定 | 1日 | [x] 完了 |
| **Phase 3** | ドメイン層 | 2日 | [x] 完了 |
| **Phase 4** | リポジトリ層 | 2日 | [x] 完了 |
| **Phase 5** | サービス層 | 3日 | [ ] 次のタスク |
| **Phase 6** | セキュリティ層 | 2日 | [ ] |
| **Phase 7** | API層（バックエンド） | 3日 | [ ] |
| **Phase 8** | フロントエンド | 5日 | [ ] |
| **Phase 9** | テスト | 3日 | [ ] |
| **Phase 10** | ドキュメント・デプロイ | 2日 | [ ] |

---

## 3. 詳細タスクリスト

### Phase 1: プロジェクト基盤（1日） ✅ **完了 (2025-11-05)**

**目標**: プロジェクト構造を作成し、ビルドできる状態にする

#### 1.1 バックエンド基盤

- [x] **Spring Bootプロジェクト初期化**
  - [x] Spring Initializr でプロジェクト作成
    - Group: `net.hidearea`
    - Artifact: `hidearea-backend`
    - Java: 25 LTS
    - Dependencies: Spring Web, Spring Data JPA, PostgreSQL Driver, Spring Security, Lombok
  - [x] Gradle設定 (build.gradle使用)
  - [x] `./gradlew build` で正常にビルドできることを確認

- [x] **パッケージ構造作成**
  - [x] `net.hidearea.core.domain.entity/` パッケージ作成
  - [x] `net.hidearea.core.repository/` パッケージ作成
  - [x] `net.hidearea.core.service/` パッケージ作成 (Phase 5で実装予定)
  - [x] `net.hidearea.core.infrastructure.security/` パッケージ作成
  - [x] `net.hidearea.core.presentation.api.v1.controller/` パッケージ作成
  - [x] `net.hidearea.core.api.v1.dto/` パッケージ作成 (Phase 7で実装予定)
  - [x] `net.hidearea.core.config/` パッケージ作成 (Phase 6で実装予定)
  - [x] `net.hidearea.core.exception/` パッケージ作成 (Phase 5で実装予定)

- [x] **application.yml設定**
  - [x] データベース接続設定 (PostgreSQL + H2)
  - [x] JPA/Hibernate設定
  - [x] ログ設定
  - [x] Spring Profilesの設定（h2プロファイル作成済み）

**参考**: [PACKAGE_DESIGN.md](../architecture/PACKAGE_DESIGN.md)

#### 1.2 フロントエンド基盤

- [x] **Reactプロジェクト初期化**
  - [x] Viteでプロジェクト作成: `npm create vite@latest frontend -- --template react-ts`
  - [x] 依存関係インストール: `npm install`
  - [x] `npm run dev` で起動確認

- [~] **必要なライブラリのインストール** (Phase 8で実装予定)
  - [~] Axios: `npm install axios`
  - [~] React Router: `npm install react-router-dom`
  - [~] 状態管理: `npm install zustand`（または React Context API使用）
  - [~] UIライブラリ: `npm install @mui/material @emotion/react @emotion/styled`（Material-UI）

- [~] **ディレクトリ構造作成** (Phase 8で実装予定)
  - [~] `src/components/` ディレクトリ作成
  - [~] `src/pages/` ディレクトリ作成
  - [~] `src/services/v1/` ディレクトリ作成
  - [~] `src/types/v1/` ディレクトリ作成
  - [~] `src/hooks/` ディレクトリ作成
  - [~] `src/utils/` ディレクトリ作成

**参考**: [PACKAGE_DESIGN.md](../architecture/PACKAGE_DESIGN.md) - 3. フロントエンド ディレクトリ構造

#### 1.3 Docker設定

- [x] **Docker Compose設定**
  - [x] `docker-compose.yml` 作成
    - [x] PostgreSQL サービス定義
    - [x] 環境変数設定（.env）
    - [x] ボリューム設定（データ永続化）
  - [x] `.env.example` 作成
  - [x] `docker compose up -d postgres` でDB起動確認

---

### Phase 2: データベース設定（1日） ✅ **完了 (2025-11-06)**

**目標**: データベーススキーマを構築し、マイグレーションを実行

#### 2.1 Flyway設定

- [x] **Flyway依存関係追加**
  - [x] `build.gradle` にFlyway依存関係追加
  - [x] `application.yml` でFlyway設定

- [x] **マイグレーションファイル作成**
  - [x] `src/main/resources/db/migration/` ディレクトリ作成
  - [x] `V1__init_schema.sql` 作成
    - [x] `users` テーブル作成
    - [x] `profiles` テーブル作成
    - [x] `user_profiles` 中間テーブル作成
    - [x] `profile_profile_relations` 中間テーブル作成
    - [x] インデックス作成
  - [~] `V2__add_sample_data.sql` 作成（必要に応じて後で追加）

- [x] **マイグレーション実行**
  - [x] Flywayマイグレーション自動実行（アプリケーション起動時）
  - [x] H2データベースでテーブル確認済み

**参考**: [DATA_DESIGN.md](../architecture/DATA_DESIGN.md) - 5. データベーススキーマ

---

### Phase 3: ドメイン層（2日） ✅ **完了 (2025-11-12)**

**目標**: エンティティクラスとEnumを実装

#### 3.1 Enum定義

- [x] **UserRole Enum**
  - [x] `net.hidearea.core.domain.entity.UserRole` 作成
  - [x] `USER`, `ADMIN` 定義

- [x] **ProfileType Enum**
  - [x] `net.hidearea.core.domain.entity.ProfileType` 作成
  - [x] `PERSONAL`, `BUSINESS` 定義

- [x] **RoleInProfile Enum**
  - [x] `net.hidearea.core.domain.entity.RoleInProfile` 作成
  - [x] `OWNER`, `MEMBER` 定義

**参考**: [DATA_DESIGN.md](../architecture/DATA_DESIGN.md) - 1. エンティティ設計

#### 3.2 エンティティ実装

- [x] **User エンティティ**
  - [x] `net.hidearea.core.domain.entity.User` 作成
  - [x] フィールド定義: `id`, `username`, `passwordHash`, `role`, `enabled`, `createdAt`, `updatedAt`
    - 📝 注: emailフィールドはProfileエンティティで管理（設計変更）
  - [x] `@Entity`, `@Table`, `@Id`, `@GeneratedValue` アノテーション設定
  - [x] `@OneToMany` リレーション設定（UserProfile）
  - [x] Lombok: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
  - [x] 監査機能: `@EntityListeners(AuditingEntityListener.class)`

- [x] **Profile エンティティ**
  - [x] `net.hidearea.core.domain.entity.Profile` 作成
  - [x] フィールド定義: `id`, `email`, `profileName`, `displayName`, `bio`, `avatarUrl`, `profileType`, `isPublic`, `createdAt`, `updatedAt`
  - [x] `@OneToMany` リレーション設定（UserProfile, ProfileProfileRelation）
  - [x] Lombok設定: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`

- [x] **UserProfile エンティティ**
  - [x] `net.hidearea.core.domain.entity.UserProfile` 作成
  - [x] フィールド定義: `id`, `user`, `profile`, `roleInProfile`, `joinedAt`
  - [x] `@ManyToOne` リレーション設定（User, Profile）
  - [x] 複合ユニーク制約: `@UniqueConstraint(columnNames = {"user_id", "profile_id"})`
  - [x] Lombok設定: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`

- [x] **ProfileProfileRelation エンティティ**
  - [x] `net.hidearea.core.domain.entity.ProfileProfileRelation` 作成
  - [x] フィールド定義: `id`, `parentProfile`, `childProfile`, `createdAt`
  - [x] `@ManyToOne` リレーション設定（Profile）
  - [x] 複合ユニーク制約: `@UniqueConstraint(columnNames = {"parent_profile_id", "child_profile_id"})`
  - [x] Lombok設定: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`

**参考**: [DATA_DESIGN.md](../architecture/DATA_DESIGN.md) - 1. エンティティ設計

#### 3.3 JPA設定

- [x] **JPA Auditing有効化**
  - [x] `@EnableJpaAuditing` を`@SpringBootApplication`クラスに追加
  - [x] `@CreatedDate`, `@LastModifiedDate` が自動設定されることを確認

---

### Phase 4: リポジトリ層（2日） ✅ **完了 (2025-11-12)**

**目標**: データアクセス層を実装

#### 4.1 Repositoryインターフェース作成

- [x] **UserRepository**
  - [x] `net.hidearea.core.repository.UserRepository` 作成
  - [x] `JpaRepository<User, Long>` を継承
  - [x] メソッド定義:
    - [x] `Optional<User> findByUsername(String username)`
    - [x] `boolean existsByUsername(String username)`
    - [x] `Optional<User> findByUsernameAndEnabledTrue(String username)`

- [x] **ProfileRepository**
  - [x] `net.hidearea.core.repository.ProfileRepository` 作成
  - [x] `JpaRepository<Profile, Long>` を継承
  - [x] メソッド定義:
    - [x] `Optional<Profile> findByEmail(String email)`
    - [x] `boolean existsByEmail(String email)`
    - [x] `List<Profile> findByProfileType(ProfileType profileType)`
    - [x] `List<Profile> findByIsPublicTrue()`
    - [x] `List<Profile> findByProfileNameContaining(String keyword)`
    - [x] カスタムクエリ（`@Query`）:
      - [x] `findChildrenByParentId(Long parentId)` - 子プロフィール取得
      - [x] `findRootProfiles()` - ルートプロフィール取得
      - [x] `getProfileDepth(Long profileId)` - 階層深度取得（再帰クエリ）
      - [x] `findRootProfileId(Long profileId)` - ルートID取得（再帰クエリ）

- [x] **UserProfileRepository**
  - [x] `net.hidearea.core.repository.UserProfileRepository` 作成
  - [x] `JpaRepository<UserProfile, Long>` を継承
  - [x] メソッド定義:
    - [x] `List<UserProfile> findByUserId(Long userId)` - JOIN FETCH使用
    - [x] `List<UserProfile> findByProfileId(Long profileId)` - JOIN FETCH使用
    - [x] `Optional<UserProfile> findByUserIdAndProfileId(Long userId, Long profileId)`
    - [x] `boolean existsByUserIdAndProfileId(Long userId, Long profileId)`
    - [x] `List<UserProfile> findOwnersByProfileId(Long profileId)`
    - [x] `boolean existsByUserIdAndProfileIdAndRoleInProfile(...)`
    - [x] `List<UserProfile> findRootProfilesByUserId(Long userId)` - カスタムクエリ

- [x] **ProfileProfileRelationRepository**
  - [x] `net.hidearea.core.repository.ProfileProfileRelationRepository` 作成
  - [x] `JpaRepository<ProfileProfileRelation, Long>` を継承
  - [x] メソッド定義:
    - [x] `List<ProfileProfileRelation> findByParentProfileId(Long parentProfileId)`
    - [x] `Optional<ProfileProfileRelation> findByChildProfileId(Long childProfileId)`
    - [x] `boolean existsByParentProfileIdAndChildProfileId(...)`
    - [x] カスタムクエリ（再帰CTE）:
      - [x] `findAllDescendantIds(Long profileId)` - 全子孫取得
      - [x] `findAllAncestorIds(Long profileId)` - 全祖先取得

**参考**: [DATA_DESIGN.md](../architecture/DATA_DESIGN.md) - 3. リポジトリパターン

#### 4.2 Repositoryテスト作成

- [~] **統合テスト環境設定** (Phase 9で実装予定)
  - [~] `@DataJpaTest` を使用したテストクラス作成
  - [~] テスト用データベース設定（H2 or Testcontainers）

- [~] **各Repositoryのテスト** (Phase 9で実装予定)
  - [~] UserRepositoryTest作成
  - [~] ProfileRepositoryTest作成（再帰クエリ含む）
  - [~] UserProfileRepositoryTest作成
  - [~] ProfileProfileRelationRepositoryTest作成

---

### Phase 5: サービス層（3日）

**目標**: ビジネスロジックを実装

#### 5.1 例外クラス作成

- [ ] **カスタム例外定義**
  - [ ] `net.hidearea.core.exception.BusinessException` - ビジネスロジック例外
  - [ ] `net.hidearea.core.exception.ResourceNotFoundException` - リソース未検出
  - [ ] `net.hidearea.core.exception.UnauthorizedException` - 認証失敗
  - [ ] `net.hidearea.core.exception.ErrorCode` - エラーコードEnum

- [ ] **グローバル例外ハンドラー**
  - [ ] `@RestControllerAdvice` でグローバルハンドラー作成
  - [ ] 各例外をHTTPステータスコードにマッピング

#### 5.2 AuthService実装

- [ ] **AuthService インターフェース**
  - [ ] `net.hidearea.core.service.AuthService` 作成
  - [ ] メソッド定義:
    - [ ] `AuthResponse login(LoginRequest request)` - ログイン
    - [ ] `AuthResponse register(RegisterRequest request)` - ユーザー登録
    - [ ] `void logout(String token)` - ログアウト（将来対応）

- [ ] **AuthServiceImpl 実装クラス**
  - [ ] `net.hidearea.core.service.impl.AuthServiceImpl` 作成
  - [ ] `@Service`, `@Transactional` アノテーション設定
  - [ ] ログイン処理:
    - [ ] ユーザー名/メールで検索
    - [ ] パスワード検証（BCrypt）
    - [ ] JWT生成
  - [ ] ユーザー登録処理:
    - [ ] 重複チェック（username, email）
    - [ ] パスワードハッシュ化（BCrypt）
    - [ ] User エンティティ作成・保存
    - [ ] JWT生成

**参考**: [ARCHITECTURE.md](../architecture/ARCHITECTURE.md) - 6. データフロー

#### 5.3 UserService実装

- [ ] **UserService インターフェース**
  - [ ] `net.hidearea.core.service.UserService` 作成
  - [ ] メソッド定義:
    - [ ] `UserDto getUserById(Long id)` - ユーザー取得
    - [ ] `UserDto getUserByUsername(String username)` - ユーザー取得
    - [ ] `UserDto updateUser(Long id, UpdateUserRequest request)` - 更新
    - [ ] `void deleteUser(Long id)` - 削除
    - [ ] `void changePassword(Long id, ChangePasswordRequest request)` - パスワード変更

- [ ] **UserServiceImpl 実装クラス**
  - [ ] `net.hidearea.core.service.impl.UserServiceImpl` 作成
  - [ ] `@Service`, `@Transactional` アノテーション設定
  - [ ] 各メソッド実装
  - [ ] 権限チェック（自分自身またはADMINのみ）

#### 5.4 ProfileService実装

- [ ] **ProfileService インターフェース**
  - [ ] `net.hidearea.core.service.ProfileService` 作成
  - [ ] メソッド定義:
    - [ ] `ProfileDto createProfile(CreateProfileRequest request, String username)` - 作成
    - [ ] `ProfileDto createChildProfile(Long parentId, CreateProfileRequest request, String username)` - 子プロフィール作成
    - [ ] `ProfileDto getProfileById(Long id)` - 取得
    - [ ] `List<ProfileDto> getProfilesByUser(String username)` - ユーザーのプロフィール一覧
    - [ ] `ProfileTreeDto getProfileTree(Long id)` - 階層構造取得
    - [ ] `ProfileDto updateProfile(Long id, UpdateProfileRequest request, String username)` - 更新
    - [ ] `void deleteProfile(Long id, String username)` - 削除
    - [ ] `void addMember(Long profileId, AddMemberRequest request, String username)` - メンバー追加
    - [ ] `void removeMember(Long profileId, Long userId, String username)` - メンバー削除

- [ ] **ProfileServiceImpl 実装クラス**
  - [ ] `net.hidearea.core.service.impl.ProfileServiceImpl` 作成
  - [ ] `@Service`, `@Transactional` アノテーション設定
  - [ ] プロフィール作成処理:
    - [ ] Profile エンティティ作成
    - [ ] UserProfile 作成（OWNER）
  - [ ] 子プロフィール作成処理:
    - [ ] 権限チェック（親プロフィールのOWNER）
    - [ ] 階層深度チェック
    - [ ] Profile エンティティ作成
    - [ ] ProfileProfileRelation 作成
    - [ ] UserProfile 作成（OWNER）
  - [ ] 階層構造取得処理:
    - [ ] 再帰的にツリー構築
  - [ ] 更新・削除処理:
    - [ ] 権限チェック（OWNER）
  - [ ] メンバー追加・削除処理:
    - [ ] 権限チェック（OWNER）

**参考**: [DATA_DESIGN.md](../architecture/DATA_DESIGN.md) - 6. ユースケース例

#### 5.5 DTOとMapperの実装

- [ ] **DTO作成（Request）**
  - [ ] `LoginRequest`, `RegisterRequest`, `ChangePasswordRequest`
  - [ ] `CreateProfileRequest`, `UpdateProfileRequest`, `AddMemberRequest`
  - [ ] バリデーションアノテーション（`@NotBlank`, `@Email`, etc.）

- [ ] **DTO作成（Response）**
  - [ ] `AuthResponse`, `UserDto`, `ProfileDto`, `ProfileTreeDto`
  - [ ] Lombok: `@Data`, `@Builder`

- [ ] **Mapper作成**
  - [ ] `UserMapper` - Entity ↔ DTO 変換
  - [ ] `ProfileMapper` - Entity ↔ DTO 変換
  - [ ] MapStruct または手動実装

---

### Phase 6: セキュリティ層（2日）

**目標**: JWT認証とSpring Securityを実装

#### 6.1 JWT実装

- [ ] **JWT依存関係追加**
  - [ ] `build.gradle.kts` に`jjwt` ライブラリ追加
    ```kotlin
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")
    ```

- [ ] **JwtTokenProvider 実装**
  - [ ] `net.hidearea.core.security.JwtTokenProvider` 作成
  - [ ] メソッド実装:
    - [ ] `generateToken(User user)` - JWT生成
    - [ ] `getUsernameFromToken(String token)` - ユーザー名取得
    - [ ] `validateToken(String token)` - トークン検証
    - [ ] `getExpirationTime()` - 有効期限取得
  - [ ] JWT設定:
    - [ ] 秘密鍵（環境変数から取得）
    - [ ] 有効期限（24時間）
    - [ ] ペイロード（userId, username, role）

**参考**: [ARCHITECTURE.md](../architecture/ARCHITECTURE.md) - 7.1 認証・認可の仕組み

#### 6.2 Spring Security設定

- [ ] **SecurityConfig 実装**
  - [ ] `net.hidearea.core.config.SecurityConfig` 作成
  - [ ] `@Configuration`, `@EnableWebSecurity` アノテーション
  - [ ] SecurityFilterChain Bean定義:
    - [ ] CSRF無効化（JWT使用のため）
    - [ ] CORS設定
    - [ ] 認証不要エンドポイント: `/api/v1/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`
    - [ ] その他エンドポイント: 認証必要
    - [ ] ステートレスセッション管理
  - [ ] PasswordEncoder Bean定義（BCrypt、強度10）

- [ ] **JwtAuthenticationFilter 実装**
  - [ ] `net.hidearea.core.security.JwtAuthenticationFilter` 作成
  - [ ] `OncePerRequestFilter` を継承
  - [ ] フィルター処理:
    - [ ] `Authorization` ヘッダーからJWT取得
    - [ ] JWT検証
    - [ ] SecurityContextにAuthenticationを設定
  - [ ] SecurityFilterChainに登録（UsernamePasswordAuthenticationFilter の前）

- [ ] **UserDetailsServiceImpl 実装**
  - [ ] `net.hidearea.core.security.UserDetailsServiceImpl` 作成
  - [ ] `UserDetailsService` を実装
  - [ ] `loadUserByUsername(String username)` 実装:
    - [ ] UserRepository でユーザー検索
    - [ ] `UserDetails` オブジェクト生成

**参考**: [ARCHITECTURE.md](../architecture/ARCHITECTURE.md) - 7. セキュリティアーキテクチャ

#### 6.3 セキュリティテスト

- [ ] **JwtTokenProviderTest 作成**
  - [ ] トークン生成テスト
  - [ ] トークン検証テスト
  - [ ] 有効期限切れテスト

- [ ] **SecurityConfigTest 作成**
  - [ ] 認証不要エンドポイントのテスト
  - [ ] 認証必要エンドポイントのテスト（401エラー確認）

---

### Phase 7: API層（バックエンド）（3日）

**目標**: REST APIエンドポイントを実装

#### 7.1 AuthController実装

- [ ] **AuthController 作成**
  - [ ] `net.hidearea.core.api.v1.controller.AuthController` 作成
  - [ ] `@RestController`, `@RequestMapping("/api/v1/auth")` アノテーション
  - [ ] エンドポイント実装:
    - [ ] `POST /api/v1/auth/login` - ログイン
    - [ ] `POST /api/v1/auth/register` - ユーザー登録
    - [ ] `POST /api/v1/auth/logout` - ログアウト（将来対応）
  - [ ] バリデーション（`@Valid`）
  - [ ] レスポンス（`ResponseEntity`）

**参考**: [API_SPECIFICATION.md](../api/API_SPECIFICATION.md) - 2. 認証API

#### 7.2 UserController実装

- [ ] **UserController 作成**
  - [ ] `net.hidearea.core.api.v1.controller.UserController` 作成
  - [ ] `@RestController`, `@RequestMapping("/api/v1/users")` アノテーション
  - [ ] エンドポイント実装:
    - [ ] `GET /api/v1/users/me` - 現在のユーザー情報取得
    - [ ] `GET /api/v1/users/{id}` - ユーザー情報取得
    - [ ] `PUT /api/v1/users/{id}` - ユーザー情報更新
    - [ ] `DELETE /api/v1/users/{id}` - ユーザー削除
    - [ ] `PUT /api/v1/users/{id}/password` - パスワード変更
  - [ ] 認証情報取得（`@AuthenticationPrincipal UserDetails`）
  - [ ] 権限チェック

**参考**: [API_SPECIFICATION.md](../api/API_SPECIFICATION.md) - 3. ユーザーAPI

#### 7.3 ProfileController実装

- [ ] **ProfileController 作成**
  - [ ] `net.hidearea.core.api.v1.controller.ProfileController` 作成
  - [ ] `@RestController`, `@RequestMapping("/api/v1/profiles")` アノテーション
  - [ ] エンドポイント実装:
    - [ ] `POST /api/v1/profiles` - プロフィール作成
    - [ ] `POST /api/v1/profiles/{parentId}/children` - 子プロフィール作成
    - [ ] `GET /api/v1/profiles` - プロフィール一覧取得
    - [ ] `GET /api/v1/profiles/{id}` - プロフィール詳細取得
    - [ ] `GET /api/v1/profiles/{id}/tree` - プロフィール階層取得
    - [ ] `PUT /api/v1/profiles/{id}` - プロフィール更新
    - [ ] `DELETE /api/v1/profiles/{id}` - プロフィール削除
    - [ ] `POST /api/v1/profiles/{id}/members` - メンバー追加
    - [ ] `DELETE /api/v1/profiles/{id}/members/{userId}` - メンバー削除
  - [ ] 認証情報取得
  - [ ] 権限チェック

**参考**: [API_SPECIFICATION.md](../api/API_SPECIFICATION.md) - 4. プロフィールAPI

#### 7.4 例外ハンドリング

- [ ] **GlobalExceptionHandler 実装**
  - [ ] `@RestControllerAdvice` でグローバル例外ハンドラー作成
  - [ ] 各例外のハンドリング:
    - [ ] `ResourceNotFoundException` → 404 Not Found
    - [ ] `UnauthorizedException` → 401 Unauthorized
    - [ ] `BusinessException` → 400 Bad Request
    - [ ] `MethodArgumentNotValidException` → 400 Bad Request（バリデーションエラー）
    - [ ] `Exception` → 500 Internal Server Error
  - [ ] エラーレスポンス形式統一（ErrorResponse DTO）

#### 7.5 OpenAPI（Swagger）設定

- [ ] **OpenAPI依存関係追加**
  - [ ] `build.gradle.kts` に`springdoc-openapi-starter-webmvc-ui` 追加

- [ ] **OpenApiConfig 実装**
  - [ ] `net.hidearea.core.config.OpenApiConfig` 作成
  - [ ] API情報設定（タイトル、バージョン、説明）
  - [ ] APIバージョン別グループ設定（v1, v2）
  - [ ] JWT認証スキーム設定

- [ ] **Swagger UI動作確認**
  - [ ] http://localhost:8080/swagger-ui.html にアクセス
  - [ ] 全エンドポイントが表示されることを確認

**参考**: [PACKAGE_DESIGN.md](../architecture/PACKAGE_DESIGN.md) - 2.5 OpenAPI (Swagger) 設定

---

### Phase 8: フロントエンド（5日）

**目標**: React SPAを実装

#### 8.1 基盤設定

- [ ] **ルーティング設定**
  - [ ] React Router設定（`src/routes/AppRoutes.tsx`）
  - [ ] ルート定義:
    - [ ] `/login` - ログイン画面
    - [ ] `/register` - 登録画面
    - [ ] `/dashboard` - ダッシュボード（認証必須）
    - [ ] `/profiles` - プロフィール一覧
    - [ ] `/profiles/:id` - プロフィール詳細
    - [ ] `/profiles/:id/edit` - プロフィール編集
  - [ ] 認証ルートガード実装

- [ ] **Axios設定**
  - [ ] `src/services/api.ts` 作成
  - [ ] ベースURL設定（環境変数: `VITE_API_BASE_URL`）
  - [ ] リクエストインターセプター（JWT付与）
  - [ ] レスポンスインターセプター（エラーハンドリング）

**参考**: [PACKAGE_DESIGN.md](../architecture/PACKAGE_DESIGN.md) - 3.2 APIバージョニング（フロントエンド）

#### 8.2 型定義

- [ ] **TypeScript型定義作成**
  - [ ] `src/types/v1/user.ts`:
    - [ ] `User`, `UserResponse`, `UpdateUserRequest`
  - [ ] `src/types/v1/profile.ts`:
    - [ ] `Profile`, `ProfileResponse`, `CreateProfileRequest`, `UpdateProfileRequest`, `ProfileTree`
  - [ ] `src/types/v1/auth.ts`:
    - [ ] `LoginRequest`, `RegisterRequest`, `AuthResponse`

**参考**: [PACKAGE_DESIGN.md](../architecture/PACKAGE_DESIGN.md) - 3.2.3 型定義例

#### 8.3 APIクライアント実装

- [ ] **authService 実装**
  - [ ] `src/services/v1/authService.ts` 作成
  - [ ] メソッド実装:
    - [ ] `login(request: LoginRequest)` → `POST /api/v1/auth/login`
    - [ ] `register(request: RegisterRequest)` → `POST /api/v1/auth/register`
    - [ ] `logout()` → `POST /api/v1/auth/logout`

- [ ] **userService 実装**
  - [ ] `src/services/v1/userService.ts` 作成
  - [ ] メソッド実装:
    - [ ] `getMe()` → `GET /api/v1/users/me`
    - [ ] `getUser(id: number)` → `GET /api/v1/users/{id}`
    - [ ] `updateUser(id: number, request)` → `PUT /api/v1/users/{id}`
    - [ ] `deleteUser(id: number)` → `DELETE /api/v1/users/{id}`
    - [ ] `changePassword(id: number, request)` → `PUT /api/v1/users/{id}/password`

- [ ] **profileService 実装**
  - [ ] `src/services/v1/profileService.ts` 作成
  - [ ] メソッド実装:
    - [ ] `createProfile(request)` → `POST /api/v1/profiles`
    - [ ] `createChildProfile(parentId, request)` → `POST /api/v1/profiles/{parentId}/children`
    - [ ] `getProfiles()` → `GET /api/v1/profiles`
    - [ ] `getProfile(id)` → `GET /api/v1/profiles/{id}`
    - [ ] `getProfileTree(id)` → `GET /api/v1/profiles/{id}/tree`
    - [ ] `updateProfile(id, request)` → `PUT /api/v1/profiles/{id}`
    - [ ] `deleteProfile(id)` → `DELETE /api/v1/profiles/{id}`
    - [ ] `addMember(id, request)` → `POST /api/v1/profiles/{id}/members`
    - [ ] `removeMember(id, userId)` → `DELETE /api/v1/profiles/{id}/members/{userId}`

#### 8.4 状態管理

- [ ] **認証状態管理**
  - [ ] `src/context/AuthContext.tsx` または `src/stores/authStore.ts` 作成
  - [ ] 状態: `user`, `token`, `isAuthenticated`
  - [ ] アクション: `login()`, `logout()`, `register()`
  - [ ] LocalStorageでトークン永続化

- [ ] **カスタムフック**
  - [ ] `src/hooks/useAuth.ts` - 認証フック
  - [ ] `src/hooks/useUser.ts` - ユーザー情報フック
  - [ ] `src/hooks/useProfile.ts` - プロフィール情報フック

#### 8.5 共通コンポーネント

- [ ] **レイアウトコンポーネント**
  - [ ] `src/components/layout/Header.tsx` - ヘッダー（ナビゲーション、ログアウト）
  - [ ] `src/components/layout/Footer.tsx` - フッター
  - [ ] `src/components/layout/Sidebar.tsx` - サイドバー
  - [ ] `src/components/layout/Layout.tsx` - レイアウトラッパー

- [ ] **汎用コンポーネント**
  - [ ] `src/components/common/Button.tsx` - ボタン
  - [ ] `src/components/common/Input.tsx` - 入力フィールド
  - [ ] `src/components/common/Modal.tsx` - モーダルダイアログ
  - [ ] `src/components/common/Loading.tsx` - ローディングスピナー
  - [ ] `src/components/common/ErrorBoundary.tsx` - エラーバウンダリー

#### 8.6 認証フロー

- [ ] **ログイン画面**
  - [ ] `src/pages/Login.tsx` 作成
  - [ ] フォーム実装（username/email, password）
  - [ ] バリデーション
  - [ ] ログイン処理
  - [ ] エラーハンドリング
  - [ ] 登録画面へのリンク

- [ ] **登録画面**
  - [ ] `src/pages/Register.tsx` 作成
  - [ ] フォーム実装（username, email, password, password確認）
  - [ ] バリデーション
  - [ ] ユーザー登録処理
  - [ ] エラーハンドリング
  - [ ] ログイン画面へのリンク

- [ ] **認証ガード**
  - [ ] `src/components/ProtectedRoute.tsx` 作成
  - [ ] 未認証時はログイン画面へリダイレクト

#### 8.7 プロフィール管理画面

- [ ] **プロフィール一覧画面**
  - [ ] `src/pages/Profile/ProfileList.tsx` 作成
  - [ ] プロフィール一覧表示（カード形式）
  - [ ] 検索・フィルター機能
  - [ ] プロフィール作成ボタン

- [ ] **プロフィール詳細画面**
  - [ ] `src/pages/Profile/ProfileDetail.tsx` 作成
  - [ ] プロフィール情報表示
  - [ ] 階層構造表示（ツリー形式）
  - [ ] メンバー一覧表示
  - [ ] 編集・削除ボタン（OWNER のみ）

- [ ] **プロフィール作成・編集画面**
  - [ ] `src/pages/Profile/ProfileEdit.tsx` 作成
  - [ ] フォーム実装（profileName, displayName, bio, avatarUrl, profileType, isPublic）
  - [ ] バリデーション
  - [ ] 作成/更新処理
  - [ ] エラーハンドリング

- [ ] **プロフィール階層コンポーネント**
  - [ ] `src/components/user/ProfileHierarchy.tsx` 作成
  - [ ] ツリー構造の表示（再帰的）
  - [ ] 子プロフィール追加ボタン

#### 8.8 ダッシュボード

- [ ] **ダッシュボード画面**
  - [ ] `src/pages/Dashboard.tsx` 作成
  - [ ] ユーザー情報表示
  - [ ] 所属プロフィール一覧
  - [ ] クイックアクション（プロフィール作成など）

---

### Phase 9: テスト（3日）

**目標**: ユニットテスト・統合テストを実装

#### 9.1 バックエンドテスト

- [ ] **Repositoryテスト**
  - [ ] UserRepositoryTest
  - [ ] ProfileRepositoryTest
  - [ ] UserProfileRepositoryTest
  - [ ] ProfileProfileRelationRepositoryTest
  - [ ] `@DataJpaTest` 使用
  - [ ] テストデータ投入（`@BeforeEach`）

- [ ] **Serviceテスト**
  - [ ] AuthServiceTest
  - [ ] UserServiceTest
  - [ ] ProfileServiceTest
  - [ ] `@SpringBootTest` または `@ExtendWith(MockitoExtension.class)` 使用
  - [ ] Repositoryをモック化（`@Mock`, `@InjectMocks`）
  - [ ] 正常系・異常系テスト

- [ ] **Controllerテスト**
  - [ ] AuthControllerTest
  - [ ] UserControllerTest
  - [ ] ProfileControllerTest
  - [ ] `@WebMvcTest` 使用
  - [ ] MockMvcでHTTPリクエストシミュレーション
  - [ ] JWT認証テスト

**参考**: [DEVELOPMENT_GUIDE.md](./DEVELOPMENT_GUIDE.md)

#### 9.2 フロントエンドテスト

- [ ] **ユニットテスト**
  - [ ] ユーティリティ関数のテスト
  - [ ] カスタムフックのテスト
  - [ ] Vitest使用

- [ ] **コンポーネントテスト**
  - [ ] React Testing Libraryでコンポーネントテスト
  - [ ] LoginコンポーネントTest
  - [ ] ProfileListコンポーネントTest
  - [ ] モックAPIレスポンス（MSW使用）

#### 9.3 E2Eテスト（オプション）

- [ ] **Cypressセットアップ**
  - [ ] Cypress依存関係追加
  - [ ] テストシナリオ作成:
    - [ ] ユーザー登録 → ログイン → プロフィール作成 → ログアウト

---

### Phase 10: ドキュメント・デプロイ（2日）

**目標**: ドキュメント整備とデプロイ準備

#### 10.1 ドキュメント整備

- [ ] **README.md更新**
  - [ ] プロジェクト概要
  - [ ] 環境構築手順
  - [ ] 使い方
  - [ ] スクリーンショット追加

- [ ] **API仕様書更新**
  - [ ] Swagger/OpenAPI自動生成確認
  - [ ] API_SPECIFICATION.mdの更新（変更があれば）

- [ ] **CHANGELOG.md作成**
  - [ ] MVPフェーズの変更履歴記録

#### 10.2 Docker本番環境設定

- [ ] **Dockerfile作成**
  - [ ] バックエンド用Dockerfile
  - [ ] フロントエンド用Dockerfile（Nginxでホスティング）
  - [ ] マルチステージビルド

- [ ] **docker-compose.prod.yml作成**
  - [ ] 本番環境用Docker Compose設定
  - [ ] サービス定義: postgres, backend, frontend, nginx
  - [ ] 環境変数設定
  - [ ] ボリューム設定
  - [ ] ネットワーク設定

- [ ] **Nginx設定**
  - [ ] `nginx/nginx.conf` 作成
  - [ ] リバースプロキシ設定（/api → backend:8080）
  - [ ] 静的ファイル配信（React SPA）
  - [ ] SSL/TLS設定（Let's Encrypt）

#### 10.3 CI/CD設定（オプション）

- [ ] **GitHub Actions設定**
  - [ ] `.github/workflows/ci.yml` 作成
  - [ ] ビルド・テスト自動実行
  - [ ] Dockerイメージビルド
  - [ ] デプロイ自動化（将来対応）

#### 10.4 本番デプロイ

- [ ] **環境変数設定**
  - [ ] `.env.prod` 作成
  - [ ] DB接続情報、JWT秘密鍵など設定

- [ ] **デプロイ実行**
  - [ ] `docker compose -f docker-compose.prod.yml up -d` でデプロイ
  - [ ] 動作確認
  - [ ] ログ確認

---

## 4. 進捗管理

### 4.1 進捗状況

| フェーズ | 進捗 | 備考 |
|--------|------|------|
| Phase 1: プロジェクト基盤 | 100% | ✅ 完了 (2025-11-05) |
| Phase 2: データベース設定 | 100% | ✅ 完了 (2025-11-06) |
| Phase 3: ドメイン層 | 100% | ✅ 完了 (2025-11-12) |
| Phase 4: リポジトリ層 | 100% | ✅ 完了 (2025-11-12) |
| Phase 5: サービス層 | 0% | ⏳ 次のタスク |
| Phase 6: セキュリティ層 | 0% | 未着手 |
| Phase 7: API層（バックエンド） | 0% | 未着手 |
| Phase 8: フロントエンド | 0% | 未着手 |
| Phase 9: テスト | 0% | 未着手 |
| Phase 10: ドキュメント・デプロイ | 0% | 未着手 |

**全体進捗**: 40% (4/10フェーズ完了)

### 4.2 マイルストーン

- [x] **マイルストーン1（Week 1）**: Phase 1-3完了（プロジェクト基盤、DB、ドメイン層） ✅ 完了
- [x] **マイルストーン2（Week 2）**: Phase 4-5完了（リポジトリ層、サービス層） 🔄 進行中 (Phase 4完了、Phase 5は次のタスク)
- [ ] **マイルストーン3（Week 3）**: Phase 6-7完了（セキュリティ層、API層）
- [ ] **マイルストーン4（Week 4）**: Phase 8-9完了（フロントエンド、テスト）
- [ ] **マイルストーン5（Week 5）**: Phase 10完了、MVP完成

### 4.3 今週のタスク

**Week 2（現在）**:
- [x] Phase 4: リポジトリ層 ✅ 完了
- [ ] Phase 5: サービス層 ⏳ 次のタスク

### 4.4 ブロッカー（障害）

現在のブロッカー: なし

---

## 参考リンク

- [QUICK_START.md](./QUICK_START.md) - クイックスタートガイド
- [ARCHITECTURE.md](../architecture/ARCHITECTURE.md) - システムアーキテクチャ
- [DATA_DESIGN.md](../architecture/DATA_DESIGN.md) - データベース設計
- [PACKAGE_DESIGN.md](../architecture/PACKAGE_DESIGN.md) - パッケージ構造
- [API_SPECIFICATION.md](../api/API_SPECIFICATION.md) - API仕様
- [ENVIRONMENT_SETUP.md](./ENVIRONMENT_SETUP.md) - 環境構築
- [DEVELOPMENT_GUIDE.md](./DEVELOPMENT_GUIDE.md) - 開発ガイド

---

**実装頑張ってください！**

このチェックリストを活用して、着実にMVPフェーズを完成させましょう。
