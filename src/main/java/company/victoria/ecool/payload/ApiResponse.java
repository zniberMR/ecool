package company.victoria.ecool.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ApiResponse {
    private Boolean success;

    private String message;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> errors;

    public ApiResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
        this.errors = new ArrayList<>();
    }

    public void addError(String error) {
        this.errors.add(error);
    }
}
