package company.victoria.ecool.payload.user;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
public class UserSummary {
    
    public UserSummary(UserSummary userSummary) {
        this.id             = userSummary.id;
        this.name           = userSummary.name;
        this.username       = userSummary.username;
        this.email          = userSummary.email;
        this.imageUrl       = userSummary.imageUrl;
        this.compteVerified = userSummary.compteVerified;
        this.joinedAt       = userSummary.joinedAt;
    }

    protected Long id;

    protected String name;

    protected String username;

    protected String email;

    protected String imageUrl;

    protected Boolean compteVerified;

    protected Instant joinedAt;
}
