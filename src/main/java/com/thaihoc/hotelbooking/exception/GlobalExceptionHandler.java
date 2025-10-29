package com.thaihoc.hotelbooking.exception;

import com.thaihoc.hotelbooking.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingNotValidException(MethodArgumentNotValidException exception){
        String keyErrorCode = exception.getFieldError().getDefaultMessage();

        ErrorCode errorCode = ErrorCode.INVALID_KEY;

        try {
            errorCode = ErrorCode.valueOf(keyErrorCode);
        } catch (Exception e){

        }

        return ResponseEntity
                .status(errorCode.getHttpStatusCode())
                .body(
                        ApiResponse.builder()
                                .code(errorCode.getCode())
                                .message(errorCode.getMessage())
                                .build()
                );
    }

}
