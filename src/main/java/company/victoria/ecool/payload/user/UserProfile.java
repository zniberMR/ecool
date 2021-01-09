package company.victoria.ecool.payload.user;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
public class UserProfile extends UserSummary {
    
    public UserProfile(UserSummary userSummary) {
        super(userSummary);
    }

    protected String about;

    protected String country;

    protected String city;

    protected String skills;

    protected String bannerUrl;

    protected Boolean followed;

    protected Long followingCount;

    protected Long followersCount;
}
