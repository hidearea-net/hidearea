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
 * ユーザーエンティティ
 * <p>
 * 認証アカウント情報を保持します。
 * emailフィールドは不要 - 認証にはusernameを使用し、
 * メールアドレスは各プロフィールに紐づけます。
 * </p>
 */
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

    /**
     * ユーザー名（ログインID）
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * パスワードハッシュ（BCrypt）
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * ユーザーの権限役割
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.USER;

    /**
     * アカウント有効フラグ
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

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
     * ユーザーが所属するプロフィール関連
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserProfile> userProfiles = new ArrayList<>();
}
