package net.hidearea.core.api.v1.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.hidearea.core.domain.entity.RoleInProfile;

import java.time.LocalDateTime;

/**
 * ユーザー・プロフィール関連DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private UserDto user;
    private ProfileDto profile;
    private RoleInProfile roleInProfile;
    private LocalDateTime joinedAt;
}
