package net.hidearea.core.service.mapper;

import lombok.RequiredArgsConstructor;
import net.hidearea.core.api.v1.dto.response.UserProfileDto;
import net.hidearea.core.domain.entity.UserProfile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UserProfile エンティティと UserProfileDto の変換を行うマッパー
 */
@Component
@RequiredArgsConstructor
public class UserProfileMapper {

    private final UserMapper userMapper;
    private final ProfileMapper profileMapper;

    /**
     * UserProfile エンティティを UserProfileDto に変換
     */
    public UserProfileDto toDto(UserProfile userProfile) {
        if (userProfile == null) {
            return null;
        }

        return UserProfileDto.builder()
                .id(userProfile.getId())
                .user(userMapper.toDto(userProfile.getUser()))
                .profile(profileMapper.toDto(userProfile.getProfile()))
                .roleInProfile(userProfile.getRoleInProfile())
                .joinedAt(userProfile.getJoinedAt())
                .build();
    }

    /**
     * UserProfile エンティティのリストを UserProfileDto のリストに変換
     */
    public List<UserProfileDto> toDtoList(List<UserProfile> userProfiles) {
        if (userProfiles == null) {
            return null;
        }

        return userProfiles.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
