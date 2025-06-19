package jeju.bear.user.dto;

import jeju.bear.user.entity.User;
import lombok.Getter;

@Getter
public class FriendResponseDto {

    private Long friendId;

    private Long userId;

    private String nickname;

    private String profileImage;

    public FriendResponseDto(Long friendId, User user) {
        this.friendId = friendId;
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.profileImage = user.getProfileImage();
    }

}
