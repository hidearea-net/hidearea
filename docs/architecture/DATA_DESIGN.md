# データ設計書

**最終更新日**: 2025-11-04

---

## 目次

1. [エンティティ設計](#1-エンティティ設計)
2. [ER図](#2-er図)
3. [リポジトリパターン](#3-リポジトリパターン)
4. [インデックス戦略](#4-インデックス戦略)
5. [データベーススキーマ](#5-データベーススキーマ)
6. [ユースケース例](#6-ユースケース例)

---

## 1. エンティティ設計

### 1.1 User（ユーザー）エンティティ

#### テーブル定義

| カラム名 | 型 | 制約 | 説明 |
|---------|---|------|-----|
| id | BIGINT | PK, Auto Increment | ユーザーID |
| username | VARCHAR(50) | UNIQUE, NOT NULL | ユーザー名（ログインID） |
| password_hash | VARCHAR(255) | NOT NULL | パスワードハッシュ（BCrypt） |
| role | VARCHAR(20) | NOT NULL | 権限（USER, ADMIN） |
| enabled | BOOLEAN | NOT NULL, DEFAULT TRUE | アカウント有効フラグ |
| created_at | TIMESTAMP | NOT NULL | 作成日時 |
| updated_at | TIMESTAMP | NOT NULL | 更新日時 |

#### Java エンティティ実装

```java
package com.hidearea.core.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    @Column(nullable = false)
    private Boolean enabled = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // リレーション（多対多）
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserProfile> userProfiles = new ArrayList<>();
}
```

#### UserRole Enum

```java
package com.hidearea.core.domain.entity;

public enum UserRole {
    USER,
    ADMIN
}
```

---

### 1.2 Profile（プロフィール）エンティティ

#### テーブル定義

| カラム名 | 型 | 制約 | 説明 |
|---------|---|------|-----|
| id | BIGINT | PK, Auto Increment | プロフィールID |
| email | VARCHAR(100) | UNIQUE, NOT NULL | メールアドレス |
| profile_name | VARCHAR(100) | NOT NULL | プロフィール名 |
| display_name | VARCHAR(100) | - | 表示名 |
| bio | TEXT | - | 自己紹介 |
| avatar_url | VARCHAR(255) | - | アバター画像URL |
| profile_type | VARCHAR(20) | NOT NULL | タイプ（PERSONAL, BUSINESS） |
| is_public | BOOLEAN | NOT NULL, DEFAULT TRUE | 公開フラグ |
| created_at | TIMESTAMP | NOT NULL | 作成日時 |
| updated_at | TIMESTAMP | NOT NULL | 更新日時 |

**設計方針（案C採用）**:
- **自己参照外部キーを使用しない**: プロフィール本体テーブルはシンプルに保つ
- **階層構造は別テーブルで管理**: `profile_profile_relations` テーブルで管理
- **初学者にも理解しやすい**: 「多対多」の中間テーブルと同じパターン

#### Java エンティティ実装

```java
package com.hidearea.core.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "profiles")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "profile_name", nullable = false, length = 100)
    private String profileName;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_type", nullable = false, length = 20)
    private ProfileType profileType = ProfileType.PERSONAL;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // リレーション（多対多: ユーザーとの関連）
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserProfile> userProfiles = new ArrayList<>();

    // リレーション（階層関係: 親プロフィール関連）
    @OneToMany(mappedBy = "childProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProfileProfileRelation> parentRelations = new ArrayList<>();

    // リレーション（階層関係: 子プロフィール関連）
    @OneToMany(mappedBy = "parentProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProfileProfileRelation> childRelations = new ArrayList<>();
}
```

#### ProfileType Enum

```java
package com.hidearea.core.domain.entity;

public enum ProfileType {
    PERSONAL,   // 個人用プロフィール
    BUSINESS    // ビジネス用プロフィール
}
```

---

### 1.3 UserProfile（ユーザー・プロフィール関連）中間テーブル

#### テーブル定義

| カラム名 | 型 | 制約 | 説明 |
|---------|---|------|-----|
| id | BIGINT | PK, Auto Increment | 関連ID |
| user_id | BIGINT | FK (users.id), NOT NULL | ユーザーID |
| profile_id | BIGINT | FK (profiles.id), NOT NULL | プロフィールID |
| role_in_profile | VARCHAR(20) | NOT NULL | プロフィール内での役割（OWNER, MEMBER） |
| joined_at | TIMESTAMP | NOT NULL | 参加日時 |

**制約**:
- **複合ユニーク制約**: `UNIQUE (user_id, profile_id)` - 同一ユーザーが同じプロフィールに重複参加不可
- **外部キー**: `user_id` → `users.id` (ON DELETE CASCADE)
- **外部キー**: `profile_id` → `profiles.id` (ON DELETE CASCADE)

#### Java エンティティ実装

```java
package com.hidearea.core.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_profiles",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "profile_id"})
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_in_profile", nullable = false, length = 20)
    private RoleInProfile roleInProfile = RoleInProfile.MEMBER;

    @CreatedDate
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;
}
```

#### RoleInProfile Enum

```java
package com.hidearea.core.domain.entity;

public enum RoleInProfile {
    OWNER,   // プロフィール所有者（作成者、完全な権限）
    MEMBER   // プロフィールメンバー（参加者）
}
```

---

### 1.4 ProfileProfileRelation（プロフィール階層関連）中間テーブル

アーキテクチャ概要は [ARCHITECTURE.md](./ARCHITECTURE.md) の「データモデル」セクションを参照してください。

#### テーブル定義

| カラム名 | 型 | 制約 | 説明 |
|---------|---|------|-----|
| id | BIGINT | PK, Auto Increment | 関連ID |
| parent_profile_id | BIGINT | FK (profiles.id), NOT NULL | 親プロフィールID |
| child_profile_id | BIGINT | FK (profiles.id), NOT NULL | 子プロフィールID |
| created_at | TIMESTAMP | NOT NULL | 作成日時 |

**制約**:
- **複合ユニーク制約**: `UNIQUE (parent_profile_id, child_profile_id)` - 同一親子関係の重複不可
- **外部キー**: `parent_profile_id` → `profiles.id` (ON DELETE CASCADE)
- **外部キー**: `child_profile_id` → `profiles.id` (ON DELETE CASCADE)
- **CHECK制約**: `parent_profile_id != child_profile_id` - 自分自身を親にできない

**設計方針**:
- **明示的な関連テーブル**: 「親プロフィール」と「子プロフィール」の関連を明確に管理
- **初学者に優しい**: 「多対多」の中間テーブルと同じパターン（自己参照外部キーを使用しない）
- **循環参照の防止**: アプリケーション層でバリデーション実装

#### Java エンティティ実装

```java
package com.hidearea.core.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "profile_profile_relations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"parent_profile_id", "child_profile_id"})
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

---

## 2. ER図

### 2.1 概念ER図（案C: 明示的な2つの関連テーブル）

```
┌─────────────────────┐
│       User          │
├─────────────────────┤
│ id (PK)             │
│ username (UNIQUE)   │
│ email (UNIQUE)      │
│ password_hash       │
│ role                │
│ enabled             │
│ created_at          │
│ updated_at          │
└─────────────────────┘
          │
          │ 1
          │
          │
          │ *
┌─────────────────────────────┐
│   UserProfile               │ ◄── 中間テーブル①（ユーザー・プロフィール多対多）
├─────────────────────────────┤
│ id (PK)                     │
│ user_id (FK → users)        │
│ profile_id (FK → profiles)  │
│ role_in_profile             │
│ joined_at                   │
└─────────────────────────────┘
          │
          │ *
          │
          │
          │ 1
┌─────────────────────┐         ┌────────────────────────────────┐
│      Profile        │ ◄─────* │ ProfileProfileRelation         │ ◄── 中間テーブル②（プロフィール階層）
├─────────────────────┤         ├────────────────────────────────┤
│ id (PK)             │ *─────► │ id (PK)                        │
│ profile_name        │         │ parent_profile_id (FK)         │
│ display_name        │         │ child_profile_id (FK)          │
│ bio                 │         │ created_at                     │
│ avatar_url          │         └────────────────────────────────┘
│ profile_type        │              親(1) : 子(*)
│ is_public           │
│ created_at          │
│ updated_at          │
└─────────────────────┘
```

**設計方針**:
- **自己参照外部キーを使用しない**: Profileテーブルはシンプルに保つ
- **階層構造は別テーブル**: ProfileProfileRelationで管理
- **全ての関連が「AとBの関連」パターン**: 初学者にも理解しやすい

### 2.2 物理ER図（DDL）- 案C採用

```sql
-- Users テーブル
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Profiles テーブル（シンプル構造: 自己参照なし）
CREATE TABLE profiles (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    profile_name VARCHAR(100) NOT NULL,
    display_name VARCHAR(100),
    bio TEXT,
    avatar_url VARCHAR(255),
    profile_type VARCHAR(20) NOT NULL DEFAULT 'PERSONAL',
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- UserProfiles 中間テーブル（ユーザー・プロフィール関連）
CREATE TABLE user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    role_in_profile VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_profiles_profile FOREIGN KEY (profile_id)
        REFERENCES profiles(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_profile UNIQUE (user_id, profile_id)
);

-- ProfileProfileRelations 中間テーブル（プロフィール階層関連）
CREATE TABLE profile_profile_relations (
    id BIGSERIAL PRIMARY KEY,
    parent_profile_id BIGINT NOT NULL,
    child_profile_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ppr_parent FOREIGN KEY (parent_profile_id)
        REFERENCES profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_ppr_child FOREIGN KEY (child_profile_id)
        REFERENCES profiles(id) ON DELETE CASCADE,
    CONSTRAINT uk_parent_child UNIQUE (parent_profile_id, child_profile_id),
    CONSTRAINT chk_not_self CHECK (parent_profile_id != child_profile_id)
);
```

### 2.3 プロフィール階層の説明（案C）

#### 階層構造の実現

**ProfileProfileRelation中間テーブル**により、プロフィールが別のプロフィールを親として持つことができます：

```
親プロフィール（ルート）: profile_profile_relationsに親として登録されていない
  └─ 子プロフィール1: parent_profile_id = 親のID, child_profile_id = 子1のID
      └─ 孫プロフィール: parent_profile_id = 子1のID, child_profile_id = 孫のID
  └─ 子プロフィール2: parent_profile_id = 親のID, child_profile_id = 子2のID
```

**ルートプロフィールの判定**:
```sql
-- ルートプロフィール = profile_profile_relationsのchild_profile_idに存在しないプロフィール
SELECT p.*
FROM profiles p
LEFT JOIN profile_profile_relations ppr ON p.id = ppr.child_profile_id
WHERE ppr.id IS NULL;
```

#### ユースケース例

**企業プロフィール配下のキャラクタープロフィール**:

```
企業A（BUSINESS）
  ├─ ゆるキャラA（PERSONAL）
  ├─ ゆるキャラB（PERSONAL）
  └─ 公式アカウント（BUSINESS）

ユーザー（個人）
  ├─ カメラマンプロフィール（PERSONAL）
  ├─ 小説家プロフィール（PERSONAL）
  └─ 配信者プロフィール（PERSONAL）
```

---

## 3. リポジトリパターン

### 3.1 UserRepository

```java
package com.hidearea.core.repository;

import com.hidearea.core.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsernameAndEnabledTrue(String username);
}
```

### 3.2 ProfileRepository

```java
package com.hidearea.core.repository;

import com.hidearea.core.domain.entity.Profile;
import com.hidearea.core.domain.entity.ProfileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    // プロフィールタイプで検索
    List<Profile> findByProfileType(ProfileType profileType);

    // 公開プロフィールのみ検索
    List<Profile> findByIsPublicTrue();

    // プロフィール名で検索（部分一致）
    List<Profile> findByProfileNameContaining(String keyword);

    // 特定の親プロフィールの子プロフィールを取得（案C: 関連テーブル経由）
    @Query("SELECT r.childProfile FROM ProfileProfileRelation r WHERE r.parentProfile.id = :parentId")
    List<Profile> findChildrenByParentId(@Param("parentId") Long parentId);

    // ルートプロフィール（子として登録されていない）を取得
    @Query("""
        SELECT p FROM Profile p
        WHERE NOT EXISTS (
            SELECT 1 FROM ProfileProfileRelation r WHERE r.childProfile.id = p.id
        )
        """)
    List<Profile> findRootProfiles();

    // プロフィール階層の深さを取得（再帰クエリ）
    @Query(value = """
        WITH RECURSIVE profile_tree AS (
            SELECT child_profile_id as id, parent_profile_id, 0 as depth
            FROM profile_profile_relations
            WHERE child_profile_id = :profileId

            UNION ALL

            SELECT ppr.child_profile_id, ppr.parent_profile_id, pt.depth + 1
            FROM profile_profile_relations ppr
            INNER JOIN profile_tree pt ON ppr.parent_profile_id = pt.id
        )
        SELECT MAX(depth) FROM profile_tree
        """, nativeQuery = true)
    Integer getProfileDepth(@Param("profileId") Long profileId);

    // プロフィールのルートを取得（階層の最上位）
    @Query(value = """
        WITH RECURSIVE profile_ancestors AS (
            SELECT child_profile_id as id, parent_profile_id
            FROM profile_profile_relations
            WHERE child_profile_id = :profileId

            UNION ALL

            SELECT ppr.child_profile_id, ppr.parent_profile_id
            FROM profile_profile_relations ppr
            INNER JOIN profile_ancestors pa ON ppr.child_profile_id = pa.parent_profile_id
        )
        SELECT id FROM profile_ancestors
        WHERE parent_profile_id IS NULL
        OR parent_profile_id NOT IN (SELECT child_profile_id FROM profile_profile_relations)
        LIMIT 1
        """, nativeQuery = true)
    Long findRootProfileId(@Param("profileId") Long profileId);
}
```

### 3.3 UserProfileRepository

```java
package com.hidearea.core.repository;

import com.hidearea.core.domain.entity.UserProfile;
import com.hidearea.core.domain.entity.RoleInProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    // ユーザーが所属する全プロフィールを取得
    @Query("SELECT up FROM UserProfile up " +
           "JOIN FETCH up.profile " +
           "WHERE up.user.id = :userId")
    List<UserProfile> findByUserId(@Param("userId") Long userId);

    // プロフィールに所属する全ユーザーを取得
    @Query("SELECT up FROM UserProfile up " +
           "JOIN FETCH up.user " +
           "WHERE up.profile.id = :profileId")
    List<UserProfile> findByProfileId(@Param("profileId") Long profileId);

    // 特定のユーザーとプロフィールの関連を検索
    Optional<UserProfile> findByUserIdAndProfileId(Long userId, Long profileId);

    // ユーザーがプロフィールに所属しているか確認
    boolean existsByUserIdAndProfileId(Long userId, Long profileId);

    // プロフィールのオーナーを取得
    @Query("SELECT up FROM UserProfile up " +
           "WHERE up.profile.id = :profileId " +
           "AND up.roleInProfile = 'OWNER'")
    List<UserProfile> findOwnersByProfileId(@Param("profileId") Long profileId);

    // ユーザーが特定の役割でプロフィールに所属しているか確認
    boolean existsByUserIdAndProfileIdAndRoleInProfile(
        Long userId, Long profileId, RoleInProfile roleInProfile
    );

    // ユーザーがルートプロフィールを所有しているか取得（案C: 関連テーブル経由）
    @Query("""
        SELECT up FROM UserProfile up
        JOIN FETCH up.profile p
        WHERE up.user.id = :userId
        AND NOT EXISTS (
            SELECT 1 FROM ProfileProfileRelation r WHERE r.childProfile.id = p.id
        )
        AND up.roleInProfile = 'OWNER'
        """)
    List<UserProfile> findRootProfilesByUserId(@Param("userId") Long userId);
}
```

### 3.4 ProfileProfileRelationRepository

```java
package com.hidearea.core.repository;

import com.hidearea.core.domain.entity.ProfileProfileRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileProfileRelationRepository extends JpaRepository<ProfileProfileRelation, Long> {

    // 特定の親プロフィールの子プロフィール関連を取得
    List<ProfileProfileRelation> findByParentProfileId(Long parentProfileId);

    // 特定の子プロフィールの親プロフィール関連を取得
    Optional<ProfileProfileRelation> findByChildProfileId(Long childProfileId);

    // 親子関係が存在するか確認
    boolean existsByParentProfileIdAndChildProfileId(Long parentProfileId, Long childProfileId);

    // 特定のプロフィールの全子孫を取得（再帰クエリ）
    @Query(value = """
        WITH RECURSIVE profile_tree AS (
            SELECT parent_profile_id, child_profile_id, 1 as depth
            FROM profile_profile_relations
            WHERE parent_profile_id = :profileId

            UNION ALL

            SELECT ppr.parent_profile_id, ppr.child_profile_id, pt.depth + 1
            FROM profile_profile_relations ppr
            INNER JOIN profile_tree pt ON ppr.parent_profile_id = pt.child_profile_id
        )
        SELECT child_profile_id FROM profile_tree
        """, nativeQuery = true)
    List<Long> findAllDescendantIds(@Param("profileId") Long profileId);

    // 特定のプロフィールの全祖先を取得（再帰クエリ）
    @Query(value = """
        WITH RECURSIVE profile_ancestors AS (
            SELECT parent_profile_id, child_profile_id, 1 as depth
            FROM profile_profile_relations
            WHERE child_profile_id = :profileId

            UNION ALL

            SELECT ppr.parent_profile_id, ppr.child_profile_id, pa.depth + 1
            FROM profile_profile_relations ppr
            INNER JOIN profile_ancestors pa ON ppr.child_profile_id = pa.parent_profile_id
        )
        SELECT parent_profile_id FROM profile_ancestors
        """, nativeQuery = true)
    List<Long> findAllAncestorIds(@Param("profileId") Long profileId);
}
```

---

## 4. インデックス戦略

### 4.1 インデックス定義

#### Users テーブル

```sql
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_enabled ON users(enabled) WHERE enabled = TRUE;
CREATE INDEX idx_users_created_at ON users(created_at);
```

#### Profiles テーブル

```sql
-- プロフィールタイプでのフィルタリング用
CREATE INDEX idx_profiles_profile_type ON profiles(profile_type);

-- 公開プロフィールのフィルタリング用
CREATE INDEX idx_profiles_is_public ON profiles(is_public) WHERE is_public = TRUE;

-- プロフィール名での検索用
CREATE INDEX idx_profiles_profile_name ON profiles(profile_name);

-- 作成日時でのソート用
CREATE INDEX idx_profiles_created_at ON profiles(created_at);
```

#### UserProfiles 中間テーブル

```sql
CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX idx_user_profiles_profile_id ON user_profiles(profile_id);
CREATE INDEX idx_user_profiles_role ON user_profiles(role_in_profile);
CREATE INDEX idx_user_profiles_joined_at ON user_profiles(joined_at);
```

#### ProfileProfileRelations 中間テーブル（案C）

```sql
-- 親プロフィールIDでの検索用（子プロフィール一覧取得）
CREATE INDEX idx_ppr_parent_profile_id ON profile_profile_relations(parent_profile_id);

-- 子プロフィールIDでの検索用（親プロフィール取得、ルート判定）
CREATE INDEX idx_ppr_child_profile_id ON profile_profile_relations(child_profile_id);

-- 作成日時でのソート用
CREATE INDEX idx_ppr_created_at ON profile_profile_relations(created_at);
```

---

## 5. データベーススキーマ

### 5.1 Flyway マイグレーションファイル

#### V1__init_schema.sql

```sql
-- Users テーブル
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Profiles テーブル（案C: シンプル構造、階層は別テーブルで管理）
CREATE TABLE profiles (
    id BIGSERIAL PRIMARY KEY,
    profile_name VARCHAR(100) NOT NULL,
    display_name VARCHAR(100),
    bio TEXT,
    avatar_url VARCHAR(255),
    profile_type VARCHAR(20) NOT NULL DEFAULT 'PERSONAL',
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- UserProfiles 中間テーブル（ユーザー・プロフィール関連）
CREATE TABLE user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    role_in_profile VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_profiles_profile FOREIGN KEY (profile_id)
        REFERENCES profiles(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_profile UNIQUE (user_id, profile_id)
);

-- ProfileProfileRelations 中間テーブル（案C: プロフィール階層関連）
CREATE TABLE profile_profile_relations (
    id BIGSERIAL PRIMARY KEY,
    parent_profile_id BIGINT NOT NULL,
    child_profile_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ppr_parent FOREIGN KEY (parent_profile_id)
        REFERENCES profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_ppr_child FOREIGN KEY (child_profile_id)
        REFERENCES profiles(id) ON DELETE CASCADE,
    CONSTRAINT uk_parent_child UNIQUE (parent_profile_id, child_profile_id),
    CONSTRAINT chk_not_self CHECK (parent_profile_id != child_profile_id)
);

-- Indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_enabled ON users(enabled) WHERE enabled = TRUE;

CREATE INDEX idx_profiles_profile_type ON profiles(profile_type);
CREATE INDEX idx_profiles_is_public ON profiles(is_public) WHERE is_public = TRUE;
CREATE INDEX idx_profiles_profile_name ON profiles(profile_name);
CREATE INDEX idx_profiles_created_at ON profiles(created_at);

CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX idx_user_profiles_profile_id ON user_profiles(profile_id);
CREATE INDEX idx_user_profiles_role ON user_profiles(role_in_profile);

CREATE INDEX idx_ppr_parent_profile_id ON profile_profile_relations(parent_profile_id);
CREATE INDEX idx_ppr_child_profile_id ON profile_profile_relations(child_profile_id);
CREATE INDEX idx_ppr_created_at ON profile_profile_relations(created_at);
```

#### V2__add_sample_data.sql（開発環境用サンプルデータ）

```sql
-- サンプルユーザー（パスワード: password123）
INSERT INTO users (username, email, password_hash, role, enabled) VALUES
('admin', 'admin@hidearea.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', TRUE),
('john_doe', 'john@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', TRUE),
('jane_smith', 'jane@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', TRUE);

-- サンプルプロフィール（案C: 全てルートプロフィールとして作成）
INSERT INTO profiles (id, profile_name, display_name, bio, profile_type, is_public) VALUES
(1, 'john-personal', 'John Doe', 'Software Engineer', 'PERSONAL', TRUE),
(2, 'acme-corp', 'ACME Corporation', 'Building the future', 'BUSINESS', TRUE),
(3, 'jane-photographer', 'Jane Smith - Photographer', 'Professional photographer', 'PERSONAL', TRUE),
(4, 'acme-mascot-alpha', 'ACME Mascot Alpha', 'Official mascot character', 'PERSONAL', TRUE),
(5, 'acme-mascot-beta', 'ACME Mascot Beta', 'Second mascot character', 'PERSONAL', TRUE),
(6, 'acme-official', 'ACME Official Account', 'Official announcements', 'BUSINESS', TRUE),
(7, 'alpha-mini', 'Alpha Mini', 'Mini version of Alpha', 'PERSONAL', TRUE);

-- プロフィール階層の作成（案C: profile_profile_relationsテーブル使用）
INSERT INTO profile_profile_relations (parent_profile_id, child_profile_id) VALUES
-- acme-corp の子プロフィール
(2, 4),  -- acme-corp → acme-mascot-alpha
(2, 5),  -- acme-corp → acme-mascot-beta
(2, 6),  -- acme-corp → acme-official
-- acme-mascot-alpha の子プロフィール（孫）
(4, 7);  -- acme-mascot-alpha → alpha-mini

-- ユーザーとプロフィールの関連付け
INSERT INTO user_profiles (user_id, profile_id, role_in_profile) VALUES
-- john_doe の所有プロフィール
(2, 1, 'OWNER'),      -- john-personal

-- jane_smith の所有プロフィール
(3, 2, 'OWNER'),      -- acme-corp (親)
(3, 4, 'OWNER'),      -- acme-mascot-alpha (子)
(3, 5, 'OWNER'),      -- acme-mascot-beta (子)
(3, 6, 'OWNER'),      -- acme-official (子)
(3, 7, 'OWNER'),      -- alpha-mini (孫)
(3, 3, 'OWNER'),      -- jane-photographer

-- john_doe が acme-corp にメンバーとして参加
(2, 2, 'MEMBER');
```

---

## 6. ユースケース例

### 6.1 企業プロフィール配下のキャラクタープロフィール

**シナリオ**: ACME社が企業プロフィールを持ち、その配下に複数のゆるキャラプロフィールを管理

```
ACME Corporation（企業プロフィール）
  ├─ ACME Mascot Alpha（ゆるキャラA）
  │    └─ Alpha Mini（ミニバージョン）
  ├─ ACME Mascot Beta（ゆるキャラB）
  └─ ACME Official Account（公式アカウント）
```

**SQLクエリ例（案C）**:

```sql
-- ACME Corporation の全子プロフィールを取得
SELECT p.*
FROM profiles p
INNER JOIN profile_profile_relations ppr ON p.id = ppr.child_profile_id
WHERE ppr.parent_profile_id = 2;

-- ACME Mascot Alpha の全子プロフィール（孫）を取得
SELECT p.*
FROM profiles p
INNER JOIN profile_profile_relations ppr ON p.id = ppr.child_profile_id
WHERE ppr.parent_profile_id = 4;

-- 階層全体を取得（再帰クエリ）
WITH RECURSIVE profile_tree AS (
    SELECT p.id, p.profile_name, 0 as level, CAST(NULL AS BIGINT) as parent_profile_id
    FROM profiles p
    WHERE p.id = 2  -- ACME Corporation (ルート)

    UNION ALL

    SELECT p.id, p.profile_name, pt.level + 1, ppr.parent_profile_id
    FROM profiles p
    INNER JOIN profile_profile_relations ppr ON p.id = ppr.child_profile_id
    INNER JOIN profile_tree pt ON ppr.parent_profile_id = pt.id
)
SELECT * FROM profile_tree ORDER BY level, id;
```

### 6.2 個人ユーザーの複数プロフィール

**シナリオ**: ユーザーが趣味用、仕事用など複数のプロフィールを保持

```
John Doe（ユーザー）
  ├─ カメラマンプロフィール
  ├─ 小説家プロフィール
  └─ 配信者プロフィール
```

これらは全てルートプロフィール（`profile_profile_relations`に`child_profile_id`として登録されていない）として作成されます。

### 6.3 プロフィール階層の制限

#### 推奨事項

- **最大階層深度**: 3階層程度を推奨（ルート → 子 → 孫）
- **循環参照防止**: アプリケーションレベルでチェック
- **削除時の挙動**: CASCADE削除により、親プロフィール削除時に`profile_profile_relations`の関係も削除

#### アプリケーションレベルでの制御（案C）

```java
@Service
public class ProfileService {

    // プロフィール作成時の階層チェック（案C）
    public Profile createChildProfile(Long parentProfileId, ProfileCreateRequest request) {
        Profile parent = profileRepository.findById(parentProfileId)
            .orElseThrow(() -> new ResourceNotFoundException("Parent profile not found"));

        // 階層深度チェック
        int depth = getProfileDepth(parentProfileId);
        if (depth >= MAX_DEPTH) {
            throw new BusinessException("Maximum profile hierarchy depth exceeded");
        }

        // 子プロフィールを作成（案C: 親参照なし）
        Profile child = Profile.builder()
            .profileName(request.getProfileName())
            // ... 他のフィールド（parentProfileフィールドなし）
            .build();

        Profile savedChild = profileRepository.save(child);

        // 案C: profile_profile_relationsに関係を作成
        ProfileProfileRelation relation = ProfileProfileRelation.builder()
            .parentProfile(parent)
            .childProfile(savedChild)
            .build();
        profileProfileRelationRepository.save(relation);

        return savedChild;
    }

    // 循環参照チェック
    public void checkCircularReference(Long profileId, Long newParentId) {
        Long rootId = profileRepository.findRootProfileId(profileId);
        if (rootId.equals(newParentId)) {
            throw new BusinessException("Circular reference detected");
        }
    }
}
```

---

## まとめ

本データ設計では、以下の拡張を行いました：

### 主要な変更点（案C採用）

1. **プロフィールの階層構造対応**:
   - `ProfileProfileRelation`中間テーブルによる階層管理（案C）
   - Profilesテーブルはシンプルに保ち、階層は別テーブルで管理
   - プロフィールがプロフィールを親として持つことが可能

2. **ユースケースのサポート**:
   - 企業プロフィール配下のキャラクタープロフィール
   - 個人ユーザーの複数ペルソナ（趣味用、仕事用）
   - プロフィールのネスト構造

3. **データ整合性の保証**:
   - CASCADE削除で親子関係を維持
   - インデックス最適化で階層検索を高速化
   - 再帰クエリでの階層全体の取得

4. **拡張性**:
   - 階層深度の制限はアプリケーションレベルで制御
   - 循環参照防止のロジック実装
   - 将来的なコミュニティ機能への拡張も容易

この設計により、柔軟なプロフィール管理と階層構造を実現しつつ、データ整合性とパフォーマンスを両立させています。
