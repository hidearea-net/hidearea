package net.hidearea.core.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * プロフィールエンティティ
 * <p>
 * ユーザーが作成する複数のプロフィールを管理します。
 * 階層構造は profile_profile_relations テーブルで管理され、
 * このテーブルはシンプルに保たれます。
 * </p>
 */
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

    /**
     * メールアドレス（プロフィールごとに異なる連絡先）
     * <p>
     * ビジネス用、個人用など異なるメールアドレスを使い分けるため、
     * Userエンティティではなく、Profileエンティティで管理
     * </p>
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * プロフィール名
     */
    @Column(name = "profile_name", nullable = false, length = 100)
    private String profileName;

    /**
     * 表示名
     */
    @Column(name = "display_name", length = 100)
    private String displayName;

    /**
     * 自己紹介
     */
    @Column(columnDefinition = "TEXT")
    private String bio;

    /**
     * アバター画像URL
     */
    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    /**
     * プロフィールタイプ
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "profile_type", nullable = false, length = 20)
    @Builder.Default
    private ProfileType profileType = ProfileType.PERSONAL;

    /**
     * 公開フラグ
     */
    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = true;

    /**
     * 作成日時
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新日時
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * プロフィールに所属するユーザー関連
     */
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserProfile> userProfiles = new ArrayList<>();

    /**
     * このプロフィールが親として持つ関係（子プロフィール一覧）
     */
    @OneToMany(mappedBy = "parentProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProfileProfileRelation> childRelations = new ArrayList<>();

    /**
     * このプロフィールが子として持つ関係（親プロフィール）
     */
    @OneToMany(mappedBy = "childProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProfileProfileRelation> parentRelations = new ArrayList<>();
}
