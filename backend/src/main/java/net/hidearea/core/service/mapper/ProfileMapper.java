package net.hidearea.core.service.mapper;

import net.hidearea.core.api.v1.dto.response.ProfileDto;
import net.hidearea.core.api.v1.dto.response.ProfileTreeDto;
import net.hidearea.core.domain.entity.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Profile エンティティと ProfileDto の変換を行うマッパー
 */
@Component
public class ProfileMapper {

    /**
     * Profile エンティティを ProfileDto に変換
     */
    public ProfileDto toDto(Profile profile) {
        if (profile == null) {
            return null;
        }

        return ProfileDto.builder()
                .id(profile.getId())
                .email(profile.getEmail())
                .profileName(profile.getProfileName())
                .displayName(profile.getDisplayName())
                .bio(profile.getBio())
                .avatarUrl(profile.getAvatarUrl())
                .profileType(profile.getProfileType())
                .isPublic(profile.getIsPublic())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    /**
     * Profile エンティティのリストを ProfileDto のリストに変換
     */
    public List<ProfileDto> toDtoList(List<Profile> profiles) {
        if (profiles == null) {
            return null;
        }

        return profiles.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Profile エンティティを ProfileTreeDto に変換
     */
    public ProfileTreeDto toTreeDto(Profile profile) {
        if (profile == null) {
            return null;
        }

        return ProfileTreeDto.builder()
                .profile(toDto(profile))
                .build();
    }
}
