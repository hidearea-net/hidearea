package net.hidearea.core.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * プロフィール階層関連エンティティ（中間テーブル）
 * <p>
 * プロフィール間の親子関係を管理します。
 * 企業プロフィール配下のキャラクタープロフィールなど、
 * 階層構造を表現します。
 * </p>
 * <p>
 * 設計方針: 自己参照外部キーを使用せず、明示的な中間テーブルで管理することで、
 * 初学者にも理解しやすい構造を実現しています。
 * </p>
 */
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

    /**
     * 親プロフィール
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_profile_id", nullable = false)
    private Profile parentProfile;

    /**
     * 子プロフィール
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_profile_id", nullable = false)
    private Profile childProfile;

    /**
     * 関係の作成日時
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
