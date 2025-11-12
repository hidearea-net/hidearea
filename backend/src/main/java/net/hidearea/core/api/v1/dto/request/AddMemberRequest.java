package net.hidearea.core.api.v1.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.hidearea.core.domain.entity.RoleInProfile;

/**
 * プロフィールメンバー追加リクエストDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddMemberRequest {
    @NotBlank(message = "ユーザー名は必須です")
    private String username;

    @NotNull(message = "役割は必須です")
    private RoleInProfile role;
}
