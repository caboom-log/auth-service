package site.caboomlog.authservice.exception;

public class DuplicateBlogException extends DuplicateException {
    public DuplicateBlogException(String message) {
        super(message);
    }
}
