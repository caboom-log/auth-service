package site.caboomlog.authservice.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.caboomlog.authservice.dto.ErrorResponse;
import site.caboomlog.authservice.exception.DuplicateBlogException;
import site.caboomlog.authservice.exception.DuplicateMemberException;
import site.caboomlog.authservice.exception.EmailSendException;
import site.caboomlog.authservice.exception.VerificationCodeExpiredException;

@RestControllerAdvice
public class MemberRegisterControllerAdvice {

    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<ErrorResponse> emailSendException(EmailSendException e) {
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("500", e.getMessage()));
    }

    @ExceptionHandler(VerificationCodeExpiredException.class)
    public ResponseEntity<ErrorResponse> verificationCodeExpiredException(VerificationCodeExpiredException e) {
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("500", e.getMessage()));
    }

    @ExceptionHandler(DuplicateMemberException.class)
    public ResponseEntity<ErrorResponse> duplicateMemberException(DuplicateMemberException e) {
        return ResponseEntity.status(409)
                .body(new ErrorResponse("409", e.getMessage()));
    }

    @ExceptionHandler(DuplicateBlogException.class)
    public ResponseEntity<ErrorResponse> duplicateMemberException(DuplicateBlogException e) {
        return ResponseEntity.status(409)
                .body(new ErrorResponse("409", e.getMessage()));
    }
}
