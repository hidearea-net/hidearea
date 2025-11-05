package net.hidearea.core.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * ユーザー・プロフィール関連エンティティ（中間テーブル）
 * <p>
 * ユーザーとプロフィールの多対多関係を管理します。
 * 1人のユーザーが複数のプロフィールを持ち、
 * 1つのプロフィールに複数のユーザーが所属できます。
 * </p>
 */
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

    /**
     * ユーザー
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * プロフィール
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    /**
     * プロフィール内での役割
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role_in_profile", nullable = false, length = 20)
    @Builder.Default
    private RoleInProfile roleInProfile = RoleInProfile.MEMBER;

    /**
     * 参加日時
     */
    @CreatedDate
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;
}
