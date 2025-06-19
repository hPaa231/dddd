package jeju.bear.board.dto;

import jeju.bear.board.entity.Comment;
import jeju.bear.user.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponseDto {

    private Long userId;

    private String profileImage;

    private String content;

    private LocalDateTime createdAt;

    private boolean isWriter;

    public CommentResponseDto(Comment comment, User user) {
        this.userId = comment.getUser().getId();
        this.profileImage = comment.getUser().getProfileImage();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        if(user != null) {
            this.isWriter = userId.equals(user.getId());
        } else {
            this.isWriter = false;
        }
    }

}
