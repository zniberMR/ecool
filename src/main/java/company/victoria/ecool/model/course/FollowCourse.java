package company.victoria.ecool.model.course;

import company.victoria.ecool.model.audit.DateAudit;
import company.victoria.ecool.model.user.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "follow_courses", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "course_id",
                "user_id"
        })
})
@Data
@NoArgsConstructor
public class FollowCourse extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public FollowCourse(Course course, User user) {
        this.course = course;
        this.user = user;
    }
}
