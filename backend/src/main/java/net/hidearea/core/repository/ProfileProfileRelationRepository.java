package net.hidearea.core.repository;

import net.hidearea.core.domain.entity.ProfileProfileRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ProfileProfileRelationエンティティのリポジトリ
 * <p>
 * プロフィール階層（親子関係）のデータアクセスを提供します。
 * 再帰クエリを使用して、階層構造の全子孫・全祖先を取得できます。
 * </p>
 */
@Repository
public interface ProfileProfileRelationRepository extends JpaRepository<ProfileProfileRelation, Long> {

    /**
     * 特定の親プロフィールの子プロフィール関連を取得
     *
     * @param parentProfileId 親プロフィールID
     * @return プロフィール・プロフィール関連のリスト
     */
    List<ProfileProfileRelation> findByParentProfileId(Long parentProfileId);

    /**
     * 特定の子プロフィールの親プロフィール関連を取得
     *
     * @param childProfileId 子プロフィールID
     * @return プロフィール・プロフィール関連（Optional）
     */
    Optional<ProfileProfileRelation> findByChildProfileId(Long childProfileId);

    /**
     * 親子関係が存在するか確認
     *
     * @param parentProfileId 親プロフィールID
     * @param childProfileId 子プロフィールID
     * @return 存在する場合true
     */
    boolean existsByParentProfileIdAndChildProfileId(Long parentProfileId, Long childProfileId);

    /**
     * 特定のプロフィールの全子孫を取得（再帰クエリ）
     *
     * @param profileId プロフィールID
     * @return 子孫プロフィールIDのリスト
     */
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

    /**
     * 特定のプロフィールの全祖先を取得（再帰クエリ）
     *
     * @param profileId プロフィールID
     * @return 祖先プロフィールIDのリスト
     */
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
