# 設計決定記録 (Design Decisions)

このドキュメントは、HideAreaプロジェクトにおける重要な設計決定を一元管理します。

**最終更新日**: 2025-11-05

---

## 📋 決定事項一覧

### 1. データモデル設計

#### 1.1 ユーザーとプロフィールのemail管理

**決定**: `User`エンティティから`email`フィールドを削除し、`Profile`エンティティのみに保持する。

**理由**:
- プロフィールごとに異なるメールアドレスを使用するユースケース（ビジネス用、個人用など）に対応
- 認証には`username`を使用するため、`User`レベルでのemail管理は不要
- データの重複を避け、Single Source of Truthを実現

**影響範囲**:
- `User`エンティティ定義
- 認証フロー（usernameベース）
- プロフィール作成時のバリデーション（email必須）

**参照**: DATA_DESIGN.md

---

#### 1.2 プロフィール階層の削除動作

**決定**: 親プロフィール削除時、子プロフィールを削除するかどうかを**ユーザーが選択可能**とする。

**実装方法**:
```
DELETE /api/v1/profiles/{id}?cascadeDelete=true
```

- `cascadeDelete=true`: 子プロフィールも一緒に削除
- `cascadeDelete=false` (デフォルト): 子プロフィールをルートプロフィールに変換

**理由**:
- ユースケースによって要件が異なる
  - 「部署ごと削除したい」→ cascade
  - 「部署は解散するが、個人プロフィールは残したい」→ no cascade
- 柔軟性を持たせることでユーザー体験向上

**影響範囲**:
- `DELETE /api/v1/profiles/{id}` API
- `ProfileService.deleteProfile()` メソッド
- `ProfileRelation`の更新ロジック

**参照**: API_SPECIFICATION.md, DATA_DESIGN.md

---

#### 1.3 階層深さの最大値

**決定**: 3階層を**推奨**とし、バリデーションでのチェックは実施しない。

**理由**:
- 技術的制約ではなく、UX上の推奨事項
- 深い階層はUI表示が複雑になるためユーザーに推奨しないが、強制はしない
- 将来的に深い階層が必要になる可能性に対応

**影響範囲**:
- UIでの警告表示（「3階層以上は推奨されません」など）
- バリデーションロジックには影響なし

**参照**: DATA_DESIGN.md

---

#### 1.4 Enumのテーブル管理

**決定**: プロフィールタイプ、権限種別などのEnumを**データベーステーブルで管理**する。

