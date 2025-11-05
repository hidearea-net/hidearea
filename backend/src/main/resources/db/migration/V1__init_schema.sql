-- HideArea Database Schema - Initial Migration
-- Version: 1.0
-- Description: Creates users, profiles, user_profiles, and profile_profile_relations tables

-- ========================================
-- Users テーブル
-- ========================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE users IS 'ユーザーアカウント情報';
COMMENT ON COLUMN users.id IS 'ユーザーID（主キー）';
COMMENT ON COLUMN users.username IS 'ユーザー名（ログインID）';
COMMENT ON COLUMN users.password_hash IS 'パスワードハッシュ（BCrypt）';
COMMENT ON COLUMN users.role IS '権限（USER, ADMIN）';
COMMENT ON COLUMN users.enabled IS 'アカウント有効フラグ';
COMMENT ON COLUMN users.created_at IS '作成日時';
COMMENT ON COLUMN users.updated_at IS '更新日時';

-- ========================================
-- Profiles テーブル
-- ========================================
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

COMMENT ON TABLE profiles IS 'プロフィール情報';
COMMENT ON COLUMN profiles.id IS 'プロフィールID（主キー）';
COMMENT ON COLUMN profiles.email IS 'メールアドレス（プロフィールごとに異なる連絡先）';
COMMENT ON COLUMN profiles.profile_name IS 'プロフィール名';
COMMENT ON COLUMN profiles.display_name IS '表示名';
COMMENT ON COLUMN profiles.bio IS '自己紹介';
COMMENT ON COLUMN profiles.avatar_url IS 'アバター画像URL';
COMMENT ON COLUMN profiles.profile_type IS 'プロフィールタイプ（PERSONAL, BUSINESS）';
COMMENT ON COLUMN profiles.is_public IS '公開フラグ';
COMMENT ON COLUMN profiles.created_at IS '作成日時';
COMMENT ON COLUMN profiles.updated_at IS '更新日時';

-- ========================================
-- UserProfiles 中間テーブル（ユーザー・プロフィール関連）
-- ========================================
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

COMMENT ON TABLE user_profiles IS 'ユーザーとプロフィールの多対多関連';
COMMENT ON COLUMN user_profiles.id IS '関連ID（主キー）';
COMMENT ON COLUMN user_profiles.user_id IS 'ユーザーID（外部キー）';
COMMENT ON COLUMN user_profiles.profile_id IS 'プロフィールID（外部キー）';
COMMENT ON COLUMN user_profiles.role_in_profile IS 'プロフィール内での役割（OWNER, MEMBER）';
COMMENT ON COLUMN user_profiles.joined_at IS '参加日時';

-- ========================================
-- ProfileProfileRelations 中間テーブル（プロフィール階層関連）
-- ========================================
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

COMMENT ON TABLE profile_profile_relations IS 'プロフィール階層関係';
COMMENT ON COLUMN profile_profile_relations.id IS '関連ID（主キー）';
COMMENT ON COLUMN profile_profile_relations.parent_profile_id IS '親プロフィールID（外部キー）';
COMMENT ON COLUMN profile_profile_relations.child_profile_id IS '子プロフィールID（外部キー）';
COMMENT ON COLUMN profile_profile_relations.created_at IS '作成日時';

-- ========================================
-- Indexes - Users
-- ========================================
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_enabled ON users(enabled);
CREATE INDEX idx_users_created_at ON users(created_at);

-- ========================================
-- Indexes - Profiles
-- ========================================
CREATE INDEX idx_profiles_email ON profiles(email);
CREATE INDEX idx_profiles_profile_type ON profiles(profile_type);
CREATE INDEX idx_profiles_is_public ON profiles(is_public);
CREATE INDEX idx_profiles_profile_name ON profiles(profile_name);
CREATE INDEX idx_profiles_created_at ON profiles(created_at);

-- ========================================
-- Indexes - UserProfiles
-- ========================================
CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX idx_user_profiles_profile_id ON user_profiles(profile_id);
CREATE INDEX idx_user_profiles_role ON user_profiles(role_in_profile);
CREATE INDEX idx_user_profiles_joined_at ON user_profiles(joined_at);

-- ========================================
-- Indexes - ProfileProfileRelations
-- ========================================
CREATE INDEX idx_ppr_parent_profile_id ON profile_profile_relations(parent_profile_id);
CREATE INDEX idx_ppr_child_profile_id ON profile_profile_relations(child_profile_id);
CREATE INDEX idx_ppr_created_at ON profile_profile_relations(created_at);
