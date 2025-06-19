package jeju.bear.user.dto;

import jeju.bear.board.dto.CommentResponseDto;
import jeju.bear.place.entity.Favorite;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class MypageResponseDto {

    private String email; // 필요한가??

    private String nickname;

    private String profileImage;

    private List<MypagePostDto> posts;

    private List<CommentResponseDto> comments;

    private List<Favorite> favorites;

    private List<FriendResponseDto> friends;

    @Builder
    public MypageResponseDto(String email, String nickname, String profileImage, List<MypagePostDto> posts, List<CommentResponseDto> comments, List<Favorite> favorites, List<FriendResponseDto> friends) {
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.posts = posts;
        this.comments = comments;
        this.favorites = favorites;
        this.friends = friends;
    }

}
