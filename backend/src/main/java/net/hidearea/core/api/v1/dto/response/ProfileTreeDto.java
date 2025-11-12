package net.hidearea.core.api.v1.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * プロフィール階層ツリーDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileTreeDto {
    private ProfileDto profile;

    @Builder.Default
    private List<ProfileTreeDto> children = new ArrayList<>();

    public void addChild(ProfileTreeDto child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
    }
}
