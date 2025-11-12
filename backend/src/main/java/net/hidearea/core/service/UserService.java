package net.hidearea.core.service;

import net.hidearea.core.api.v1.dto.request.ChangePasswordRequest;
import net.hidearea.core.api.v1.dto.request.UpdateUserRequest;
import net.hidearea.core.api.v1.dto.response.ProfileDto;
import net.hidearea.core.api.v1.dto.response.UserDto;

import java.util.List;

/**
 * ユーザーサービスインターフェース
 */
public interface UserService {

    /**
     * ユーザーIDでユーザーを取得
     *
     * @param id ユーザーID
     * @return ユーザー情報
     */
    UserDto getUserById(Long id);

    /**
     * ユーザー名でユーザーを取得
     *
     * @param username ユーザー名
     * @return ユーザー情報
     */
    UserDto getUserByUsername(String username);

    /**
     * ユーザー情報更新
     *
     * @param id      ユーザーID
     * @param request 更新リクエスト
     * @param currentUsername 現在のユーザー名（権限チェック用）
     * @return 更新後のユーザー情報
     */
    UserDto updateUser(Long id, UpdateUserRequest request, String currentUsername);

    /**
     * ユーザー削除
     *
     * @param id ユーザーID
     * @param currentUsername 現在のユーザー名（権限チェック用）
     */
    void deleteUser(Long id, String currentUsername);

    /**
     * パスワード変更
     *
     * @param id      ユーザーID
     * @param request パスワード変更リクエスト
     * @param currentUsername 現在のユーザー名（権限チェック用）
     */
    void changePassword(Long id, ChangePasswordRequest request, String currentUsername);

    /**
     * ユーザーが所属するプロフィール一覧を取得
     *
     * @param id ユーザーID
     * @return プロフィール一覧
     */
    List<ProfileDto> getUserProfiles(Long id);
}
