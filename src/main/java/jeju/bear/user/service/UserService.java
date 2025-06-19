package jeju.bear.user.service;

import jeju.bear.user.dto.MypageResponseDto;
import jeju.bear.user.dto.ProfileUpdateDto;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    MypageResponseDto getMyPage(Long userId);

    void updateProfile(Long userId, ProfileUpdateDto dto, MultipartFile image);

    void deleteById(Long id);

}
