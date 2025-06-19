package jeju.bear.user.service;

import jeju.bear.board.dto.CommentResponseDto;
import jeju.bear.global.common.CustomException;
import jeju.bear.global.common.ErrorCode;
import jeju.bear.global.service.ImageService;
import jeju.bear.user.dto.FriendResponseDto;
import jeju.bear.user.dto.MypagePostDto;
import jeju.bear.user.dto.MypageResponseDto;
import jeju.bear.user.dto.ProfileUpdateDto;
import jeju.bear.user.entity.User;
import jeju.bear.user.repository.FriendRepository;
import jeju.bear.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ImageService imageService;

    @Override
    public MypageResponseDto getMyPage(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if(userOptional.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }
        User user = userOptional.get();
        List<MypagePostDto> posts = user.getPosts().stream().map(MypagePostDto::new).toList();
        List<CommentResponseDto> comments = user.getComments().stream().map(comment -> new CommentResponseDto(comment, user)).toList();
        List<FriendResponseDto> friends = friendRepository.findAcceptedFriends(user).stream().map(friend -> new FriendResponseDto(friend.getId(), user)).toList();

        return MypageResponseDto.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .posts(posts)
                .comments(comments)
                .friends(friends)
                .build();
    }

    @Override
    public void updateProfile(Long userId, ProfileUpdateDto dto, MultipartFile image) {
        Optional<User> userOptional = userRepository.findById(userId);
        if(userOptional.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }
        User user = userOptional.get();

        if(dto != null) {
            // 닉네임 변경
            if(dto.getNickname() != null) {
                String nickname = dto.getNickname();
                Optional<User> other = userRepository.findByNickname(nickname);
                if(other.isPresent()) {
                    throw new CustomException(ErrorCode.NAME_CONFLICT);
                }
                user.updateNickname(nickname);
            }
            // 비밀번호 변경
            if(dto.getPassword() != null) {
                if(user.getProvider() != null) {
                    throw new CustomException(ErrorCode.FORBIDDEN, "소셜 로그인 사용자는 비밀번호 변경이 불가능합니다.");
                }
                String password = bCryptPasswordEncoder.encode(dto.getPassword());
                user.updatePassword(password);
            }
            // 프로필 이미지 삭제
            if(dto.isDeleteProfileImage()) {
                imageService.deleteImage(user.getProfileImage());
            }
        }

        // 프로필 이미지 변경
        if(image != null && !image.isEmpty()) {
            String profileImageUrl = imageService.uploadProfileImage(image);
            imageService.deleteImage(user.getProfileImage());
            user.updateProfileImage(profileImageUrl);
        }

        userRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if(userOptional.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }

        User user = userOptional.get();
        imageService.deleteImage(user.getProfileImage());
        userRepository.delete(user);
    }

}
