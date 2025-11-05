package net.hidearea.core.domain.entity;

/**
 * プロフィール内でのユーザーの役割
 */
public enum RoleInProfile {
    /**
     * プロフィール所有者（作成者、完全な権限）
     */
    OWNER,

    /**
     * プロフィールメンバー（参加者）
     */
    MEMBER
}
