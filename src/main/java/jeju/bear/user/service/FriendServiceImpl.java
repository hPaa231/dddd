package jeju.bear.user.service;

import jeju.bear.global.common.CustomException;
import jeju.bear.global.common.ErrorCode;
import jeju.bear.user.dto.FriendRequestDto;
import jeju.bear.user.dto.FriendResponseDto;
import jeju.bear.user.entity.Friend;
import jeju.bear.user.entity.FriendStatus;
import jeju.bear.user.entity.User;
import jeju.bear.user.repository.FriendRepository;
import jeju.bear.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class FriendServiceImpl implements FriendService{

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    @Override
    public void addFriend(User user, Long targetId) {
        if(user.getId().equals(targetId)) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }

        Optional<User> targetOptional = userRepository.findById(targetId);
        if(targetOptional.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 유저입니다.");
        }

        User target = targetOptional.get();

        Optional<Friend> friendOptional = friendRepository.findAcceptedFriendRelation(user, target);
        if(friendOptional.isPresent()) {
            throw new CustomException(ErrorCode.CONFLICT, "이미 요청을 보냈거나 친구 상태입니다.");
        }

        Friend friend = Friend.builder()
                .requester(user)
                .receiver(target)
                .status(FriendStatus.PENDING)
                .build();

        friendRepository.save(friend);

    }

    @Override
    public void cancelRequest(User user, Long requestId) {
        Optional<Friend> requestOptional = friendRepository.findById(requestId);
        if(requestOptional.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }

        Friend request = requestOptional.get();
        if(!request.getRequester().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "요청을 처리할 권한이 없습니다.");
        }
        if(request.getStatus().equals(FriendStatus.ACCEPTED)) {
            throw new CustomException(ErrorCode.CONFLICT, "이미 수락한 요청입니다.");
        }

        friendRepository.delete(request);
    }

    @Override
    public List<FriendResponseDto> getFriendList(User user) {
        List<Friend> friends = friendRepository.findAcceptedFriends(user);

        List<FriendResponseDto> response = new ArrayList<>();
        for(Friend friend : friends) {
            if(friend.getRequester().getId().equals(user.getId())) {
                response.add(new FriendResponseDto(friend.getId(), friend.getReceiver()));
            } else {
                response.add(new FriendResponseDto(friend.getId(), friend.getRequester()));
            }
        }

        return response;
    }

    @Override
    public List<FriendRequestDto> getReceivedRequests(User user) {
        List<Friend> requests = friendRepository.findReceivedRequests(user);

        List<FriendRequestDto> response = new ArrayList<>();
        for(Friend request : requests) {
            User requester = request.getRequester();
            FriendRequestDto friendRequestDto = FriendRequestDto.builder()
                    .requestId(request.getId())
                    .userId(requester.getId())
                    .nickname(requester.getNickname())
                    .profileImage(requester.getProfileImage())
                    .requestedAt(request.getRequestedAt())
                    .build();
            response.add(friendRequestDto);
        }

        return response;
    }

    @Override
    public List<FriendRequestDto> getSentRequests(User user) {
        List<Friend> requests = friendRepository.findSentRequests(user);

        List<FriendRequestDto> response = new ArrayList<>();
        for(Friend request : requests) {
            User receiver = request.getReceiver();
            FriendRequestDto friendRequestDto = FriendRequestDto.builder()
                    .requestId(request.getId())
                    .userId(receiver.getId())
                    .nickname(receiver.getNickname())
                    .profileImage(receiver.getProfileImage())
                    .requestedAt(request.getRequestedAt())
                    .build();
            response.add(friendRequestDto);
        }

        return response;
    }

    @Override
    @Transactional
    public void acceptRequest(User user, Long requestId) {
        Optional<Friend> requestOptional = friendRepository.findById(requestId);
        if(requestOptional.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }

        Friend request = requestOptional.get();
        if(!request.getStatus().equals(FriendStatus.PENDING)) {
            throw new CustomException(ErrorCode.CONFLICT, "이미 수락한 요청입니다.");
        }
        if(!request.getReceiver().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "요청을 처리할 권한이 없습니다.");
        }

        request.accept();
    }

    @Override
    public void rejectRequest(User user, Long requestId) {
        Optional<Friend> requestOptional = friendRepository.findById(requestId);
        if(requestOptional.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }

        Friend request = requestOptional.get();
        if(!request.getReceiver().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "요청을 처리할 권한이 없습니다.");
        }
        if(!request.getStatus().equals(FriendStatus.PENDING)) {
            throw new CustomException(ErrorCode.CONFLICT, "이미 수락한 요청입니다.");
        }

        friendRepository.delete(request);
    }

    @Override
    public void deleteFriend(User user, Long friendId) {
        Optional<Friend> requestOptional = friendRepository.findById(friendId);
        if(requestOptional.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }

        Friend friend = requestOptional.get();
        if(!friend.getReceiver().getId().equals(user.getId()) && !friend.getRequester().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "요청을 처리할 권한이 없습니다.");
        }
        if(!friend.getStatus().equals(FriendStatus.ACCEPTED)) {
            throw new CustomException(ErrorCode.CONFLICT, "친구가 아닙니다.");
        }

        friendRepository.delete(friend);
    }

}
