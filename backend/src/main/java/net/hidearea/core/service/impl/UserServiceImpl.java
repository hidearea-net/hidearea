package net.hidearea.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hidearea.core.api.v1.dto.request.ChangePasswordRequest;
import net.hidearea.core.api.v1.dto.request.UpdateUserRequest;
import net.hidearea.core.api.v1.dto.response.ProfileDto;
import net.hidearea.core.api.v1.dto.response.UserDto;
import net.hidearea.core.domain.entity.User;
import net.hidearea.core.domain.entity.UserProfile;
import net.hidearea.core.domain.entity.UserRole;
import net.hidearea.core.exception.BusinessException;
import net.hidearea.core.exception.ErrorCode;
import net.hidearea.core.exception.ResourceNotFoundException;
import net.hidearea.core.exception.UnauthorizedException;
import net.hidearea.core.repository.UserProfileRepository;
import net.hidearea.core.repository.UserRepository;
import net.hidearea.core.service.UserService;
import net.hidearea.core.service.mapper.ProfileMapper;
import net.hidearea.core.service.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ユーザーサービス実装クラス
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserMapper userMapper;
    private final ProfileMapper profileMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto getUserById(Long id) {
        log.debug("Getting user by id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User", id));

        return userMapper.toDto(user);
    }

    @Override
    public UserDto getUserByUsername(String username) {
        log.debug("Getting user by username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User", username));

        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UpdateUserRequest request, String currentUsername) {
        log.info("Updating user: id={}, currentUser={}", id, currentUsername);

        // ユーザー取得
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User", id));

        // 権限チェック（自分自身またはADMIN）
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.UNAUTHORIZED));

        if (!user.getId().equals(currentUser.getId()) && currentUser.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException(ErrorCode.ACCESS_DENIED);
        }

        // 更新
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        user = userRepository.save(user);
        log.info("User updated successfully: userId={}", user.getId());

        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id, String currentUsername) {
        log.info("Deleting user: id={}, currentUser={}", id, currentUsername);

        // ユーザー取得
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User", id));

        // 権限チェック（自分自身またはADMIN）
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.UNAUTHORIZED));

        if (!user.getId().equals(currentUser.getId()) && currentUser.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException(ErrorCode.ACCESS_DENIED);
        }

        // 削除
        userRepository.delete(user);
        log.info("User deleted successfully: userId={}", id);
    }

    @Override
    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request, String currentUsername) {
        log.info("Changing password: userId={}, currentUser={}", id, currentUsername);

        // ユーザー取得
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User", id));

        // 権限チェック（自分自身のみ）
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.UNAUTHORIZED));

        if (!user.getId().equals(currentUser.getId())) {
            throw new UnauthorizedException(ErrorCode.ACCESS_DENIED);
        }

        // 現在のパスワード検証
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 新しいパスワードをハッシュ化して保存
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(newPasswordHash);

        userRepository.save(user);
        log.info("Password changed successfully: userId={}", id);
    }

    @Override
    public List<ProfileDto> getUserProfiles(Long id) {
        log.debug("Getting profiles for user: userId={}", id);

        // ユーザー存在確認
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User", id);
        }

        // ユーザー・プロフィール関連を取得
        List<UserProfile> userProfiles = userProfileRepository.findByUserId(id);

        // プロフィールのみを抽出してDTO変換
        return userProfiles.stream()
                .map(UserProfile::getProfile)
                .map(profileMapper::toDto)
                .collect(Collectors.toList());
    }
}
