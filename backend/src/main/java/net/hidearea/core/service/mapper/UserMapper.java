package net.hidearea.core.service.mapper;

import net.hidearea.core.api.v1.dto.response.UserDto;
import net.hidearea.core.domain.entity.User;
import org.springframework.stereotype.Component;

/**
 * User エンティティと UserDto の変換を行うマッパー
 */
@Component
public class UserMapper {

    /**
     * User エンティティを UserDto に変換
     */
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
