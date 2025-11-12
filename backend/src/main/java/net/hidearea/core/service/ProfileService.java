package net.hidearea.core.service;

import net.hidearea.core.api.v1.dto.request.AddMemberRequest;
import net.hidearea.core.api.v1.dto.request.CreateProfileRequest;
import net.hidearea.core.api.v1.dto.request.UpdateProfileRequest;
import net.hidearea.core.api.v1.dto.response.ProfileDto;
import net.hidearea.core.api.v1.dto.response.ProfileTreeDto;
import net.hidearea.core.api.v1.dto.response.UserProfileDto;
import net.hidearea.core.domain.entity.RoleInProfile;

import java.util.List;

/**
 * プロフィールサービスインターフェース
 */
public interface ProfileService {

    /**
     * ルートプロフィール作成
     *
     * @param request 作成リクエスト
     * @param username 作成者のユーザー名
     * @return 作成されたプロフィール
     */
    ProfileDto createProfile(CreateProfileRequest request, String username);

    /**
     * 子プロフィール作成
     *
     * @param parentId 親プロフィールID
     * @param request  作成リクエスト
     * @param username 作成者のユーザー名
     * @return 作成されたプロフィール
     */
    ProfileDto createChildProfile(Long parentId, CreateProfileRequest request, String username);

    /**
     * プロフィールID取得
     *
     * @param id プロフィールID
     * @return プロフィール情報
     */
    ProfileDto getProfileById(Long id);

    /**
     * ユーザーのプロフィール一覧取得
     *
     * @param username ユーザー名
     * @return プロフィール一覧
     */
    List<ProfileDto> getProfilesByUser(String username);

    /**
     * プロフィール階層ツリー取得
     *
     * @param id プロフィールID
     * @return プロフィール階層ツリー
     */
    ProfileTreeDto getProfileTree(Long id);

    /**
     * プロフィール更新
     *
     * @param id       プロフィールID
     * @param request  更新リクエスト
     * @param username 更新者のユーザー名
     * @return 更新されたプロフィール
     */
    ProfileDto updateProfile(Long id, UpdateProfileRequest request, String username);

    /**
     * プロフィール削除
     *
     * @param id       プロフィールID
     * @param username 削除者のユーザー名
     */
    void deleteProfile(Long id, String username);

    /**
     * プロフィールメンバー追加
     *
     * @param profileId プロフィールID
     * @param request   メンバー追加リクエスト
     * @param username  実行者のユーザー名
     */
    void addMember(Long profileId, AddMemberRequest request, String username);

    /**
     * プロフィールメンバー削除
     *
     * @param profileId プロフィールID
     * @param userId    削除対象ユーザーID
     * @param username  実行者のユーザー名
     */
    void removeMember(Long profileId, Long userId, String username);

    /**
     * プロフィールのメンバー一覧取得
     *
     * @param profileId プロフィールID
     * @return メンバー一覧
     */
    List<UserProfileDto> getMembers(Long profileId);

    /**
     * ユーザーがプロフィールのオーナーかチェック
     *
     * @param profileId プロフィールID
     * @param username  ユーザー名
     * @return オーナーの場合true
     */
    boolean isOwner(Long profileId, String username);
}
