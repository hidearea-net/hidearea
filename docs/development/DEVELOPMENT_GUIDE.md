# 開発ガイドライン

**プロジェクト名**: HideArea
**ドキュメントバージョン**: 1.0
**作成日**: 2025-11-04
**最終更新日**: 2025-11-04

---

## 目次

1. [開発フロー](#1-開発フロー)
2. [コーディング規約](#2-コーディング規約)
3. [テスト戦略](#3-テスト戦略)
4. [コードレビュー](#4-コードレビュー)
5. [デバッグとトラブルシューティング](#5-デバッグとトラブルシューティング)
6. [パフォーマンス最適化](#6-パフォーマンス最適化)
7. [セキュリティガイドライン](#7-セキュリティガイドライン)
8. [ドキュメント作成](#8-ドキュメント作成)

---

## 1. 開発フロー

### 1.1 機能開発の基本フロー

```
1. Issue作成
   ↓
2. ブランチ作成 (feature/xxx)
   ↓
3. 開発 + ローカルテスト
   ↓
4. コミット (Conventional Commits)
   ↓
5. プルリクエスト作成
   ↓
6. セルフレビュー
   ↓
7. テスト実行確認
   ↓
8. mainへマージ (Squash and merge)
   ↓
9. ブランチ削除
```

### 1.2 Issue駆動開発

すべての作業はIssueから開始します。

#### Issueテンプレート

**機能追加**:
```markdown
## 機能概要
<!-- 追加する機能の説明 -->

## 要件
- [ ] 要件1
- [ ] 要件2

## 実装方針
<!-- 実装アプローチ -->

## タスク
- [ ] バックエンドAPI実装
- [ ] フロントエンドUI実装
- [ ] テスト作成
- [ ] ドキュメント更新

## 受け入れ基準
- [ ] 基準1
- [ ] 基準2
```

**バグ報告**:
```markdown
## バグ概要
<!-- バグの簡潔な説明 -->

## 再現手順
1. ステップ1
2. ステップ2

## 期待される動作
<!-- 本来あるべき動作 -->

## 実際の動作
<!-- 実際に起こった動作 -->

## 環境
- OS:
- ブラウザ:
- バージョン:

## エラーログ
```
<!-- エラーメッセージやスタックトレース -->
```
```

### 1.3 ブランチ作成

Issueに対応するブランチを作成:

```bash
# 機能追加
git checkout -b feature/user-registration

# バグ修正
git checkout -b fix/login-error

# ドキュメント更新
git checkout -b docs/update-api-spec
```

### 1.4 開発とテスト

#### ローカル開発サーバー起動

```bash
# 開発環境全体を起動
./scripts/start-dev.sh

# または個別起動
cd backend && ./gradlew bootRun
cd frontend && npm run dev
```

#### 変更確認

- バックエンド: http://localhost:8080
- フロントエンド: http://localhost:5173
- H2 Console: http://localhost:8080/h2-console

### 1.5 コミット

Conventional Commits形式でコミット:

```bash
git add .
git commit -m "feat(auth): add user registration endpoint"
```

**良いコミット例**:
```
feat(auth): add user registration endpoint

- Implement POST /api/v1/auth/register
- Add email validation
- Hash password with BCrypt
- Return JWT token on success

Closes #42
```

**悪いコミット例**:
```
fix stuff
update code
work in progress
```

### 1.6 プルリクエスト

#### PRの作成

```bash
git push origin feature/user-registration
```

GitHub上でPRを作成し、以下を記載:

- **概要**: 変更内容の説明
- **変更内容**: 具体的な変更点
- **テスト**: テスト方法と結果
- **関連Issue**: `Closes #<issue番号>`

#### セルフレビュー

PRを作成したら、まず自分でレビュー:

- [ ] コードが意図通りに動作するか
- [ ] 不要なコメントやデバッグコードがないか
- [ ] テストが通過するか
- [ ] コーディング規約に準拠しているか
- [ ] ドキュメントが更新されているか

#### マージ

テストが通過し、セルフレビューが完了したら、Squash and mergeでmainにマージ。

---

## 2. コーディング規約

### 2.1 命名規則

#### Java (バックエンド)

| 要素 | 命名規則 | 例 |
|-----|---------|---|
| クラス | PascalCase | `UserService`, `AuthController` |
| インターフェース | PascalCase | `UserRepository`, `Validator` |
| メソッド | camelCase | `findUserById()`, `validateEmail()` |
| 変数 | camelCase | `userId`, `jwtToken` |
| 定数 | UPPER_SNAKE_CASE | `MAX_LOGIN_ATTEMPTS`, `JWT_EXPIRATION` |
| パッケージ | lowercase | `com.hidearea.core.service` |

**命名の原則**:
- **明確で説明的**: `u` より `user`、`temp` より `temporaryToken`
- **動詞 + 名詞**: `getUserById()`, `createProfile()`
- **boolean型**: `isActive()`, `hasPermission()`

#### TypeScript (フロントエンド)

| 要素 | 命名規則 | 例 |
|-----|---------|---|
| コンポーネント | PascalCase | `UserProfile`, `LoginForm` |
| 関数 | camelCase | `fetchUser()`, `validateForm()` |
| 変数 | camelCase | `userName`, `apiBaseUrl` |
| 定数 | UPPER_SNAKE_CASE | `API_TIMEOUT`, `MAX_FILE_SIZE` |
| 型・インターフェース | PascalCase | `User`, `AuthResponse` |
| Enum | PascalCase | `UserRole`, `ProfileType` |

**React固有**:
- **Hooksプレフィックス**: `use` → `useAuth()`, `useProfile()`
- **イベントハンドラ**: `handle` → `handleSubmit()`, `handleClick()`
- **Props型**: コンポーネント名 + `Props` → `LoginFormProps`

### 2.2 コードスタイル

#### Java

**インデント**: タブ

**クラス構成順序**:
```java
public class UserService {
    // 1. 定数
    private static final int MAX_ATTEMPTS = 5;

    // 2. フィールド
    private final UserRepository userRepository;

    // 3. コンストラクタ
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 4. publicメソッド
    public User findById(Long id) { }

    // 5. privateメソッド
    private void validateUser(User user) { }
}
```

**メソッドサイズ**: 50行以内推奨

**クラスサイズ**: 300行以内推奨

#### TypeScript

**インデント**: タブ

**インポート順序**:
```typescript
// 1. 外部ライブラリ
import React from 'react';
import axios from 'axios';

// 2. 内部モジュール（絶対パス）
import { User } from '@/types/v1/user';
import { api } from '@/services/api';

// 3. 相対パス
import { Button } from '../common/Button';
import './UserProfile.css';
```

**関数コンポーネント**:
```typescript
interface UserProfileProps {
  userId: string;
  onUpdate?: (user: User) => void;
}

export const UserProfile: React.FC<UserProfileProps> = ({ userId, onUpdate }) => {
  // Hooks
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(false);

  // Effects
  useEffect(() => {
    fetchUser();
  }, [userId]);

  // Handlers
  const handleSubmit = async (e: React.FormEvent) => {
    // ...
  };

  // Render
  if (loading) return <Spinner />;

  return (
    <div>{/* JSX */}</div>
  );
};
```

### 2.3 コメント

#### 必須のコメント

- **複雑なロジック**: アルゴリズムの説明
- **非自明な処理**: なぜその実装を選んだか
- **制約・注意点**: 「XXXの場合のみ動作」など
- **TODOコメント**: 将来の改善点

```java
/**
 * ユーザーのパスワードをBCryptでハッシュ化します。
 * ストレングス10を使用（セキュリティと性能のバランス）。
 *
 * @param rawPassword 平文パスワード
 * @return ハッシュ化されたパスワード
 * @throws IllegalArgumentException パスワードが空の場合
 */
public String hashPassword(String rawPassword) {
    // BCryptのストレングス10を使用
    // これにより2^10回のハッシュ化が行われる
    return BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
}
```

#### 不要なコメント

- **自明な処理**: `// ユーザーを取得` → コードで明らか
- **コメントアウトされたコード**: 削除する（Git履歴に残る）
- **古い・誤ったコメント**: コードと矛盾するコメントは有害

### 2.4 エラーハンドリング

#### バックエンド

**カスタム例外**:
```java
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId) {
        super("User not found: " + userId);
    }
}
```

**グローバルエラーハンドラ**:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralError(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred"
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

#### フロントエンド

**API呼び出しエラーハンドリング**:
```typescript
export const fetchUser = async (userId: string): Promise<User> => {
  try {
    const response = await api.get<User>(`/api/v1/users/${userId}`);
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      if (error.response?.status === 404) {
        throw new Error('User not found');
      }
      if (error.response?.status === 401) {
        throw new Error('Unauthorized');
      }
    }
    throw new Error('Failed to fetch user');
  }
};
```

---

## 3. テスト戦略

### 3.1 テストピラミッド

```
        /\
       /E2E\       ← 少数の重要フロー
      /------\
     /統合テスト\    ← 主要機能
    /----------\
   /  単体テスト  \  ← 大部分のロジック
  /--------------\
```

### 3.2 バックエンドテスト

#### 単体テスト (JUnit 5 + Mockito)

**対象**: Service層のビジネスロジック

**カバレッジ目標**: 70%以上

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void findById_UserExists_ReturnsUser() {
        // Given
        Long userId = 1L;
        User expectedUser = new User(userId, "testuser", "test@example.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        // When
        User actualUser = userService.findById(userId);

        // Then
        assertNotNull(actualUser);
        assertEquals(expectedUser.getUsername(), actualUser.getUsername());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void findById_UserNotFound_ThrowsException() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.findById(userId));
    }
}
```

#### 統合テスト (Spring Boot Test + Testcontainers)

**対象**: Controller → Service → Repository の統合動作

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createUser_ValidRequest_ReturnsCreated() {
        // Given
        CreateUserRequest request = new CreateUserRequest("testuser", "test@example.com", "password");

        // When
        ResponseEntity<UserResponse> response = restTemplate.postForEntity(
            "/api/v1/users",
            request,
            UserResponse.class
        );

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getUsername());
    }
}
```

#### APIテスト (REST Assured)

**対象**: REST APIエンドポイント

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class AuthApiTest {

    @Test
    void login_ValidCredentials_ReturnsToken() {
        given()
            .contentType(ContentType.JSON)
            .body(new LoginRequest("testuser", "password"))
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("expiresIn", equalTo(86400));
    }
}
```

### 3.3 フロントエンドテスト

#### 単体テスト (Vitest)

**対象**: ユーティリティ関数、カスタムHooks

**カバレッジ目標**: 60%以上

```typescript
import { describe, it, expect } from 'vitest';
import { formatDate } from '@/utils/dateUtils';

describe('formatDate', () => {
  it('formats ISO date to YYYY-MM-DD', () => {
    const isoDate = '2025-11-04T10:30:00Z';
    expect(formatDate(isoDate)).toBe('2025-11-04');
  });

  it('handles invalid date', () => {
    expect(formatDate('invalid')).toBe('Invalid Date');
  });
});
```

#### コンポーネントテスト (React Testing Library)

**対象**: Reactコンポーネント

```typescript
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { LoginForm } from './LoginForm';

describe('LoginForm', () => {
  it('renders login form', () => {
    render(<LoginForm onSubmit={vi.fn()} />);

    expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /login/i })).toBeInTheDocument();
  });

  it('calls onSubmit with form data', async () => {
    const handleSubmit = vi.fn();
    render(<LoginForm onSubmit={handleSubmit} />);

    fireEvent.change(screen.getByLabelText(/username/i), {
      target: { value: 'testuser' },
    });
    fireEvent.change(screen.getByLabelText(/password/i), {
      target: { value: 'password' },
    });
    fireEvent.click(screen.getByRole('button', { name: /login/i }));

    expect(handleSubmit).toHaveBeenCalledWith({
      username: 'testuser',
      password: 'password',
    });
  });
});
```

#### E2Eテスト (Playwright)

**対象**: 重要なユーザーフロー

```typescript
import { test, expect } from '@playwright/test';

test('user registration flow', async ({ page }) => {
  // 登録ページに移動
  await page.goto('http://localhost:5173/register');

  // フォーム入力
  await page.fill('input[name="username"]', 'testuser');
  await page.fill('input[name="email"]', 'test@example.com');
  await page.fill('input[name="password"]', 'SecurePass123!');
  await page.fill('input[name="confirmPassword"]', 'SecurePass123!');

  // 登録ボタンクリック
  await page.click('button[type="submit"]');

  // 成功メッセージ確認
  await expect(page.locator('.success-message')).toContainText('Registration successful');

  // ダッシュボードにリダイレクト
  await expect(page).toHaveURL('http://localhost:5173/dashboard');
});
```

### 3.4 テスト実行

#### バックエンド

```bash
cd backend

# 全テスト実行
./gradlew test

# 統合テストのみ
./gradlew integrationTest

# カバレッジレポート生成
./gradlew jacocoTestReport
# レポート: backend/build/reports/jacoco/test/html/index.html
```

#### フロントエンド

```bash
cd frontend

# 全テスト実行
npm test

# ウォッチモード
npm test -- --watch

# カバレッジレポート
npm test -- --coverage
# レポート: frontend/coverage/index.html
```

---

## 4. コードレビュー

### 4.1 セルフレビューチェックリスト

PRを作成したら、まず自分でレビュー:

#### 機能性
- [ ] 要件を満たしているか
- [ ] エッジケースを考慮しているか
- [ ] エラーハンドリングが適切か

#### コード品質
- [ ] 命名が明確か
- [ ] 重複コードがないか
- [ ] 関数/メソッドのサイズが適切か（50行以内）
- [ ] コメントが適切か（過不足なく）

#### テスト
- [ ] テストが通過するか
- [ ] テストカバレッジが十分か
- [ ] エッジケースのテストがあるか

#### セキュリティ
- [ ] 入力値の検証があるか
- [ ] 認証・認可が適切か
- [ ] 機密情報がハードコードされていないか

#### パフォーマンス
- [ ] N+1クエリがないか
- [ ] 不要なループがないか
- [ ] キャッシュが適切に使われているか

#### ドキュメント
- [ ] APIドキュメントが更新されているか
- [ ] READMEが更新されているか（必要な場合）

### 4.2 レビューの観点

#### コードの可読性

**Good**:
```java
public User findActiveUserByEmail(String email) {
    return userRepository.findByEmailAndActive(email, true)
        .orElseThrow(() -> new UserNotFoundException("Active user not found: " + email));
}
```

**Bad**:
```java
public User find(String e) {
    User u = userRepository.findByEmailAndActive(e, true).get();
    return u;
}
```

#### エラーハンドリング

**Good**:
```typescript
const handleSubmit = async (data: FormData) => {
  setLoading(true);
  setError(null);

  try {
    await createUser(data);
    navigate('/dashboard');
  } catch (err) {
    setError(err instanceof Error ? err.message : 'An error occurred');
  } finally {
    setLoading(false);
  }
};
```

**Bad**:
```typescript
const handleSubmit = async (data: FormData) => {
  await createUser(data); // エラー処理なし
  navigate('/dashboard');
};
```

---

## 5. デバッグとトラブルシューティング

### 5.1 バックエンドデバッグ

#### ログレベル設定

**`application-dev.yml`**:
```yaml
logging:
  level:
    com.hidearea: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

#### IntelliJ IDEAデバッグ

1. ブレークポイント設定
2. `Debug 'HideAreaApplication'` で起動
3. ステップ実行で変数確認

#### cURLでのAPI動作確認

```bash
# ユーザー登録
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password"}'

# ログイン
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password"}'

# 認証付きリクエスト
curl http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer <token>"
```

### 5.2 フロントエンドデバッグ

#### React Developer Tools

ブラウザ拡張機能でコンポーネント階層とStateを確認。

#### Console.log

開発環境のみ有効化:

```typescript
const debugLog = (message: string, data?: any) => {
  if (import.meta.env.DEV) {
    console.log(`[DEBUG] ${message}`, data);
  }
};
```

#### Network監視

ブラウザDevToolsのNetworkタブでAPI通信を確認。

### 5.3 よくある問題と解決策

#### CORS エラー

**症状**: `Access-Control-Allow-Origin` エラー

**解決**:
```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:5173")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```

#### JWT トークン期限切れ

**症状**: 401 Unauthorized

**解決**: フロントエンドでトークンリフレッシュ実装

```typescript
api.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 401) {
      // トークンリフレッシュまたは再ログイン
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

---

## 6. パフォーマンス最適化

### 6.1 バックエンド最適化

#### データベースクエリ最適化

**N+1問題の回避**:
```java
// Bad: N+1クエリ発生
public List<UserDTO> getAllUsers() {
    List<User> users = userRepository.findAll();
    return users.stream()
        .map(user -> new UserDTO(user, user.getProfiles())) // 各userごとにクエリ
        .toList();
}

// Good: Fetch Joinで一括取得
public List<UserDTO> getAllUsers() {
    List<User> users = userRepository.findAllWithProfiles();
    return users.stream()
        .map(user -> new UserDTO(user, user.getProfiles()))
        .toList();
}
```

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.profiles")
    List<User> findAllWithProfiles();
}
```

#### インデックス追加

```sql
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_profiles_user_id ON user_profiles(user_id);
```

### 6.2 フロントエンド最適化

#### コード分割 (Code Splitting)

```typescript
// 遅延ロード
const Dashboard = lazy(() => import('./pages/Dashboard'));
const Profile = lazy(() => import('./pages/Profile'));

function App() {
  return (
    <Suspense fallback={<Spinner />}>
      <Routes>
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/profile" element={<Profile />} />
      </Routes>
    </Suspense>
  );
}
```

#### メモ化

```typescript
// useMemoで計算結果をキャッシュ
const sortedUsers = useMemo(() => {
  return users.sort((a, b) => a.name.localeCompare(b.name));
}, [users]);

// useCallbackで関数をメモ化
const handleUserClick = useCallback((userId: string) => {
  navigate(`/users/${userId}`);
}, [navigate]);
```

---

## 7. セキュリティガイドライン

### 7.1 認証・認可

#### パスワード要件

- 最小長: 8文字
- 大文字・小文字・数字を含む
- BCryptでハッシュ化（ストレングス10）

```java
public void validatePassword(String password) {
    if (password.length() < 8) {
        throw new IllegalArgumentException("Password must be at least 8 characters");
    }
    if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")) {
        throw new IllegalArgumentException("Password must contain uppercase, lowercase, and digit");
    }
}
```

#### JWTトークン管理

- 有効期限: 24時間
- リフレッシュトークン: 将来実装
- HTTPOnly Cookie または LocalStorage

### 7.2 入力検証

#### バックエンド

```java
@PostMapping("/users")
public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
    // @Validでバリデーション自動実行
}
```

```java
public class CreateUserRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
```

#### フロントエンド

```typescript
const validateEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

const validateForm = (data: FormData): string[] => {
  const errors: string[] = [];

  if (!data.username || data.username.length < 3) {
    errors.push('Username must be at least 3 characters');
  }

  if (!validateEmail(data.email)) {
    errors.push('Invalid email format');
  }

  return errors;
};
```

### 7.3 XSS対策

Reactは自動的にエスケープするが、`dangerouslySetInnerHTML`は避ける。

**Bad**:
```typescript
<div dangerouslySetInnerHTML={{ __html: userInput }} />
```

**Good**:
```typescript
<div>{userInput}</div>
```

### 7.4 CSRF対策（将来実装）

Double Submit Cookie パターンを採用予定。

---

## 8. ドキュメント作成

### 8.1 コードドキュメント

#### Javadoc

```java
/**
 * ユーザー管理サービス。
 * ユーザーのCRUD操作と認証関連の処理を提供します。
 */
@Service
public class UserService {

    /**
     * ユーザーIDでユーザーを検索します。
     *
     * @param id ユーザーID
     * @return ユーザー情報
     * @throws UserNotFoundException ユーザーが存在しない場合
     */
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }
}
```

#### TSDoc

```typescript
/**
 * ユーザー情報を取得します。
 *
 * @param userId - ユーザーID
 * @returns ユーザー情報を含むPromise
 * @throws {Error} ユーザーが見つからない場合
 *
 * @example
 * ```typescript
 * const user = await fetchUser('123');
 * console.log(user.username);
 * ```
 */
export const fetchUser = async (userId: string): Promise<User> => {
  // ...
};
```

### 8.2 API ドキュメント

新しいAPIエンドポイントを追加したら、`docs/api/API_SPECIFICATION.md` を更新。

### 8.3 アーキテクチャドキュメント

設計変更があった場合、該当ドキュメントを更新:

- `docs/architecture/ARCHITECTURE.md`
- `docs/architecture/DATA_DESIGN.md`
- など

---

## まとめ

本開発ガイドラインでは、HideAreaプロジェクトの開発フロー、コーディング規約、テスト戦略、デバッグ方法、パフォーマンス最適化、セキュリティガイドライン、ドキュメント作成方法を定義しました。

**開発の原則**:
- **シンプルさ**: YAGNIの原則、過度な抽象化を避ける
- **一貫性**: 命名規則とコーディングスタイルの統一
- **テスタビリティ**: 十分なテストカバレッジ
- **セキュリティ**: 入力検証と認証・認可の徹底
- **ドキュメント**: コードと同期したドキュメント維持

このガイドラインに従うことで、保守性と拡張性の高いコードベースを維持できます。

---

**次のステップ**: [環境セットアップガイド](./ENVIRONMENT_SETUP.md) を参照し、開発環境を構築してください。
