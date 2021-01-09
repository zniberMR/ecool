package company.victoria.ecool.payload.user;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class UserRequest {

    private String name;

    private String password;

    private String about;

    private String country;

    private String city;

    private String skills;

    private String imageUrl;

    private String bannerUrl;

}