**実装方法**:
```sql
-- プロフィールタイプマスタ
CREATE TABLE profile_types (
    code VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    display_order INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 権限マスタ
CREATE TABLE permissions (
    code VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    display_order INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**理由**:
- コードのデプロイなしで新しいタイプを追加可能
- マスタデータの管理（表示順、有効/無効切り替え）が容易
- 多言語対応が将来的に容易
- 管理画面でのマスタメンテナンスが可能

**影響範囲**:
- データベーススキーマ設計
- マスタデータ管理API追加（管理者用）
- 初期データ投入（Flyway migration）

**参照**: DATA_DESIGN.md

---

### 2. API設計

#### 2.1 トークンリフレッシュ機構

**決定**: **フェーズ1（MVP）でリフレッシュトークンを実装する**。

**実装仕様**:
- **アクセストークン**: 有効期限 1時間
- **リフレッシュトークン**: 有効期限 30日
- **エンドポイント**: `POST /api/v1/auth/refresh`
- **セキュリティ**: リフレッシュトークンローテーション方式

**理由**:
- 24時間ごとの再ログインはUX上受け入れられない
- 後からの追加は既存ユーザー移行が必要で困難
- 実装コスト（2-3日）は許容範囲
- アクセストークンを短命化してセキュリティ向上

**実装例**:
```java
@PostMapping("/refresh")
public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
    RefreshToken refreshToken = refreshTokenService.verifyAndGet(request.getRefreshToken());
    String newAccessToken = jwtService.generateAccessToken(refreshToken.getUser());
    String newRefreshToken = refreshTokenService.rotate(refreshToken);
    return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshToken));
}
```

**影響範囲**:
- `RefreshToken`エンティティ追加
- `POST /api/v1/auth/refresh` API追加
- フロントエンドのAxios interceptor実装
- JWT生成ロジックの修正（有効期限短縮）

**参照**: API_SPECIFICATION.md, IMPLEMENTATION_CHECKLIST.md

---

#### 2.2 ページネーション実装

**決定**: **オフセットベース**（`page`/`size`）を基本実装とし、カーソルベースは将来実装とする。

**実装仕様**:
```
GET /api/v1/profiles?page=0&size=20&sort=createdAt,desc
```

**レスポンス形式** (Spring Data JPA標準):
```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {"sorted": true}
  },
  "totalPages": 10,
  "totalElements": 200,
  "last": false,
  "first": true
}
```

**理由**:
- Spring Data JPAの`Pageable`をそのまま使用可能（実装簡単）
- ページ番号表示UIが直感的
- 任意ページへのジャンプが可能
- 現時点のデータ量では性能問題なし

**将来実装（フェーズ2以降）**:
- リアルタイムフィード機能でカーソルベース追加
- 無限スクロールUI対応

**影響範囲**:
- 全ての一覧取得API
- フロントエンドのページネーションコンポーネント

**参照**: API_SPECIFICATION.md

---

#### 2.3 APIバージョニング後方互換性

**決定**: APIバージョンは**後方互換性を維持**する。

**ポリシー**:
- メジャーバージョン（v1 → v2）: 破壊的変更を含む可能性あり
- 旧バージョンは**最低12ヶ月間サポート**
- 非推奨化（Deprecation）の事前通知: 6ヶ月前
- レスポンスヘッダーで警告: `Deprecation: true`, `Sunset: 2026-12-31`

**バージョン管理方法**:
- URL-pathベース: `/api/v1/`, `/api/v2/`
- 同時実行: v1とv2を並行稼働

**理由**:
- クライアントアプリの更新強制を避ける
- 段階的な移行を可能にする
- サードパーティ統合の安定性確保

**影響範囲**:
- API設計全般
- バージョニング戦略
- ドキュメント管理

**参照**: PACKAGE_DESIGN.md, API_SPECIFICATION.md

---

#### 2.4 ユーザー情報取得APIの権限

**決定**: `GET /api/v1/users/{id}` は**管理者のみアクセス可能**とする。

**実装**:
```java
@GetMapping("/users/{id}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
    // 実装
}
```

**一般ユーザー向け**:
- 自分の情報取得: `GET /api/v1/users/me`
- 他ユーザーのプロフィール閲覧: `GET /api/v1/profiles/{id}`（公開設定による）

**理由**:
- プライバシー保護（他ユーザーの情報閲覧制限）
- 権限による明確な分離

**影響範囲**:
- `GET /api/v1/users/{id}` API
- Spring Securityの権限チェック

**参照**: API_SPECIFICATION.md

---

### 3. 技術スタック

#### 3.1 UIライブラリ

**決定**: **shadcn/ui** を採用する。

**理由**:
- コンポーネント所有権（プロジェクト内にコピー、完全カスタマイズ可能）
- TypeScriptファースト、型安全性が高い
- TailwindCSSベース（既存選定と一致）
- アクセシビリティ標準装備（Radix UI）
- バンドルサイズが最小
- モダンなデザイン、ダークモード対応容易

**代替案として検討したもの**:
- Material-UI: 成熟しているが、Material Design準拠で自由度低い

**影響範囲**:
- フロントエンド全体
- コンポーネント設計
- デザインシステム

**参照**: TECH_STACK.md

---

#### 3.2 ロギングフレームワーク

**決定**: **SLF4J** (インターフェース) + **Logback** (実装) を使用する。

**理由**:
- Spring Bootのデフォルト
- 成熟した安定性
- 設定が柔軟
- パフォーマンス良好

**ログレベル**:
- 開発環境: DEBUG
- 本番環境: INFO
- エラー: ERROR

**影響範囲**:
- バックエンド全体
- ログ出力実装

**参照**: TECH_STACK.md

---

#### 3.3 テストデータベース

**決定**: **H2 Database** (インメモリ) を使用する。

**理由**:
- 高速なテスト実行
- CIパイプラインでのセットアップ不要
- PostgreSQL互換モード使用可能
- 開発者環境での依存性最小化

**設定**:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
```

**将来検討**:
- 複雑なSQL（ウィンドウ関数など）でTestcontainers併用検討

**影響範囲**:
- テスト環境設定
- `application-test.yml`

**参照**: TECH_STACK.md

---

### 4. パッケージ構成

#### 4.1 パッケージ名

**決定**: `net.hidearea.core` を正式なパッケージ名とする。

**訂正箇所**:
- PROJECT_PLAN.md に記載の `com.hidearea.core` は誤り

**理由**:
- 大多数のドキュメントで `net.hidearea` を使用
- 統一性の確保

**影響範囲**:
- 全ドキュメント
- 実装時のパッケージ命名

**参照**: PACKAGE_DESIGN.md

---

## 📚 設計決定の背景と原則

### 設計原則

1. **ユーザー体験優先**: 技術的制約よりもUXを重視
2. **段階的実装**: MVP機能を明確にし、段階的に拡張
3. **拡張性**: 将来の変更に柔軟に対応できる設計
4. **標準準拠**: Spring Boot、React標準に従い、学習コスト最小化
5. **セキュリティ**: セキュリティは後回しにせず、最初から組み込む

---

## 📝 変更履歴

| 日付 | 変更内容 | 変更者 |
|------|----------|--------|
| 2025-11-05 | 初版作成、全設計決定を統合 | - |

---

## 🔗 関連ドキュメント

- [PROJECT_PLAN.md](./PROJECT_PLAN.md) - プロジェクト全体計画
- [DATA_DESIGN.md](./DATA_DESIGN.md) - データベース設計
- [API_SPECIFICATION.md](./API_SPECIFICATION.md) - API仕様
- [TECH_STACK.md](./TECH_STACK.md) - 技術スタック
- [PACKAGE_DESIGN.md](./PACKAGE_DESIGN.md) - パッケージ設計
- [IMPLEMENTATION_CHECKLIST.md](./IMPLEMENTATION_CHECKLIST.md) - 実装チェックリスト
