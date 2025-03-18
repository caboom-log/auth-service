package site.caboomlog.authservice.exception;

public class DuplicateMemberException extends DuplicateException {
    public DuplicateMemberException(String message) {
        super(message);
    }

    public DuplicateMemberException(String message, Throwable cause) {
        super(message, cause);
    }
}
