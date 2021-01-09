package company.victoria.ecool.payload.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    @NotBlank(message = "Username or email are required")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    private String password;
}
