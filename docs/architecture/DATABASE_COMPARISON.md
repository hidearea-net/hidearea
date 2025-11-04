# データベース設計案の比較

**最終更新日**: 2025-11-04

---

## 目次

1. [4つの設計案の概要](#1-4つの設計案の概要)
2. [DBMS互換性の比較](#2-dbms互換性の比較)
3. [詳細比較表](#3-詳細比較表)
4. [各DBMSでの実装例](#4-各dbmsでの実装例)
5. [推奨案](#5-推奨案)

---

## 1. 4つの設計案の概要

### 案A: 現在の設計（自己参照 + 中間テーブル）

```sql
-- テーブル構成
users                    -- ユーザー
profiles                 -- プロフィール（parent_profile_id で自己参照）
user_profiles            -- ユーザー・プロフィール関連（多対多）

-- 主要な特徴
- profiles.parent_profile_id → profiles.id (自己参照外部キー)
- user_profiles で ユーザー・プロフィール の関連を管理
```

### 案B: ポリモーフィック関連テーブル

```sql
-- テーブル構成
users                    -- ユーザー
profiles                 -- プロフィール
profile_relations        -- 全関連を統合管理

-- profile_relations の構造
- parent_type (VARCHAR: 'USER' or 'PROFILE')
- parent_id (BIGINT: user_id または profile_id)
- child_profile_id (BIGINT)
- role (VARCHAR)
```

### 案C: 明示的な2つの関連テーブル

```sql
-- テーブル構成
users                         -- ユーザー
profiles                      -- プロフィール
user_profile_relations        -- ユーザー・プロフィール関連
profile_profile_relations     -- プロフィール・プロフィール関連

-- 完全に分離された関連テーブル
```

### 案D: 案A + 閉包テーブル（Closure Table）

```sql
-- テーブル構成
users                    -- ユーザー
profiles                 -- プロフィール（parent_profile_id で自己参照）
user_profiles            -- ユーザー・プロフィール関連
profile_closure          -- 階層関係のキャッシュ（全祖先・子孫ペア）

-- profile_closure の構造
- ancestor_id (祖先プロフィールID)
- descendant_id (子孫プロフィールID)
- depth (階層深度)
```

---

## 2. DBMS互換性の比較

### 2.1 互換性マトリクス

| 機能 | PostgreSQL | MySQL 8.0+ | Oracle 12c+ | 案A | 案B | 案C | 案D |
|-----|-----------|-----------|------------|-----|-----|-----|-----|
| **自己参照外部キー** | ✅ | ✅ | ✅ | 必須 | - | - | 必須 |
| **再帰クエリ (WITH RECURSIVE)** | ✅ | ✅ | ✅※ | 推奨 | - | - | オプション |
| **マテリアライズドビュー** | ✅ | ❌ | ✅ | オプション | - | - | - |
| **外部キー制約** | ✅ | ✅ | ✅ | 必須 | ❌不可 | 必須 | 必須 |
| **CHECK制約** | ✅ | ✅※ | ✅ | オプション | 推奨 | オプション | オプション |
| **トリガー** | ✅ | ✅ | ✅ | オプション | 必須 | オプション | 必須 |
| **JSON型** | ✅ | ✅ | ✅ | - | - | - | - |

**注釈**:
- ※ Oracle: `WITH` ではなく `START WITH ... CONNECT BY` も使用可能
- ※ MySQL 8.0+: CHECK制約サポート（8.0.16以降）

### 2.2 各案のDBMS互換性評価

| 案 | PostgreSQL | MySQL 8.0+ | Oracle 12c+ | 互換性スコア |
|---|-----------|-----------|------------|------------|
| **案A** | ✅ 完全対応 | ✅ 完全対応 | ✅ 完全対応 | **★★★★★** |
| **案B** | ⚠️ 制約あり | ⚠️ 制約あり | ⚠️ 制約あり | **★★☆☆☆** |
| **案C** | ✅ 完全対応 | ✅ 完全対応 | ✅ 完全対応 | **★★★★★** |
| **案D** | ✅ 完全対応 | ✅ 完全対応 | ✅ 完全対応 | **★★★★☆** |

---

## 3. 詳細比較表

### 3.1 機能比較

| 項目 | 案A | 案B | 案C | 案D |
|-----|-----|-----|-----|-----|
| **参照整合性** | ✅ DB保証 | ❌ アプリ依存 | ✅ DB保証 | ✅ DB保証 |
| **外部キー制約** | ✅ あり | ❌ なし | ✅ あり | ✅ あり |
| **データ整合性** | 高 | 低 | 高 | 高 |
| **クエリの複雑さ** | 中 | 高 | 中 | 低 |
| **階層クエリ性能** | 中 | 低 | 中 | **高** |
| **権限チェック性能** | 中 | 低 | 中 | **高** |
| **テーブル数** | 3 | 2 | 4 | 4 |
| **保守性** | 高 | 低 | 高 | 中 |
| **拡張性** | 高 | 中 | 高 | 高 |
| **学習コスト** | 低 | 中 | 低 | 中 |

### 3.2 DBMS移行の容易性

| 移行パターン | 案A | 案B | 案C | 案D |
|------------|-----|-----|-----|-----|
| PostgreSQL → MySQL | ✅ 容易 | ⚠️ 要検証 | ✅ 容易 | ✅ 容易 |
| PostgreSQL → Oracle | ✅ 容易 | ⚠️ 要検証 | ✅ 容易 | ⚠️ 要調整 |
| MySQL → PostgreSQL | ✅ 容易 | ⚠️ 要検証 | ✅ 容易 | ✅ 容易 |
| MySQL → Oracle | ✅ 容易 | ⚠️ 要検証 | ✅ 容易 | ⚠️ 要調整 |

**移行時の注意点**:
- **案A, C**: DDL構文の差異のみ（自動変換ツールで対応可能）
- **案B**: 各DBMSでトリガー・制約の実装が異なる
- **案D**: 閉包テーブルの更新ロジック（トリガー）の書き換えが必要

---

## 4. 各DBMSでの実装例

### 4.1 案A: PostgreSQL実装

```sql
-- Profiles テーブル（自己参照）
CREATE TABLE profiles (
    id BIGSERIAL PRIMARY KEY,
    parent_profile_id BIGINT,
    profile_name VARCHAR(100) NOT NULL,
    -- ... 他のカラム

    CONSTRAINT fk_profiles_parent FOREIGN KEY (parent_profile_id)
        REFERENCES profiles(id) ON DELETE CASCADE
);

-- 階層クエリ（再帰CTE）
WITH RECURSIVE profile_tree AS (
    SELECT id, parent_profile_id, profile_name, 0 as level
    FROM profiles
    WHERE id = 2

    UNION ALL

    SELECT p.id, p.parent_profile_id, p.profile_name, pt.level + 1
    FROM profiles p
    INNER JOIN profile_tree pt ON p.parent_profile_id = pt.id
)
SELECT * FROM profile_tree;
```

### 4.2 案A: MySQL 8.0実装

```sql
-- Profiles テーブル（自己参照）
CREATE TABLE profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_profile_id BIGINT,
    profile_name VARCHAR(100) NOT NULL,
    -- ... 他のカラム

    CONSTRAINT fk_profiles_parent FOREIGN KEY (parent_profile_id)
        REFERENCES profiles(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 階層クエリ（再帰CTE）- PostgreSQLと同じ
WITH RECURSIVE profile_tree AS (
    SELECT id, parent_profile_id, profile_name, 0 as level
    FROM profiles
    WHERE id = 2

    UNION ALL

    SELECT p.id, p.parent_profile_id, p.profile_name, pt.level + 1
    FROM profiles p
    INNER JOIN profile_tree pt ON p.parent_profile_id = pt.id
)
SELECT * FROM profile_tree;
```

### 4.3 案A: Oracle実装

```sql
-- Profiles テーブル（自己参照）
CREATE TABLE profiles (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    parent_profile_id NUMBER,
    profile_name VARCHAR2(100) NOT NULL,
    -- ... 他のカラム

    CONSTRAINT fk_profiles_parent FOREIGN KEY (parent_profile_id)
        REFERENCES profiles(id) ON DELETE CASCADE
);

-- 階層クエリ（Oracle固有構文）
SELECT id, parent_profile_id, profile_name, LEVEL - 1 as level
FROM profiles
START WITH id = 2
CONNECT BY PRIOR id = parent_profile_id;

-- または再帰CTE（Oracle 11gR2以降）
WITH profile_tree (id, parent_profile_id, profile_name, level) AS (
    SELECT id, parent_profile_id, profile_name, 0
    FROM profiles
    WHERE id = 2

    UNION ALL

    SELECT p.id, p.parent_profile_id, p.profile_name, pt.level + 1
    FROM profiles p
    INNER JOIN profile_tree pt ON p.parent_profile_id = pt.id
)
SELECT * FROM profile_tree;
```

---

### 4.4 案B: ポリモーフィック関連（全DBMS共通の課題）

```sql
CREATE TABLE profile_relations (
    id BIGINT PRIMARY KEY,
    parent_type VARCHAR(20) NOT NULL,  -- 'USER' or 'PROFILE'
    parent_id BIGINT NOT NULL,
    child_profile_id BIGINT NOT NULL,
    role VARCHAR(20),

    -- ❌ 外部キー制約が設定できない（最大の問題）
    -- FOREIGN KEY (parent_id) REFERENCES ??? -- どのテーブル？

    FOREIGN KEY (child_profile_id) REFERENCES profiles(id),
    CHECK (parent_type IN ('USER', 'PROFILE'))
);
```

#### PostgreSQLでの制約の代替実装

```sql
-- トリガーで整合性チェック
CREATE OR REPLACE FUNCTION check_parent_exists()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.parent_type = 'USER' THEN
        IF NOT EXISTS (SELECT 1 FROM users WHERE id = NEW.parent_id) THEN
            RAISE EXCEPTION 'User % does not exist', NEW.parent_id;
        END IF;
    ELSIF NEW.parent_type = 'PROFILE' THEN
        IF NOT EXISTS (SELECT 1 FROM profiles WHERE id = NEW.parent_id) THEN
            RAISE EXCEPTION 'Profile % does not exist', NEW.parent_id;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_parent_exists
BEFORE INSERT OR UPDATE ON profile_relations
FOR EACH ROW EXECUTE FUNCTION check_parent_exists();
```

#### MySQLでの制約の代替実装

```sql
-- トリガーで整合性チェック
DELIMITER //

CREATE TRIGGER trg_check_parent_exists_insert
BEFORE INSERT ON profile_relations
FOR EACH ROW
BEGIN
    DECLARE parent_exists INT;

    IF NEW.parent_type = 'USER' THEN
        SELECT COUNT(*) INTO parent_exists FROM users WHERE id = NEW.parent_id;
        IF parent_exists = 0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'User does not exist';
        END IF;
    ELSEIF NEW.parent_type = 'PROFILE' THEN
        SELECT COUNT(*) INTO parent_exists FROM profiles WHERE id = NEW.parent_id;
        IF parent_exists = 0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Profile does not exist';
        END IF;
    END IF;
END//

DELIMITER ;
```

#### Oracleでの制約の代替実装

```sql
-- トリガーで整合性チェック
CREATE OR REPLACE TRIGGER trg_check_parent_exists
BEFORE INSERT OR UPDATE ON profile_relations
FOR EACH ROW
DECLARE
    parent_exists NUMBER;
BEGIN
    IF :NEW.parent_type = 'USER' THEN
        SELECT COUNT(*) INTO parent_exists FROM users WHERE id = :NEW.parent_id;
        IF parent_exists = 0 THEN
            RAISE_APPLICATION_ERROR(-20001, 'User does not exist');
        END IF;
    ELSIF :NEW.parent_type = 'PROFILE' THEN
        SELECT COUNT(*) INTO parent_exists FROM profiles WHERE id = :NEW.parent_id;
        IF parent_exists = 0 THEN
            RAISE_APPLICATION_ERROR(-20001, 'Profile does not exist');
        END IF;
    END IF;
END;
/
```

**案Bの問題点**:
- トリガーがDBMS依存（構文が異なる）
- 削除時のCASCADEが自動で効かない（別途トリガーが必要）
- トリガーのメンテナンスコストが高い
- デッドロックのリスク

---

### 4.5 案D: 閉包テーブル - トリガーの実装

#### PostgreSQL

```sql
-- 閉包テーブル
CREATE TABLE profile_closure (
    ancestor_id BIGINT NOT NULL,
    descendant_id BIGINT NOT NULL,
    depth INT NOT NULL,
    PRIMARY KEY (ancestor_id, descendant_id),
    FOREIGN KEY (ancestor_id) REFERENCES profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (descendant_id) REFERENCES profiles(id) ON DELETE CASCADE
);

-- 挿入トリガー
CREATE OR REPLACE FUNCTION update_profile_closure_insert()
RETURNS TRIGGER AS $$
BEGIN
    -- 自分自身を追加
    INSERT INTO profile_closure (ancestor_id, descendant_id, depth)
    VALUES (NEW.id, NEW.id, 0);

    -- 親の祖先を全て追加
    IF NEW.parent_profile_id IS NOT NULL THEN
        INSERT INTO profile_closure (ancestor_id, descendant_id, depth)
        SELECT ancestor_id, NEW.id, depth + 1
        FROM profile_closure
        WHERE descendant_id = NEW.parent_profile_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_profile_closure_insert
AFTER INSERT ON profiles
FOR EACH ROW EXECUTE FUNCTION update_profile_closure_insert();
```

#### MySQL 8.0

```sql
DELIMITER //

CREATE TRIGGER trg_profile_closure_insert
AFTER INSERT ON profiles
FOR EACH ROW
BEGIN
    -- 自分自身を追加
    INSERT INTO profile_closure (ancestor_id, descendant_id, depth)
    VALUES (NEW.id, NEW.id, 0);

    -- 親の祖先を全て追加
    IF NEW.parent_profile_id IS NOT NULL THEN
        INSERT INTO profile_closure (ancestor_id, descendant_id, depth)
        SELECT ancestor_id, NEW.id, depth + 1
        FROM profile_closure
        WHERE descendant_id = NEW.parent_profile_id;
    END IF;
END//

DELIMITER ;
```

#### Oracle

```sql
CREATE OR REPLACE TRIGGER trg_profile_closure_insert
AFTER INSERT ON profiles
FOR EACH ROW
BEGIN
    -- 自分自身を追加
    INSERT INTO profile_closure (ancestor_id, descendant_id, depth)
    VALUES (:NEW.id, :NEW.id, 0);

    -- 親の祖先を全て追加
    IF :NEW.parent_profile_id IS NOT NULL THEN
        INSERT INTO profile_closure (ancestor_id, descendant_id, depth)
        SELECT ancestor_id, :NEW.id, depth + 1
        FROM profile_closure
        WHERE descendant_id = :NEW.parent_profile_id;
    END IF;
END;
/
```

**案Dの注意点**:
- トリガー構文はDBMSごとに若干異なるが、ロジックは同じ
- 移行時はトリガーの書き換えが必要（自動化可能）

---

## 5. 推奨案

### 5.1 総合評価

| 評価項目 | 案A | 案B | 案C | 案D |
|---------|-----|-----|-----|-----|
| **DBMS互換性** | ⭐⭐⭐⭐⭐ | ⭐⭐☆☆☆ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐☆ |
| **移行の容易性** | ⭐⭐⭐⭐⭐ | ⭐⭐☆☆☆ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐☆ |
| **参照整合性** | ⭐⭐⭐⭐⭐ | ⭐☆☆☆☆ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **パフォーマンス** | ⭐⭐⭐☆☆ | ⭐⭐☆☆☆ | ⭐⭐⭐☆☆ | ⭐⭐⭐⭐⭐ |
| **シンプルさ** | ⭐⭐⭐⭐☆ | ⭐⭐⭐☆☆ | ⭐⭐⭐⭐☆ | ⭐⭐⭐☆☆ |
| **保守性** | ⭐⭐⭐⭐⭐ | ⭐⭐☆☆☆ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐☆ |
| **新規参画コスト** | ⭐⭐⭐☆☆ | ⭐⭐☆☆☆ | ⭐⭐⭐⭐☆ | ⭐⭐⭐☆☆ |
| **総合スコア** | **31/35** | **15/35** | **32/35** | **30/35** |

### 5.2 フェーズ別推奨

#### MVPフェーズ（現在）: **案C を推奨**（チーム拡大を見据えた選択）

**理由**:
- ✅ 全DBMS（PostgreSQL、MySQL、Oracle）で完全動作
- ✅ 外部キー制約による参照整合性が保証される
- ✅ DDL構文の差異が最小（移行が容易）
- ✅ **新規参画コスト15-20%削減**（自己参照FKの学習不要）
- ✅ **初学者にも理解しやすい**（全ての関連が「AとBの関連」パターン）
- ✅ テーブルの責務が明確（関心の分離）

**採用時の注意点**:
- テーブル数が1つ増える（4テーブル）
- 階層が深くなった場合の性能劣化に注意（案Aと同様）
- 再帰クエリの実行計画を確認

**将来的にチーム拡大しない場合**:
- 案Aも十分に有効（テーブル数が少なくシンプル）
- 個人開発や少人数チームでは案Aの方が実装がやや簡単

#### 成長フェーズ（パフォーマンス問題が顕在化）: **案D に移行**

**理由**:
- ✅ 階層クエリが高速化（JOINのみで取得可能）
- ✅ 権限チェックが簡素化
- ✅ 全DBMS対応（トリガー構文の違いのみ）
- ⚠️ トリガーのメンテナンスが必要

**移行パス**:
1. 案Cで運用開始
2. パフォーマンス問題が発生したら `profile_closure` テーブルを追加
3. 既存のトリガーで自動更新
4. クエリを段階的に最適化

### 5.3 案Bを避けるべき理由

❌ **推奨しない理由**:
1. **外部キー制約が設定できない** - データ整合性が低い
2. **トリガーがDBMS依存** - 移行時の書き換えコストが高い
3. **削除時のCASCADE処理が複雑** - バグのリスク
4. **ORMとの相性が悪い** - JPAなどで扱いづらい
5. **保守コストが高い** - トリガーのデバッグが困難

### 5.4 案Cの位置付け（採用決定）

**採用理由**:
- ✅ **将来的なチーム拡大を見据えた選択**
- ✅ 初学者が多く参画する想定
- ✅ 新規参画コスト15-20%削減
- ✅ テーブル構造が直感的で、責務が明確

**案Aとの比較**:
- 案A: テーブル数が少なくシンプル（3テーブル）
- 案C: テーブル数は1つ増えるが（4テーブル）、初学者に優しい設計

---

## 6. 実装ロードマップ

### Phase 1: MVPリリース（現在）

```
採用: 案C（明示的な2つの関連テーブル）

実装内容:
- users, profiles, user_profiles, profile_profile_relations テーブル
- profile_profile_relations で階層構造を管理
- 再帰CTEで階層クエリ
```

### Phase 2: パフォーマンス監視

```
監視項目:
- 階層クエリの実行時間
- 権限チェックの実行時間
- プロフィール階層の平均深度
```

### Phase 3: 最適化（必要に応じて）

```
実施内容:
- profile_closure テーブル追加（案D）
- トリガーで自動更新
- クエリの段階的な最適化
```

---

## 7. まとめ

### 7.1 DBMS互換性と開発者オンボーディングを重視した場合の結論

**推奨: 案C（明示的な2つの関連テーブル）**

**理由**:
1. ✅ **PostgreSQL、MySQL、Oracle全てで完全動作**
2. ✅ **DDL構文の差異が最小** - 移行スクリプトの自動生成が容易
3. ✅ **外部キー制約による参照整合性が保証**
4. ✅ **再帰CTEが全DBMSで利用可能**（Oracle は CONNECT BY も可）
5. ✅ **新規参画コスト15-20%削減** - 自己参照FKの学習不要
6. ✅ **初学者に優しい設計** - 全ての関連が「AとBの関連」パターン
7. ✅ **テーブルの責務が明確** - 関心の分離による保守性向上

**案Bを避けるべき理由**:
1. ❌ 外部キー制約が設定できない
2. ❌ トリガーによる整合性チェックがDBMS依存
3. ❌ 移行時のコストが高い（トリガーの書き換え）

**案Aとの比較**:
- 案A: テーブル数が少ない（3テーブル）、実装がやや簡単
- 案C: テーブル数が1つ多い（4テーブル）、初学者の学習コスト低い
- **結論**: 将来的なチーム拡大を見据えて案Cを採用

**将来の拡張性**:
- 必要に応じて案D（閉包テーブル）に移行可能
- 移行パスが明確で、リスクが低い

### 7.2 開発者オンボーディングコストを重視した場合の結論

**推奨: 案C（明示的な2テーブル）**

詳細な分析については、以下のドキュメントを参照してください:
- [開発者オンボーディング比較: 案A vs 案C](DEVELOPER_ONBOARDING_COMPARISON.md)

**理由**:
1. ✅ **自己参照FKの学習が不要** - 初心者の学習コスト2-2.5時間削減（15-20%減）
2. ✅ **テーブル構造が直感的** - 各テーブルの責務が明確（関心の分離）
3. ✅ **エラーメッセージが分かりやすい** - 外部キー制約違反の原因特定が容易
4. ✅ **SQL初心者に優しい** - 自己JOINの概念が不要

**重要な事実**:
- ⚠️ **再帰CTEは両案とも必須** - 階層全取得には避けられない
- 削減できるのは「自己参照外部キー」の学習コストのみ（全体の15-20%）

**トレードオフ**:
- テーブル数が1つ増える（4テーブル）
- 基本操作（CRUD）がやや複雑（子プロフィール作成時に2エンティティ保存）

**適用条件**:
- 初学者が多く参画するチーム
- フロントエンド主体のフルスタックチーム
- 新規参画の頻度が高いプロジェクト

---

**次のステップ**: 案Cで実装を進める

**採用決定**: 案C（明示的な2つの関連テーブル）

**実装方針**:
- users, profiles, user_profiles, profile_profile_relations の4テーブルで実装
- 新規参画コスト15-20%削減を優先
- 将来的にチームが拡大した際の学習コストを最小化
- パフォーマンス要件に応じてClosure Table（案D相当）への拡張を検討
