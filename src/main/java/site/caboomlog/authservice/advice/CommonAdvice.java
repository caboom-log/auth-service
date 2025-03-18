package site.caboomlog.authservice.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.caboomlog.authservice.dto.ErrorResponse;
import site.caboomlog.authservice.exception.DuplicateException;
import site.caboomlog.authservice.exception.ServerErrorException;

@RestControllerAdvice
public class CommonAdvice {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> badRequest(IllegalArgumentException e) {
        return ResponseEntity.status(400)
                .body(new ErrorResponse("400", e.getMessage()));
    }

    @ExceptionHandler(ServerErrorException.class)
    public ResponseEntity<ErrorResponse> serverError(ServerErrorException e) {
        return ResponseEntity.status(500)
                .body(new ErrorResponse("500", e.getMessage()));
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ErrorResponse> duplicateException(DuplicateException e) {
        return ResponseEntity.status(409)
                .body(new ErrorResponse("409", e.getMessage()));
    }
}
