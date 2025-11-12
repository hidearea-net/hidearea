package net.hidearea.core.api.v1.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.hidearea.core.domain.entity.ProfileType;

import java.time.LocalDateTime;

/**
 * プロフィール情報DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto {
    private Long id;
    private String email;
    private String profileName;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private ProfileType profileType;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
