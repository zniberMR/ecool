package company.victoria.ecool.controller;

import company.victoria.ecool.payload.ApiResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@RestController
public class CustomErrorController implements ErrorController {
    private final String ERROR_PATH = "http://localhost:8088/error";

    @RequestMapping("/error")
    public ResponseEntity<?> handleError(HttpServletRequest request) {
        Integer status = (Integer)request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if(status != null){
            String message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE).toString();
            message = message != null && !message.isEmpty() ? message : "An error has occurred see log file";

            return new ResponseEntity(new ApiResponse(false, message), HttpStatus.valueOf(status));
        } else {
            return new ResponseEntity(new ApiResponse(false, "An error has occurred see log file"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }
}
