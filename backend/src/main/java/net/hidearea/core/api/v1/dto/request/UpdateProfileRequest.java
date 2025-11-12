package net.hidearea.core.api.v1.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * プロフィール更新リクエストDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    @Size(max = 100, message = "表示名は100文字以内である必要があります")
    private String displayName;

    @Size(max = 500, message = "自己紹介は500文字以内である必要があります")
    private String bio;

    private String avatarUrl;

    private Boolean isPublic;
}
