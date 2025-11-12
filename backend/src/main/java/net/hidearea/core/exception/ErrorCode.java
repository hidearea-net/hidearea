package net.hidearea.core.exception;

/**
 * エラーコード定義
 *
 * アプリケーション全体で使用されるエラーコードを一元管理します。
 */
public enum ErrorCode {
    // 一般エラー
    INTERNAL_SERVER_ERROR("ERR_INTERNAL_001", "内部サーバーエラーが発生しました"),
    INVALID_INPUT("ERR_INPUT_001", "入力値が不正です"),
    VALIDATION_FAILED("ERR_VALIDATION_001", "バリデーションエラーが発生しました"),

    // 認証・認可エラー
    UNAUTHORIZED("ERR_AUTH_001", "認証に失敗しました"),
    INVALID_CREDENTIALS("ERR_AUTH_002", "ユーザー名またはパスワードが正しくありません"),
    TOKEN_EXPIRED("ERR_AUTH_003", "トークンの有効期限が切れています"),
    TOKEN_INVALID("ERR_AUTH_004", "トークンが無効です"),
    ACCESS_DENIED("ERR_AUTH_005", "この操作を実行する権限がありません"),

    // リソース関連エラー
    RESOURCE_NOT_FOUND("ERR_RESOURCE_001", "リソースが見つかりません"),
    USER_NOT_FOUND("ERR_USER_001", "ユーザーが見つかりません"),
    PROFILE_NOT_FOUND("ERR_PROFILE_001", "プロフィールが見つかりません"),

    // ビジネスロジックエラー
    USER_ALREADY_EXISTS("ERR_BIZ_001", "このユーザー名は既に使用されています"),
    EMAIL_ALREADY_EXISTS("ERR_BIZ_002", "このメールアドレスは既に使用されています"),
    PROFILE_EMAIL_ALREADY_EXISTS("ERR_BIZ_003", "このプロフィールメールは既に使用されています"),
    INVALID_PARENT_PROFILE("ERR_BIZ_004", "無効な親プロフィールです"),
    CIRCULAR_REFERENCE("ERR_BIZ_005", "循環参照が検出されました"),
    MAX_DEPTH_EXCEEDED("ERR_BIZ_006", "プロフィール階層の最大深度を超えています"),
    MEMBER_ALREADY_EXISTS("ERR_BIZ_007", "このユーザーは既にプロフィールのメンバーです"),
    NOT_PROFILE_OWNER("ERR_BIZ_008", "プロフィールのオーナーではありません"),
    CANNOT_DELETE_PROFILE_WITH_CHILDREN("ERR_BIZ_009", "子プロフィールが存在するため削除できません"),
    PASSWORD_MISMATCH("ERR_BIZ_010", "現在のパスワードが一致しません");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
