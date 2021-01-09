package company.victoria.ecool.model.user;

import company.victoria.ecool.model.audit.DateAudit;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "follow_users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "follower_id",
                "following_id"
        })
})
@Data
@NoArgsConstructor
public class FollowUser  extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower; // follow user

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "following_id", nullable = false)
    private User following; // following by user

    public FollowUser(User follower, User following) {
        this.follower  = follower;
        this.following = following;
    }
}
