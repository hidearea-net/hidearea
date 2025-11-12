package net.hidearea.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security設定
 *
 * Phase 5: 基本的なPasswordEncoder設定のみ
 * Phase 6: JWT認証フィルターを追加予定
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * パスワードエンコーダー（BCrypt）
     * 強度10でハッシュ化
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * セキュリティフィルターチェーン設定
     * Phase 5: すべてのエンドポイントを一時的に許可（開発用）
     * Phase 6: JWT認証フィルターを追加し、適切な認証・認可を設定
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF無効化（JWT使用のため）
            .csrf(csrf -> csrf.disable())

            // セッション管理をステートレスに設定
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 認証・認可設定（Phase 5: 一時的にすべて許可）
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
