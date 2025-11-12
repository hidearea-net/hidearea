package net.hidearea.core.repository;

import net.hidearea.core.domain.entity.RoleInProfile;
import net.hidearea.core.domain.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserProfileエンティティのリポジトリ
 * <p>
 * ユーザーとプロフィールの中間テーブルのデータアクセスを提供します。
 * ユーザーが所属するプロフィールや、プロフィールのメンバー管理に使用します。
 * </p>
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /**
     * ユーザーが所属する全プロフィールを取得
     *
     * @param userId ユーザーID
     * @return ユーザー・プロフィール関連のリスト
     */
    @Query("SELECT up FROM UserProfile up " +
           "JOIN FETCH up.profile " +
           "WHERE up.user.id = :userId")
    List<UserProfile> findByUserId(@Param("userId") Long userId);

    /**
     * プロフィールに所属する全ユーザーを取得
     *
     * @param profileId プロフィールID
     * @return ユーザー・プロフィール関連のリスト
     */
    @Query("SELECT up FROM UserProfile up " +
           "JOIN FETCH up.user " +
           "WHERE up.profile.id = :profileId")
    List<UserProfile> findByProfileId(@Param("profileId") Long profileId);

    /**
     * 特定のユーザーとプロフィールの関連を検索
     *
     * @param userId ユーザーID
     * @param profileId プロフィールID
     * @return ユーザー・プロフィール関連（Optional）
     */
    Optional<UserProfile> findByUserIdAndProfileId(Long userId, Long profileId);

    /**
     * ユーザーがプロフィールに所属しているか確認
     *
     * @param userId ユーザーID
     * @param profileId プロフィールID
     * @return 所属している場合true
     */
    boolean existsByUserIdAndProfileId(Long userId, Long profileId);

    /**
     * プロフィールのオーナーを取得
     *
     * @param profileId プロフィールID
     * @return オーナーのユーザー・プロフィール関連のリスト
     */
    @Query("SELECT up FROM UserProfile up " +
           "WHERE up.profile.id = :profileId " +
           "AND up.roleInProfile = 'OWNER'")
    List<UserProfile> findOwnersByProfileId(@Param("profileId") Long profileId);

    /**
     * ユーザーが特定の役割でプロフィールに所属しているか確認
     *
     * @param userId ユーザーID
     * @param profileId プロフィールID
     * @param roleInProfile プロフィール内の役割
     * @return 所属している場合true
     */
    boolean existsByUserIdAndProfileIdAndRoleInProfile(
        Long userId, Long profileId, RoleInProfile roleInProfile
    );

    /**
     * ユーザーがルートプロフィールを所有しているか取得（案C: 関連テーブル経由）
     *
     * @param userId ユーザーID
     * @return ルートプロフィールのユーザー・プロフィール関連のリスト
     */
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
