package company.victoria.ecool.payload.user;

import company.victoria.ecool.payload.course.CourseSummary;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class FormerProfile extends UserProfile {

    private Long courseCount;

    private Long courseFollowersCount;

    private List<CourseSummary> cours = new ArrayList<>();

    public FormerProfile(UserProfile userResponse){
        super(userResponse);
    }
}
