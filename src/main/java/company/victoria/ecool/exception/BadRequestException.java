package company.victoria.ecool.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends AppBaseException {

    public BadRequestException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatusCode() {
        return HttpStatus.BAD_REQUEST;
    }

}
