package net.hidearea.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hidearea.core.api.v1.dto.request.AddMemberRequest;
import net.hidearea.core.api.v1.dto.request.CreateProfileRequest;
import net.hidearea.core.api.v1.dto.request.UpdateProfileRequest;
import net.hidearea.core.api.v1.dto.response.ProfileDto;
import net.hidearea.core.api.v1.dto.response.ProfileTreeDto;
import net.hidearea.core.api.v1.dto.response.UserProfileDto;
import net.hidearea.core.domain.entity.*;
import net.hidearea.core.exception.BusinessException;
import net.hidearea.core.exception.ErrorCode;
import net.hidearea.core.exception.ResourceNotFoundException;
import net.hidearea.core.exception.UnauthorizedException;
import net.hidearea.core.repository.ProfileProfileRelationRepository;
import net.hidearea.core.repository.ProfileRepository;
import net.hidearea.core.repository.UserProfileRepository;
import net.hidearea.core.repository.UserRepository;
import net.hidearea.core.service.ProfileService;
import net.hidearea.core.service.mapper.ProfileMapper;
import net.hidearea.core.service.mapper.UserProfileMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * プロフィールサービス実装クラス
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final ProfileProfileRelationRepository profileProfileRelationRepository;
    private final ProfileMapper profileMapper;
    private final UserProfileMapper userProfileMapper;

    private static final int MAX_PROFILE_DEPTH = 5; // 最大階層深度

    @Override
    @Transactional
    public ProfileDto createProfile(CreateProfileRequest request, String username) {
        log.info("Creating root profile: profileName={}, user={}", request.getProfileName(), username);

        // ユーザー取得
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.UNAUTHORIZED));

        // メールアドレス重複チェック
        if (profileRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.PROFILE_EMAIL_ALREADY_EXISTS);
        }

        // プロフィール作成
        Profile profile = Profile.builder()
                .email(request.getEmail())
                .profileName(request.getProfileName())
                .displayName(request.getDisplayName())
                .bio(request.getBio())
                .avatarUrl(request.getAvatarUrl())
                .profileType(request.getProfileType())
                .isPublic(request.getIsPublic())
                .build();

        profile = profileRepository.save(profile);

        // UserProfile関連作成（OWNER）
        UserProfile userProfile = UserProfile.builder()
                .user(user)
                .profile(profile)
                .roleInProfile(RoleInProfile.OWNER)
                .build();

        userProfileRepository.save(userProfile);

        log.info("Root profile created: profileId={}", profile.getId());
        return profileMapper.toDto(profile);
    }

    @Override
    @Transactional
    public ProfileDto createChildProfile(Long parentId, CreateProfileRequest request, String username) {
        log.info("Creating child profile: parentId={}, profileName={}, user={}", parentId, request.getProfileName(), username);

        // 親プロフィール存在確認
        Profile parentProfile = profileRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROFILE_NOT_FOUND, "Profile", parentId));

        // 権限チェック（親プロフィールのOWNER）
        if (!isOwner(parentId, username)) {
            throw new UnauthorizedException(ErrorCode.NOT_PROFILE_OWNER);
        }

        // 階層深度チェック
        Integer currentDepth = profileRepository.getProfileDepth(parentId);
        if (currentDepth != null && currentDepth >= MAX_PROFILE_DEPTH) {
            throw new BusinessException(ErrorCode.MAX_DEPTH_EXCEEDED);
        }

        // ユーザー取得
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.UNAUTHORIZED));

        // メールアドレス重複チェック
        if (profileRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.PROFILE_EMAIL_ALREADY_EXISTS);
        }

        // 子プロフィール作成
        Profile childProfile = Profile.builder()
                .email(request.getEmail())
                .profileName(request.getProfileName())
                .displayName(request.getDisplayName())
                .bio(request.getBio())
                .avatarUrl(request.getAvatarUrl())
                .profileType(request.getProfileType())
                .isPublic(request.getIsPublic())
                .build();

        childProfile = profileRepository.save(childProfile);

        // ProfileProfileRelation作成（親子関係）
        ProfileProfileRelation relation = ProfileProfileRelation.builder()
                .parentProfile(parentProfile)
                .childProfile(childProfile)
                .build();

        profileProfileRelationRepository.save(relation);

        // UserProfile関連作成（OWNER）
        UserProfile userProfile = UserProfile.builder()
                .user(user)
                .profile(childProfile)
                .roleInProfile(RoleInProfile.OWNER)
                .build();

        userProfileRepository.save(userProfile);

        log.info("Child profile created: profileId={}, parentId={}", childProfile.getId(), parentId);
        return profileMapper.toDto(childProfile);
    }

    @Override
    public ProfileDto getProfileById(Long id) {
        log.debug("Getting profile by id: {}", id);

        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROFILE_NOT_FOUND, "Profile", id));

        return profileMapper.toDto(profile);
    }

    @Override
    public List<ProfileDto> getProfilesByUser(String username) {
        log.debug("Getting profiles by user: {}", username);

        // ユーザー取得
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User", username));

        // ユーザー・プロフィール関連を取得
        List<UserProfile> userProfiles = userProfileRepository.findByUserId(user.getId());

        // プロフィールのみを抽出してDTO変換
        return userProfiles.stream()
                .map(UserProfile::getProfile)
                .map(profileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProfileTreeDto getProfileTree(Long id) {
        log.debug("Getting profile tree: profileId={}", id);

        // ルートプロフィール取得
        Profile rootProfile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROFILE_NOT_FOUND, "Profile", id));

        // 再帰的にツリー構築
        return buildTree(rootProfile);
    }

    /**
     * プロフィールツリーを再帰的に構築
     */
    private ProfileTreeDto buildTree(Profile profile) {
        ProfileTreeDto treeDto = profileMapper.toTreeDto(profile);

        // 子プロフィール取得
        List<Profile> children = profileRepository.findChildrenByParentId(profile.getId());

        // 再帰的に子ツリー構築
        for (Profile child : children) {
            ProfileTreeDto childTree = buildTree(child);
            treeDto.addChild(childTree);
        }

        return treeDto;
    }

    @Override
    @Transactional
    public ProfileDto updateProfile(Long id, UpdateProfileRequest request, String username) {
        log.info("Updating profile: profileId={}, user={}", id, username);

        // プロフィール取得
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROFILE_NOT_FOUND, "Profile", id));

        // 権限チェック（OWNER）
        if (!isOwner(id, username)) {
            throw new UnauthorizedException(ErrorCode.NOT_PROFILE_OWNER);
        }

        // 更新
        if (request.getDisplayName() != null) {
            profile.setDisplayName(request.getDisplayName());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getIsPublic() != null) {
            profile.setIsPublic(request.getIsPublic());
        }

        profile = profileRepository.save(profile);
        log.info("Profile updated: profileId={}", id);

        return profileMapper.toDto(profile);
    }

    @Override
    @Transactional
    public void deleteProfile(Long id, String username) {
        log.info("Deleting profile: profileId={}, user={}", id, username);

        // プロフィール取得
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROFILE_NOT_FOUND, "Profile", id));

        // 権限チェック（OWNER）
        if (!isOwner(id, username)) {
            throw new UnauthorizedException(ErrorCode.NOT_PROFILE_OWNER);
        }

        // 子プロフィールが存在するかチェック
        List<Profile> children = profileRepository.findChildrenByParentId(id);
        if (!children.isEmpty()) {
            throw new BusinessException(ErrorCode.CANNOT_DELETE_PROFILE_WITH_CHILDREN);
        }

        // 削除
        profileRepository.delete(profile);
        log.info("Profile deleted: profileId={}", id);
    }

    @Override
    @Transactional
    public void addMember(Long profileId, AddMemberRequest request, String username) {
        log.info("Adding member to profile: profileId={}, targetUser={}, role={}", profileId, request.getUsername(), request.getRole());

        // プロフィール取得
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROFILE_NOT_FOUND, "Profile", profileId));

        // 権限チェック（OWNER）
        if (!isOwner(profileId, username)) {
            throw new UnauthorizedException(ErrorCode.NOT_PROFILE_OWNER);
        }

        // 追加対象ユーザー取得
        User targetUser = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User", request.getUsername()));

        // 既にメンバーかチェック
        if (userProfileRepository.existsByUserIdAndProfileId(targetUser.getId(), profileId)) {
            throw new BusinessException(ErrorCode.MEMBER_ALREADY_EXISTS);
        }

        // UserProfile関連作成
        UserProfile userProfile = UserProfile.builder()
                .user(targetUser)
                .profile(profile)
                .roleInProfile(request.getRole())
                .build();

        userProfileRepository.save(userProfile);
        log.info("Member added to profile: profileId={}, userId={}", profileId, targetUser.getId());
    }

    @Override
    @Transactional
    public void removeMember(Long profileId, Long userId, String username) {
        log.info("Removing member from profile: profileId={}, userId={}", profileId, userId);

        // プロフィール存在確認
        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException(ErrorCode.PROFILE_NOT_FOUND, "Profile", profileId);
        }

        // 権限チェック（OWNER）
        if (!isOwner(profileId, username)) {
            throw new UnauthorizedException(ErrorCode.NOT_PROFILE_OWNER);
        }

        // UserProfile関連取得
        UserProfile userProfile = userProfileRepository.findByUserIdAndProfileId(userId, profileId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND, "UserProfile", userId));

        // OWNER自身は削除不可
        if (userProfile.getRoleInProfile() == RoleInProfile.OWNER) {
            throw new BusinessException(ErrorCode.NOT_PROFILE_OWNER, "オーナーは削除できません");
        }

        // 削除
        userProfileRepository.delete(userProfile);
        log.info("Member removed from profile: profileId={}, userId={}", profileId, userId);
    }

    @Override
    public List<UserProfileDto> getMembers(Long profileId) {
        log.debug("Getting members of profile: profileId={}", profileId);

        // プロフィール存在確認
        if (!profileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException(ErrorCode.PROFILE_NOT_FOUND, "Profile", profileId);
        }

        // メンバー取得
        List<UserProfile> userProfiles = userProfileRepository.findByProfileId(profileId);

        return userProfileMapper.toDtoList(userProfiles);
    }

    @Override
    public boolean isOwner(Long profileId, String username) {
        // ユーザー取得
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.UNAUTHORIZED));

        // OWNER権限チェック
        return userProfileRepository.existsByUserIdAndProfileIdAndRoleInProfile(
                user.getId(), profileId, RoleInProfile.OWNER
        );
    }
}
