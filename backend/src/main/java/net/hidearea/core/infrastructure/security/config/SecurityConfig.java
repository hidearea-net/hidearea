package net.hidearea.core.infrastructure.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 設定
 * <p>
 * 開発段階の暫定設定:
 * - ヘルスチェックエンドポイントは認証不要
 * - 今後、JWT認証を実装予定
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF を無効化（REST API のため）
            .csrf(AbstractHttpConfigurer::disable)

            // セッションをステートレスに設定
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // エンドポイントの認可設定
            .authorizeHttpRequests(authz -> authz
                // ヘルスチェックは認証不要
                .requestMatchers("/api/v1/health").permitAll()

                // H2 Console は開発環境でのみ許可（本番では無効化すること）
                .requestMatchers("/h2-console/**").permitAll()

                // Swagger UI は開発環境でのみ許可
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // その他のエンドポイントは認証が必要（将来実装）
                .anyRequest().authenticated()
            );

        // H2 Console 用のフレーム設定（開発環境のみ）
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
