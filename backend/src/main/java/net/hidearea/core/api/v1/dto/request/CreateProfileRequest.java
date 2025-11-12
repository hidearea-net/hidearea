package net.hidearea.core.api.v1.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.hidearea.core.domain.entity.ProfileType;

/**
 * プロフィール作成リクエストDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProfileRequest {
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "有効なメールアドレスを入力してください")
    private String email;

    @NotBlank(message = "プロフィール名は必須です")
    @Size(min = 3, max = 50, message = "プロフィール名は3-50文字である必要があります")
    private String profileName;

    @NotBlank(message = "表示名は必須です")
    @Size(max = 100, message = "表示名は100文字以内である必要があります")
    private String displayName;

    @Size(max = 500, message = "自己紹介は500文字以内である必要があります")
    private String bio;

    private String avatarUrl;

    @NotNull(message = "プロフィールタイプは必須です")
    private ProfileType profileType;

    @NotNull(message = "公開設定は必須です")
    private Boolean isPublic;
}
