package jeju.bear.user.dto;

import lombok.Getter;

@Getter
public class ProfileUpdateDto {

    private String nickname;

    private String password;

    private boolean deleteProfileImage;

}
