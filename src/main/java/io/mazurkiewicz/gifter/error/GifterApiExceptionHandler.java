package io.mazurkiewicz.gifter.error;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GifterApiExceptionHandler {

    @ExceptionHandler({ApiException.class})
    ResponseEntity<Object> handleLockerApiException(ApiException exception, WebRequest request) {
        HttpStatus httpStatusException = getLockerApiErrorHttpStatus(exception);
        ApiError apiError = new ApiError(httpStatusException, exception.getMessage(), exception.getErrorCode());
        log.error(ExceptionUtils.getStackTrace(exception));
        return apiError.toResponse();
    }

    private HttpStatus getLockerApiErrorHttpStatus(ApiException exception) {
        return switch (exception) {
            case NotFoundException _ -> HttpStatus.NOT_FOUND;
            case NotValidException _ -> HttpStatus.BAD_REQUEST;
            case UnauthorizedException _ -> HttpStatus.UNAUTHORIZED;
            case ForbiddenException _ -> HttpStatus.FORBIDDEN;
        };
    }
}
