package net.hidearea.core.repository;

import net.hidearea.core.domain.entity.Profile;
import net.hidearea.core.domain.entity.ProfileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Profileエンティティのリポジトリ
 * <p>
 * プロフィール情報のデータアクセスを提供します。
 * プロフィール階層の取得には再帰クエリを使用します。
 * </p>
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    /**
     * メールアドレスでプロフィールを検索
     *
     * @param email メールアドレス
     * @return プロフィール（Optional）
     */
    Optional<Profile> findByEmail(String email);

    /**
     * メールアドレスの存在確認
     *
     * @param email メールアドレス
     * @return 存在する場合true
     */
    boolean existsByEmail(String email);

    /**
     * プロフィールタイプで検索
     *
     * @param profileType プロフィールタイプ
     * @return プロフィールのリスト
     */
    List<Profile> findByProfileType(ProfileType profileType);

    /**
     * 公開プロフィールのみ検索
     *
     * @return 公開プロフィールのリスト
     */
    List<Profile> findByIsPublicTrue();

    /**
     * プロフィール名で検索（部分一致）
     *
     * @param keyword 検索キーワード
     * @return プロフィールのリスト
     */
    List<Profile> findByProfileNameContaining(String keyword);

    /**
     * 特定の親プロフィールの子プロフィールを取得（案C: 関連テーブル経由）
     *
     * @param parentId 親プロフィールID
     * @return 子プロフィールのリスト
     */
    @Query("SELECT r.childProfile FROM ProfileProfileRelation r WHERE r.parentProfile.id = :parentId")
    List<Profile> findChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * ルートプロフィール（子として登録されていない）を取得
     *
     * @return ルートプロフィールのリスト
     */
    @Query("""
        SELECT p FROM Profile p
        WHERE NOT EXISTS (
            SELECT 1 FROM ProfileProfileRelation r WHERE r.childProfile.id = p.id
        )
        """)
    List<Profile> findRootProfiles();

    /**
     * プロフィール階層の深さを取得（再帰クエリ）
     *
     * @param profileId プロフィールID
     * @return 階層の深さ（ルートからの深さ）
     */
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

    /**
     * プロフィールのルートを取得（階層の最上位）
     *
     * @param profileId プロフィールID
     * @return ルートプロフィールのID
     */
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
