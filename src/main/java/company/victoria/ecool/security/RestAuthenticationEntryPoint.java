package company.victoria.ecool.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import company.victoria.ecool.exception.AppExceptionsHandler;
import company.victoria.ecool.exception.UnauthorizedRequest;
import company.victoria.ecool.payload.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(RestAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse,
                         AuthenticationException e) throws IOException, ServletException {
        logger.error("Responding with unauthorized error. Message - {}", e.getMessage());
        //httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getLocalizedMessage());
        httpServletResponse.setContentType("application/json");
        UnauthorizedRequest ex = new UnauthorizedRequest("Unauthorized user");
        ApiResponse details = new ApiResponse(false, ex.getMessage());
        ResponseEntity<ApiResponse> responseEntity = new ResponseEntity<>(details, ex.getStatusCode());

        for (Map.Entry<String, List<String>> header : responseEntity.getHeaders().entrySet()) {
            String key = header.getKey();
            for (String value : header.getValue()) {
                httpServletResponse.addHeader(key, value);
            }
        }
        httpServletResponse.setStatus(responseEntity.getStatusCodeValue());
        httpServletResponse.getWriter().print(new ObjectMapper().writeValueAsString(responseEntity.getBody()));
    }
}
