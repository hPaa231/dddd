package jeju.bear.auth.service;

import jeju.bear.auth.dto.LoginResponseDto;
import jeju.bear.auth.dto.OAuth2JoinRequestDto;
import jeju.bear.auth.dto.OAuth2LoginRequestDto;
import jeju.bear.auth.model.PrincipalDetails;
import jeju.bear.auth.oauth.provider.GoogleUserInfo;
import jeju.bear.auth.oauth.provider.KakaoUserInfo;
import jeju.bear.auth.oauth.provider.OAuth2UserInfo;
import jeju.bear.global.common.CustomException;
import jeju.bear.global.common.ErrorCode;
import jeju.bear.global.jwt.JwtTokenProvider;
import jeju.bear.global.service.ImageService;
import jeju.bear.user.entity.Role;
import jeju.bear.user.entity.User;
import jeju.bear.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class PrincipalOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final ImageService imageService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String googleTokenUri;

    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String googleUserInfoUri;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String kakaoTokenUri;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String kakaoUserInfoUri;

    @Value("${spring.jwt.access-token.expiration-time}")
    private long accessTokenExpirationMs;

    @Value("${spring.jwt.refresh-token.expiration-time}")
    private long refreshTokenExpirationMs;

    public LoginResponseDto oauth2Login(String code, String provider) throws UnsupportedEncodingException {

        if(code == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        // 1. code를 보내서 accessToken을 받아옴
        String accessToken = getAccessToken(code, provider);

        // 2. accessToken을 보내서 사용자 정보를 받아옴
        Map<String, Object> attributes = getUserInfo(accessToken, provider);
        OAuth2UserInfo userInfo;
        if(provider.equals("google")) {
            userInfo = new GoogleUserInfo(attributes);
        } else if(provider.equals("kakao")) {
            userInfo = new KakaoUserInfo(attributes);
        } else {
            log.info("login failed: 구글, 카카오만 가능 ㅎ_ㅎ");
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        // 회원가입 여부 확인
        User user = findUser(userInfo, provider);

        // jwt 생성
        PrincipalDetails principalDetails = new PrincipalDetails(user, attributes);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
        String jwtAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        String jwtRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // redis에 refreshToken 저장
        refreshTokenService.saveRefreshToken(user.getId().toString(), jwtRefreshToken, Duration.ofMillis(refreshTokenExpirationMs));

        return new LoginResponseDto(
                "Bearer",
                jwtAccessToken,
                accessTokenExpirationMs,
                jwtRefreshToken,
                refreshTokenExpirationMs
        );
    }

    public LoginResponseDto oauth2Join(OAuth2JoinRequestDto dto, String provider, MultipartFile image) throws UnsupportedEncodingException {

        // 이메일, 닉네임 중복 체크
        if(userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.EMAIL_CONFLICT);
        }
        if(userRepository.findByNickname(dto.getNickname()).isPresent()) {
            throw new CustomException(ErrorCode.NAME_CONFLICT);
        }

        String code = dto.getCode();
        if(code == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        // 사용자 정보 받아옴
        String accessToken = getAccessToken(code, provider);
        Map<String, Object> attributes = getUserInfo(accessToken, provider);
        OAuth2UserInfo userInfo;
        if(provider.equals("google")) {
            userInfo = new GoogleUserInfo(attributes);
        } else if(provider.equals("kakao")) {
            userInfo = new KakaoUserInfo(attributes);
        } else {
            log.info("login failed: 구글, 카카오만 가능 ㅎ_ㅎ");
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        // 유저 생성
        User user = createUser(userInfo, dto, provider, image);

        // jwt 생성
        PrincipalDetails principalDetails = new PrincipalDetails(user, attributes);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
        String jwtAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        String jwtRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // redis에 refreshToken 저장
        refreshTokenService.saveRefreshToken(user.getId().toString(), jwtRefreshToken, Duration.ofMillis(refreshTokenExpirationMs));

        return new LoginResponseDto(
                "Bearer",
                jwtAccessToken,
                accessTokenExpirationMs,
                jwtRefreshToken,
                refreshTokenExpirationMs
        );
    }

    // 코드로 access token 받아옴
    private String getAccessToken(String code, String provider) throws UnsupportedEncodingException {

        // provider 체크
        String clientId;
        String clientSecret;
        String redirectUri;
        String tokenUri;

        switch (provider.toLowerCase()) {
            case "google" -> {
                clientId = googleClientId;
                clientSecret = googleClientSecret;
                redirectUri = googleRedirectUri;
                tokenUri = googleTokenUri;
            }
            case "kakao" -> {
                clientId = kakaoClientId;
                clientSecret = null;
                redirectUri = kakaoRedirectUri;
                tokenUri = kakaoTokenUri;
            }
            default -> throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        // token uri로 code를 보내서 accessToken을 받아옴
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", URLDecoder.decode(code, StandardCharsets.UTF_8));
        params.add("client_id", clientId);
        if(clientSecret != null) {
            params.add("client_secret", clientSecret);
        }
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response;
        try {
            response = restTemplate.postForEntity(tokenUri, requestEntity, Map.class);
        } catch (HttpClientErrorException e) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        return response.getBody().get("access_token").toString();
    }

    // accessToken으로 유저 정보 가져오기
    private Map getUserInfo(String accessToken, String provider) {

        // provider 체크
        String userInfoUri = switch (provider.toLowerCase()) {
            case "google" -> googleUserInfoUri;
            case "kakao" -> kakaoUserInfoUri;
            default -> throw new CustomException(ErrorCode.BAD_REQUEST);
        };

        // accessToken을 보내서 유저 정보를 받아옴
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(userInfoUri, HttpMethod.GET, entity, Map.class);
        return response.getBody();

    }

    // username(provider + providerId)으로 유저 검색
    private User findUser(OAuth2UserInfo userInfo, String provider) {
        String providerId = userInfo.getProviderId();
        String username = provider + "_" + providerId;

        Optional<User> userOptional = userRepository.findByUsername(username);
        if(userOptional.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }

        return userOptional.get();
    }

    // 유저 생성
    private User createUser(OAuth2UserInfo userInfo, OAuth2JoinRequestDto dto, String provider, MultipartFile image) {
        String providerId = userInfo.getProviderId();
        String username = provider + "_" + providerId;

        Optional<User> userOptional = userRepository.findByUsername(username);
        if(userOptional.isPresent()) {
            throw new CustomException(ErrorCode.CONFLICT);
        }

        String profileImageUrl = imageService.uploadProfileImage(image);

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode("oauth2-user:" + UUID.randomUUID()))
                .nickname(dto.getNickname())
                .email(dto.getEmail())
                .profileImage(profileImageUrl)
                .role(Role.ROLE_USER)
                .provider(userInfo.getProvider())
                .providerId(providerId)
                .build();
        userRepository.save(user);
        log.info("created new user: " + user.getEmail());

        return user;
    }

}
