package net.hidearea.core.repository;

import net.hidearea.core.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Userエンティティのリポジトリ
 * <p>
 * ユーザー認証アカウントのデータアクセスを提供します。
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * ユーザー名でユーザーを検索
     *
     * @param username ユーザー名
     * @return ユーザー（Optional）
     */
    Optional<User> findByUsername(String username);

    /**
     * ユーザー名の存在確認
     *
     * @param username ユーザー名
     * @return 存在する場合true
     */
    boolean existsByUsername(String username);

    /**
     * 有効なユーザーをユーザー名で検索
     *
     * @param username ユーザー名
     * @return 有効なユーザー（Optional）
     */
    Optional<User> findByUsernameAndEnabledTrue(String username);
}
