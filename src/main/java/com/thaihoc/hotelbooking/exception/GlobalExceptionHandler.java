package com.thaihoc.hotelbooking.exception;

import com.thaihoc.hotelbooking.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.nio.file.AccessDeniedException;
import java.util.Comparator;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppException(AppException exception){

        return ResponseEntity
                .status(exception.getErrorCode().getHttpStatusCode())
                .body(
                        ApiResponse.builder()
                                .message(exception.getMessage())
                                .code(exception.getErrorCode().getCode())
                                .build()
                );
    }


    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handlingException(Exception exception){

        return ResponseEntity
                .status(ErrorCode.UNHANDLED_EXCEPTION.getHttpStatusCode())
                .body(
                        ApiResponse.builder()
                                .message(ErrorCode.UNHANDLED_EXCEPTION.getMessage())
                                .code(ErrorCode.UNHANDLED_EXCEPTION.getCode())
                                .build()
                );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse> handleMaxSize(MaxUploadSizeExceededException ex) {

        return ResponseEntity
                .status(ErrorCode.FILE_SIZE_EXCEEDED.getHttpStatusCode())
                .body(
                        ApiResponse.builder()
                                .message(ErrorCode.FILE_SIZE_EXCEEDED.getMessage())
                                .code(ErrorCode.FILE_SIZE_EXCEEDED.getCode())
                                .build()
                );
    }


    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handlingNotValidException(MethodArgumentNotValidException exception) {

        // Lấy danh sách tất cả lỗi
        List<FieldError> errors = exception.getBindingResult().getFieldErrors();

        // Ưu tiên NotBlank -> NotEmpty -> NotNull trước Size
        FieldError fieldError = errors.stream()
                .sorted(Comparator.comparing(err -> {
                    String code = err.getCode();
                    if (code.contains("NotBlank")) return 1;
                    if (code.contains("NotEmpty")) return 2;
                    if (code.contains("NotNull")) return 3;
                    if (code.contains("Size")) return 4;
                    return 99;
                }))
                .findFirst()
                .orElse(errors.get(0));

        String keyErrorCode = fieldError.getDefaultMessage();

        ErrorCode errorCode = ErrorCode.INVALID_KEY;

        try {
            errorCode = ErrorCode.valueOf(keyErrorCode);
        } catch (Exception ignored) {}

        return ResponseEntity
                .status(errorCode.getHttpStatusCode())
                .body(
                        ApiResponse.builder()
                                .code(errorCode.getCode())
                                .message(errorCode.getMessage())
                                .build()
                );
    }


    @ExceptionHandler(value = { AccessDeniedException.class, AuthorizationDeniedException.class })
    public ResponseEntity<ApiResponse> handleAccessDenied(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN) // 403
                .body(ApiResponse.builder()
                        .code(ErrorCode.UNAUTHORIZE.getCode())
                        .message(ErrorCode.UNAUTHORIZE.getMessage())
                        .build());
    }

}
