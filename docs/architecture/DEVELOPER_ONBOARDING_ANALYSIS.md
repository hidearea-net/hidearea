# 開発者オンボーディングコスト分析: 案A vs 案C

**作成日**: 2025-11-04
**目的**: 新規参画者の学習コストを中心に、案A（自己参照FK）と案C（明示的な2テーブル）を比較

---

## 目次

1. [エグゼクティブサマリー](#1-エグゼクティブサマリー)
2. [開発者視点での比較](#2-開発者視点での比較)
3. [実装コードの比較](#3-実装コードの比較)
4. [学習コストの詳細分析](#4-学習コストの詳細分析)
5. [推奨案](#5-推奨案)

---

## 1. エグゼクティブサマリー

### 結論: **案Cを推奨（新規開発者の学習コスト重視の場合）**

| 評価項目 | 案A | 案C |
|---------|-----|-----|
| **初心者にとっての理解しやすさ** | ⭐⭐⭐☆☆ | ⭐⭐⭐⭐⭐ |
| **SQLスキル要求レベル** | 中級（再帰CTE必須） | 初級（JOINのみ） |
| **コード記述量** | 少ない | やや多い |
| **デバッグの容易性** | 中 | 高 |
| **ビジネスロジックの明確性** | やや不明瞭 | 非常に明確 |
| **新規参画コスト** | 中〜高 | 低 |

**重要なポイント**:
- 案Cは自己参照FKや再帰クエリを知らなくても実装可能
- テーブルが分離されているため、ビジネスロジックが明確
- ただし、テーブル数が1つ増えることで全体の構造把握には若干時間がかかる

---

## 2. 開発者視点での比較

### 2.1 新規参画時の理解プロセス

#### 案A: 自己参照FK + 中間テーブル

```
[学習ステップ]
1. users テーブルの理解 ✅（基礎知識）
2. profiles テーブルの理解 ⚠️（自己参照FKの理解が必要）
3. user_profiles テーブルの理解 ✅（多対多の基礎知識）
4. 再帰CTEの理解 ❌（高度な知識が必要）

必要な前提知識:
- 自己参照外部キー（Self-referencing FK）の概念
- 再帰クエリ（WITH RECURSIVE）の構文と動作原理
- ツリー構造データの扱い方
```

#### 案C: 明示的な2テーブル

```
[学習ステップ]
1. users テーブルの理解 ✅（基礎知識）
2. profiles テーブルの理解 ✅（単純なテーブル）
3. user_profile_relations テーブルの理解 ✅（多対多の基礎知識）
4. profile_profile_relations テーブルの理解 ✅（多対多の基礎知識）

必要な前提知識:
- 外部キー制約の基礎
- 多対多関係（中間テーブル）の理解
- 基本的なJOIN（INNER JOIN, LEFT JOIN）
```

**分析結果**:
- 案Cは**全て基礎的なRDB知識のみで理解可能**
- 案Aは**中級以上のSQLスキルが必須**

---

### 2.2 実際の開発タスクでの難易度

#### タスク1: 「ユーザーが所有する全プロフィールを取得」

**案A**:
```sql
-- シンプル（再帰不要）
SELECT p.*
FROM profiles p
INNER JOIN user_profiles up ON p.id = up.profile_id
WHERE up.user_id = 1 AND up.role = 'OWNER';
```

**案C**:
```sql
-- 同じくシンプル
SELECT p.*
FROM profiles p
INNER JOIN user_profile_relations upr ON p.id = upr.profile_id
WHERE upr.user_id = 1 AND upr.role = 'OWNER';
```

**難易度**: 両者とも同等（初級）

---

#### タスク2: 「プロフィールの子プロフィールを全て取得」

**案A**:
```sql
-- シンプル（再帰不要）
SELECT *
FROM profiles
WHERE parent_profile_id = 2;
```

**案C**:
```sql
-- 同じくシンプル
SELECT p.*
FROM profiles p
INNER JOIN profile_profile_relations ppr ON p.id = ppr.child_profile_id
WHERE ppr.parent_profile_id = 2;
```

**難易度**: 両者とも同等（初級）

---

#### タスク3: 「プロフィールの子孫を全て取得（再帰）」

**案A**:
```sql
-- ⚠️ 再帰CTEが必要（中級〜上級）
WITH RECURSIVE descendants AS (
    -- ベースケース: 直接の子
    SELECT id, parent_profile_id, profile_name, 1 as depth
    FROM profiles
    WHERE parent_profile_id = 2

    UNION ALL

    -- 再帰ケース: 子の子を取得
    SELECT p.id, p.parent_profile_id, p.profile_name, d.depth + 1
    FROM profiles p
    INNER JOIN descendants d ON p.parent_profile_id = d.id
)
SELECT * FROM descendants;
```

**案C**:
```sql
-- ⚠️ 再帰的なJOINまたはループが必要（中級）
-- ただし、application層で再帰的に取得することも可能
-- Option 1: 再帰CTE（案Aと同等の複雑さ）
WITH RECURSIVE descendants AS (
    SELECT child_profile_id as id, 1 as depth
    FROM profile_profile_relations
    WHERE parent_profile_id = 2

    UNION ALL

    SELECT ppr.child_profile_id, d.depth + 1
    FROM profile_profile_relations ppr
    INNER JOIN descendants d ON ppr.parent_profile_id = d.id
)
SELECT p.* FROM profiles p INNER JOIN descendants d ON p.id = d.id;

-- Option 2: Application層で段階的に取得（初級でも可能）
// 疑似コード
function getDescendants(profileId) {
    children = db.query("SELECT child_profile_id FROM profile_profile_relations WHERE parent_profile_id = ?", profileId);
    result = children;
    for (child in children) {
        result += getDescendants(child.child_profile_id);
    }
    return result;
}
```

**難易度**:
- 案A: 中級〜上級（再帰CTE必須）
- 案C: 中級（再帰CTE使用）または 初級（Application層で実装）

**重要**: 案Cでは、再帰クエリをApplication層に逃がせる選択肢がある

---

#### タスク4: 「プロフィールの祖先を全て取得」

**案A**:
```sql
-- ⚠️ 再帰CTEが必要（中級〜上級）
WITH RECURSIVE ancestors AS (
    SELECT id, parent_profile_id, profile_name, 0 as depth
    FROM profiles
    WHERE id = 4

    UNION ALL

    SELECT p.id, p.parent_profile_id, p.profile_name, a.depth + 1
    FROM profiles p
    INNER JOIN ancestors a ON p.id = a.parent_profile_id
)
SELECT * FROM ancestors WHERE depth > 0;
```

**案C**:
```sql
-- Option 1: 再帰CTE
WITH RECURSIVE ancestors AS (
    SELECT parent_profile_id as id, 1 as depth
    FROM profile_profile_relations
    WHERE child_profile_id = 4

    UNION ALL

    SELECT ppr.parent_profile_id, a.depth + 1
    FROM profile_profile_relations ppr
    INNER JOIN ancestors a ON ppr.child_profile_id = a.id
)
SELECT p.* FROM profiles p INNER JOIN ancestors a ON p.id = a.id;

-- Option 2: Application層で段階的に取得
function getAncestors(profileId) {
    parent = db.query("SELECT parent_profile_id FROM profile_profile_relations WHERE child_profile_id = ?", profileId).first();
    if (!parent) return [];
    return [parent] + getAncestors(parent.parent_profile_id);
}
```

**難易度**: 案Aと同様、ただし案Cは階層が浅ければApplication層でも実装しやすい

---

### 2.3 ビジネスロジックの明確性

#### 案A: 暗黙的な関係

```
profiles テーブルを見た時:
- parent_profile_id: これは何？ユーザー？プロフィール？（型を見るまで不明）
- プロフィール同士の関係が「埋め込まれている」

user_profiles テーブルを見た時:
- ユーザーとプロフィールの関係が明確

結論: 2種類の関係が「別々の場所」に存在し、統一感がない
```

#### 案C: 明示的な関係

```
user_profile_relations テーブル:
- ユーザー・プロフィール関係を管理（ロール付き）

profile_profile_relations テーブル:
- プロフィール・プロフィール関係を管理

結論: 2種類の関係が「対称的に」存在し、ビジネスロジックが明確
```

**例: 新規参画者がコードレビューする時**

```java
// 案A: ProfileRepository
public List<Profile> findChildProfiles(Long profileId) {
    // ⚠️ parent_profile_id って何のID？コメントなしでは理解しづらい
    return profileRepository.findByParentProfileId(profileId);
}

// 案C: ProfileRelationRepository
public List<Profile> findChildProfiles(Long profileId) {
    // ✅ profile_profile_relations という名前で関係性が明確
    List<Long> childIds = profileProfileRelationRepository
        .findChildProfileIdsByParentId(profileId);
    return profileRepository.findAllById(childIds);
}
```

**分析**:
- 案Cは**テーブル名とメソッド名だけで関係性が理解可能**
- 案Aは**ドメイン知識がないと理解に時間がかかる**

---

## 3. 実装コードの比較

### 3.1 エンティティ定義（JPA）

#### 案A

```java
@Entity
@Table(name = "profiles")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ⚠️ 自己参照（初心者には難解）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_profile_id")
    private Profile parentProfile;

    // ⚠️ 双方向関係（管理が複雑）
    @OneToMany(mappedBy = "parentProfile", cascade = CascadeType.ALL)
    private List<Profile> childProfiles = new ArrayList<>();

    private String profileName;
    // ... other fields
}
```

**注意点**:
- `@ManyToOne` で同じエンティティを参照（初心者には理解困難）
- 双方向関係の管理（`mappedBy`）が必要
- 循環参照のリスク（JSON serialization時など）

---

#### 案C

```java
@Entity
@Table(name = "profiles")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String profileName;
    // ... other fields

    // ✅ 自己参照なし、シンプルなエンティティ
}

// ✅ 関係を管理する別のエンティティ（明確）
@Entity
@Table(name = "profile_profile_relations")
public class ProfileProfileRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_profile_id", nullable = false)
    private Profile parentProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_profile_id", nullable = false)
    private Profile childProfile;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
```

**利点**:
- Profileエンティティは**自己参照がなく、非常にシンプル**
- 関係管理は専用エンティティに分離
- 循環参照のリスクがない

---

### 3.2 Repository実装

#### 案A

```java
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    // ⚠️ parent_profile_id の意味を理解している必要がある
    List<Profile> findByParentProfileId(Long parentProfileId);

    // ⚠️ parent_profile_id IS NULL で「ルート」を表現（暗黙的）
    List<Profile> findByParentProfileIdIsNull();
}
```

---

#### 案C

```java
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    // ✅ シンプル（階層管理の責務なし）
}

// ✅ 関係管理専用のRepository（責務が明確）
public interface ProfileProfileRelationRepository
    extends JpaRepository<ProfileProfileRelation, Long> {

    // 親IDから子プロフィールを取得
    List<ProfileProfileRelation> findByParentProfileId(Long parentProfileId);

    // 子IDから親プロフィールを取得
    Optional<ProfileProfileRelation> findByChildProfileId(Long childProfileId);

    // ルートプロフィール（親を持たない）を取得
    @Query("SELECT DISTINCT p FROM Profile p " +
           "WHERE p.id NOT IN (SELECT r.childProfile.id FROM ProfileProfileRelation r)")
    List<Profile> findRootProfiles();
}
```

**利点**:
- 責務が分離され、各Repositoryの役割が明確
- メソッド名で処理内容が自明

---

### 3.3 Service層の実装

#### タスク: 「プロフィールの子プロフィール一覧を取得」

**案A**:

```java
@Service
public class ProfileService {
    private final ProfileRepository profileRepository;

    public List<ProfileResponse> getChildProfiles(Long profileId) {
        // ⚠️ 自己参照FKを直接使用（ドメイン知識が必要）
        List<Profile> children = profileRepository.findByParentProfileId(profileId);
        return children.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
}
```

**案C**:

```java
@Service
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final ProfileProfileRelationRepository relationRepository;

    public List<ProfileResponse> getChildProfiles(Long profileId) {
        // ✅ 関係テーブルから関係を取得（明示的）
        List<ProfileProfileRelation> relations =
            relationRepository.findByParentProfileId(profileId);

        // ✅ 関係からプロフィールIDを抽出
        List<Long> childIds = relations.stream()
            .map(r -> r.getChildProfile().getId())
            .collect(Collectors.toList());

        // ✅ プロフィールを取得
        List<Profile> children = profileRepository.findAllById(childIds);
        return children.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
}
```

**分析**:
- 案A: コード量は少ないが、`parent_profile_id`の意味を理解していないと実装不可
- 案C: コード量は多いが、各ステップの意図が明確で、初心者でも理解可能

---

#### タスク: 「プロフィールの全子孫を取得（再帰）」

**案A**:

```java
public List<ProfileResponse> getAllDescendants(Long profileId) {
    // ⚠️ ネイティブクエリで再帰CTE（SQL中級以上のスキル必須）
    String sql = """
        WITH RECURSIVE descendants AS (
            SELECT id, parent_profile_id, profile_name, 1 as depth
            FROM profiles
            WHERE parent_profile_id = :profileId

            UNION ALL

            SELECT p.id, p.parent_profile_id, p.profile_name, d.depth + 1
            FROM profiles p
            INNER JOIN descendants d ON p.parent_profile_id = d.id
        )
        SELECT * FROM descendants
        """;

    Query query = entityManager.createNativeQuery(sql, Profile.class);
    query.setParameter("profileId", profileId);

    @SuppressWarnings("unchecked")
    List<Profile> descendants = query.getResultList();
    return descendants.stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
}
```

**案C Option 1（SQL再帰CTE）**:

```java
public List<ProfileResponse> getAllDescendants(Long profileId) {
    // 案Aと同様の複雑さ（再帰CTE）
    String sql = """
        WITH RECURSIVE descendants AS (
            SELECT child_profile_id as id, 1 as depth
            FROM profile_profile_relations
            WHERE parent_profile_id = :profileId

            UNION ALL

            SELECT ppr.child_profile_id, d.depth + 1
            FROM profile_profile_relations ppr
            INNER JOIN descendants d ON ppr.parent_profile_id = d.id
        )
        SELECT p.* FROM profiles p
        INNER JOIN descendants d ON p.id = d.id
        """;

    Query query = entityManager.createNativeQuery(sql, Profile.class);
    query.setParameter("profileId", profileId);

    @SuppressWarnings("unchecked")
    List<Profile> descendants = query.getResultList();
    return descendants.stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
}
```

**案C Option 2（Javaで再帰実装）**:

```java
public List<ProfileResponse> getAllDescendants(Long profileId) {
    // ✅ Javaの再帰関数（初心者でも理解しやすい）
    List<Profile> allDescendants = new ArrayList<>();
    collectDescendants(profileId, allDescendants);
    return allDescendants.stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
}

private void collectDescendants(Long parentId, List<Profile> accumulator) {
    // 直接の子を取得
    List<ProfileProfileRelation> relations =
        relationRepository.findByParentProfileId(parentId);

    for (ProfileProfileRelation relation : relations) {
        Profile child = relation.getChildProfile();
        accumulator.add(child);

        // 子の子を再帰的に取得
        collectDescendants(child.getId(), accumulator);
    }
}
```

**分析**:
- 案A: 再帰CTEが必須（SQL中級以上）
- 案C Option 1: 同じく再帰CTEが必要
- 案C Option 2: **Javaの再帰関数で実装可能（初心者でも理解可能）**

**重要**: 案Cでは、SQLではなくJavaで再帰を実装する選択肢がある

---

## 4. 学習コストの詳細分析

### 4.1 必要な技術スキルレベル

#### 案A

| スキル | レベル | 理由 |
|--------|--------|------|
| SQL基礎 | 初級 | SELECT, JOIN, WHERE |
| 外部キー制約 | 初級 | 基本的なFK |
| 自己参照FK | **中級** | **同じテーブルを参照する概念** |
| 再帰クエリ（WITH RECURSIVE） | **中級〜上級** | **CTEの理解と再帰処理** |
| JPA/Hibernate | 中級 | `@ManyToOne`での自己参照 |
| ツリー構造データ | 中級 | 親子関係の理解 |

**学習曲線**: やや急（自己参照と再帰クエリで躓きやすい）

---

#### 案C

| スキル | レベル | 理由 |
|--------|--------|------|
| SQL基礎 | 初級 | SELECT, JOIN, WHERE |
| 外部キー制約 | 初級 | 基本的なFK |
| 多対多関係 | 初級 | 中間テーブルの基礎 |
| 再帰処理（Java） | 初級〜中級 | プログラミング基礎 |
| JPA/Hibernate | 初級 | シンプルな`@ManyToOne` |
| ツリー構造データ | 初級〜中級 | 関係テーブルで表現 |

**学習曲線**: 緩やか（全て基礎的な知識で対応可能）

---

### 4.2 初学者がハマりやすいポイント

#### 案A

**ポイント1: 自己参照FKの理解**

```sql
-- ❓「parent_profile_id はどのテーブルを参照？」
FOREIGN KEY (parent_profile_id) REFERENCES profiles(id)
```

**初学者の混乱**:
- 「profilesテーブル自身を参照している」という概念が直感的でない
- 「無限ループにならないの?」という疑問が生じやすい

**ポイント2: 再帰CTEの構文**

```sql
WITH RECURSIVE descendants AS (
    -- ベースケース
    ...
    UNION ALL
    -- 再帰ケース
    ...
)
```

**初学者の混乱**:
- `WITH RECURSIVE`の動作原理を理解するのが困難
- `UNION ALL`の意味
- 無限ループを防ぐ方法

**ポイント3: JPAでの双方向関係**

```java
@OneToMany(mappedBy = "parentProfile")
private List<Profile> childProfiles;
```

**初学者の混乱**:
- `mappedBy`の意味と必要性
- 双方向関係の同期方法
- 循環参照によるStackOverflowError

---

#### 案C

**ポイント1: テーブルが増える**

**初学者の混乱**:
- 「なぜprofilesテーブルだけで済まないの?」
- ただし、これは「多対多関係の中間テーブル」という基礎知識で解決可能

**ポイント2: JOIN が増える**

**初学者の混乱**:
- 「なぜprofile_profile_relationsを経由する必要があるの?」
- ただし、これは「中間テーブルを介したJOIN」という基礎知識で解決可能

**結論**: 案Cのポイントは全て**RDB基礎知識で解決可能**

---

### 4.3 デバッグのしやすさ

#### 案A

```sql
-- ❓ 意図しない階層構造ができた時のデバッグ
SELECT id, parent_profile_id, profile_name
FROM profiles
ORDER BY parent_profile_id NULLS FIRST, id;
```

**デバッグの困難さ**:
- parent_profile_idの値を見ても、「どのプロフィール?」が一目で分からない
- 階層構造を可視化するには再帰クエリが必要

---

#### 案C

```sql
-- ✅ 意図しない関係ができた時のデバッグ
SELECT
    ppr.id,
    parent.profile_name AS parent_name,
    child.profile_name AS child_name
FROM profile_profile_relations ppr
INNER JOIN profiles parent ON ppr.parent_profile_id = parent.id
INNER JOIN profiles child ON ppr.child_profile_id = child.id;
```

**デバッグの容易さ**:
- 関係が明示的に見える（親の名前、子の名前）
- 異常なデータがあれば一目で分かる
- 再帰クエリなしで全関係を可視化可能

---

### 4.4 ドキュメント依存度

#### 案A

**ドキュメントが必須な項目**:
- `parent_profile_id`が何を表すか（プロフィール自身への参照）
- 階層の深さの制限（ビジネスルール）
- 再帰CTEの使い方
- ルートプロフィールの定義（`parent_profile_id IS NULL`）

**ドキュメントがないと**:
- コードを読むだけでは仕様が理解できない
- 新規参画者は経験者に質問が必要

---

#### 案C

**ドキュメントが必須な項目**:
- 階層の深さの制限（ビジネスルール）

**ドキュメントがなくても**:
- テーブル名とカラム名から仕様が推測可能
- `user_profile_relations`: ユーザー・プロフィール関係
- `profile_profile_relations`: プロフィール・プロフィール関係
- 新規参画者でもコードから理解可能

---

## 5. 推奨案

### 5.1 結論: **案Cを推奨（新規開発者の学習コスト重視の場合）**

### 5.2 推奨理由

#### ✅ メリット1: 自己参照FKが不要

- 初心者でも理解しやすい標準的な外部キー制約のみ
- 「同じテーブルを参照する」という複雑な概念が不要

#### ✅ メリット2: 再帰クエリを回避可能

- SQL再帰CTEを知らなくても実装可能
- Javaの再帰関数で代替可能（プログラミング基礎知識で対応）

#### ✅ メリット3: ビジネスロジックが明確

- `user_profile_relations`: ユーザー・プロフィール関係
- `profile_profile_relations`: プロフィール・プロフィール関係
- テーブル名だけで責務が理解可能

#### ✅ メリット4: デバッグが容易

- 関係を単純なSELECT + JOINで可視化可能
- 異常なデータを発見しやすい

#### ✅ メリット5: ドキュメント依存度が低い

- コードとスキーマから仕様が推測可能
- 新規参画者の自律的な学習を促進

---

### 5.3 案Cのデメリットと対策

#### ❌ デメリット1: テーブル数が増える

**対策**:
- テーブル数が増えること自体は問題ではない（責務分離の原則）
- むしろ、各テーブルの役割が明確になる

#### ❌ デメリット2: コード記述量が増える

**対策**:
- ServiceやRepositoryにヘルパーメソッドを用意
- コード量は増えるが、可読性と保守性は向上

#### ❌ デメリット3: パフォーマンス（JOIN回数増加）

**対策**:
- MVPフェーズでは問題にならない（階層が浅い）
- 将来的にClosure Tableを追加して最適化可能

---

### 5.4 案A vs 案C の最終比較表

| 評価項目 | 案A | 案C | 重視すべき観点 |
|---------|-----|-----|----------------|
| **初心者理解度** | ⭐⭐⭐☆☆ | ⭐⭐⭐⭐⭐ | 新規参画コスト |
| **コード記述量** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐☆☆ | 開発効率 |
| **可読性** | ⭐⭐⭐☆☆ | ⭐⭐⭐⭐⭐ | 保守性 |
| **デバッグ容易性** | ⭐⭐⭐☆☆ | ⭐⭐⭐⭐⭐ | トラブルシューティング |
| **パフォーマンス** | ⭐⭐⭐⭐☆ | ⭐⭐⭐☆☆ | スケーラビリティ |
| **DBMS互換性** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 移行容易性 |
| **ドキュメント依存** | ⭐⭐☆☆☆ | ⭐⭐⭐⭐☆ | 自律学習 |
| **総合スコア** | **22/35** | **30/35** | - |

---

### 5.5 推奨採用パターン

#### パターン1: 新規開発者が多い、または初心者中心のチーム

**推奨**: **案C**

**理由**:
- 学習コストが低い
- ドキュメントがなくてもコードから理解可能
- デバッグが容易

---

#### パターン2: 経験豊富な開発者のみ、またはSQLスキルが高いチーム

**推奨**: **案A**

**理由**:
- コード記述量が少ない
- 再帰CTEを使いこなせる
- 階層クエリのパフォーマンスが若干良い

---

#### パターン3: 将来的にチームが拡大する可能性がある

**推奨**: **案C**

**理由**:
- 新規参画者のオンボーディングコストが低い
- ビジネスロジックが明確で、引き継ぎやすい

---

## 6. 実装ロードマップ（案C採用時）

### Phase 1: MVP実装

```
実装内容:
1. profiles テーブル（階層関係なし、シンプル）
2. user_profile_relations テーブル
3. profile_profile_relations テーブル
4. 基本的なCRUD操作
5. 階層取得（Javaで再帰実装）
```

### Phase 2: パフォーマンス監視

```
監視項目:
- 階層取得クエリの実行時間
- JOIN回数とデータ量
- N+1問題の有無
```

### Phase 3: 最適化（必要に応じて）

```
実施内容:
- Closure Tableの追加（profile_closure）
- 階層取得クエリの最適化
- キャッシュ戦略の導入
```

---

## 7. まとめ

### 新規参画コスト重視の場合、**案Cを強く推奨**

**決定的な理由**:

1. **自己参照FKが不要** → 初心者でも理解可能
2. **再帰CTEを回避可能** → SQLスキルがなくてもJavaで実装可能
3. **ビジネスロジックが明確** → テーブル名から仕様が推測可能
4. **デバッグが容易** → 異常データを発見しやすい
5. **ドキュメント依存度が低い** → 新規参画者の自律学習を促進

**トレードオフ**:
- コード記述量がやや増える
- JOINが1回増える（パフォーマンスへの影響は軽微）

**結論**:
初めてソフトウェア開発に携わる人でも理解しやすく、新規参画コストを大幅に低減できる案Cが、長期的な保守性とチーム拡張性の観点から最適です。

---

**次のステップ**:
案Cで実装を進め、将来的にパフォーマンス要件に応じてClosure Table（案D相当）への拡張を検討することを推奨します。
