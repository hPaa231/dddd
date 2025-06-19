package jeju.bear.auth.service;

import jeju.bear.auth.dto.JoinRequestDto;
import jeju.bear.auth.dto.LoginRequestDto;
import jeju.bear.auth.dto.LoginResponseDto;
import jeju.bear.auth.dto.VerifyCodeDto;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {

    void join(JoinRequestDto joinRequestDto, MultipartFile image);

    LoginResponseDto login(LoginRequestDto loginRequestDto);

    void checkEmailDuplicate(String email);

    void sendEmailVerifyCode(String email);

    void verifyCode(VerifyCodeDto dto);

    void requestTempPassword(String email);

}
