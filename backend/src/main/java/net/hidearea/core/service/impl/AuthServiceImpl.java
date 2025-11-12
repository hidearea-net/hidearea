package net.hidearea.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hidearea.core.api.v1.dto.request.LoginRequest;
import net.hidearea.core.api.v1.dto.request.RegisterRequest;
import net.hidearea.core.api.v1.dto.response.AuthResponse;
import net.hidearea.core.api.v1.dto.response.UserDto;
import net.hidearea.core.domain.entity.User;
import net.hidearea.core.domain.entity.UserRole;
import net.hidearea.core.exception.BusinessException;
import net.hidearea.core.exception.ErrorCode;
import net.hidearea.core.exception.UnauthorizedException;
import net.hidearea.core.repository.UserRepository;
import net.hidearea.core.service.AuthService;
import net.hidearea.core.service.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 認証サービス実装クラス
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    // Note: JwtTokenProvider will be implemented in Phase 6
    // private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        // ユーザー名の重複チェック
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // メールアドレスの重複チェック（将来的にProfile側でチェック）
        // Note: emailはProfileエンティティで管理するため、ここでは省略

        // パスワードハッシュ化
        String passwordHash = passwordEncoder.encode(request.getPassword());

        // ユーザーエンティティ作成
        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordHash)
                .role(UserRole.USER)
                .enabled(true)
                .build();

        // 保存
        user = userRepository.save(user);
        log.info("User registered successfully: userId={}", user.getId());

        // JWT生成（Phase 6で実装予定）
        String token = "DUMMY_TOKEN_PHASE6"; // TODO: Replace with actual JWT

        // レスポンス作成
        UserDto userDto = userMapper.toDto(user);
        return AuthResponse.of(token, userDto);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getUsername());

        // ユーザー検索
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS));

        // アカウント有効性チェック
        if (!user.getEnabled()) {
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED, "アカウントが無効化されています");
        }

        // パスワード検証
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS);
        }

        log.info("User logged in successfully: userId={}", user.getId());

        // JWT生成（Phase 6で実装予定）
        String token = "DUMMY_TOKEN_PHASE6"; // TODO: Replace with actual JWT

        // レスポンス作成
        UserDto userDto = userMapper.toDto(user);
        return AuthResponse.of(token, userDto);
    }
}
