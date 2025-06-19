package jeju.bear.global.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode implements BaseResponseCode {
    OK(HttpStatus.OK, "요청이 성공적으로 처리되었습니다."),
    CREATED(HttpStatus.CREATED, "자원이 성공적으로 생성되었습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
