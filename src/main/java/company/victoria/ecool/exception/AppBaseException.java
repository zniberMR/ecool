package company.victoria.ecool.exception;

import org.springframework.http.HttpStatus;

public abstract class AppBaseException extends RuntimeException {

    public AppBaseException(String message) {
        super(message);
    }

    public abstract HttpStatus getStatusCode();
}
