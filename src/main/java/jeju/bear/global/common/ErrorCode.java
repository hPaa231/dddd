package jeju.bear.global.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode implements BaseResponseCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러, 관리자에게 문의 바랍니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "금지된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청받은 리소스를 찾을 수 없습니다."),
    EMAIL_CONFLICT(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    NAME_CONFLICT(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    LOGIN_FAIL(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."),
    CONFLICT(HttpStatus.CONFLICT, "리소스가 충돌됩니다.")
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
