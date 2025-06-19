package jeju.bear.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FriendRequestDto {

    private Long requestId;

    private Long userId;

    private String nickname;

    private String profileImage;

    private LocalDateTime requestedAt;

    @Builder
    public FriendRequestDto(Long requestId, Long userId, String nickname, LocalDateTime requestedAt, String profileImage) {
        this.requestId = requestId;
        this.userId = userId;
        this.nickname = nickname;
        this.requestedAt = requestedAt;
        this.profileImage = profileImage;
    }

}
