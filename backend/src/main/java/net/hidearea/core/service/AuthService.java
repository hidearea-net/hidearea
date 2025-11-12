package net.hidearea.core.service;

import net.hidearea.core.api.v1.dto.request.LoginRequest;
import net.hidearea.core.api.v1.dto.request.RegisterRequest;
import net.hidearea.core.api.v1.dto.response.AuthResponse;

/**
 * 認証サービスインターフェース
 */
public interface AuthService {

    /**
     * ユーザー登録
     *
     * @param request 登録リクエスト
     * @return 認証レスポンス（JWTトークンとユーザー情報）
     */
    AuthResponse register(RegisterRequest request);

    /**
     * ログイン
     *
     * @param request ログインリクエスト
     * @return 認証レスポンス（JWTトークンとユーザー情報）
     */
    AuthResponse login(LoginRequest request);
}
