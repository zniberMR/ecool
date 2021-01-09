package company.victoria.ecool.exception;

import org.springframework.http.HttpStatus;

public class AppException extends AppBaseException {
    public AppException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatusCode() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
