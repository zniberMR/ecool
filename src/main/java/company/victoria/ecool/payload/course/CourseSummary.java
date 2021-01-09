package company.victoria.ecool.payload.course;

import company.victoria.ecool.payload.user.UserProfile;
import company.victoria.ecool.payload.user.UserSummary;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
public class CourseSummary {

    protected Long id;

    protected String title;

    protected String description;

    protected String goals;

    protected String prerequisites;

    protected String imageUrl;

    protected UserSummary createdBy;

    protected Instant creationDateTime;

    protected Boolean isFollowed = false;

    public CourseSummary(CourseSummary courseSummary) {
        this.id               = courseSummary.id;
        this.title            = courseSummary.title;
        this.description      = courseSummary.description;
        this.goals            = courseSummary.goals;
        this.prerequisites    = courseSummary.prerequisites;
        this.imageUrl         = courseSummary.imageUrl;
        this.createdBy        = courseSummary.createdBy;
        this.creationDateTime = courseSummary.creationDateTime;
        this.isFollowed       = courseSummary.isFollowed;
    }

    public Boolean getFollowed(){
        return isFollowed;
    }

    public void setFollowed(Boolean isFollowed){
        this.isFollowed = isFollowed;
    }

}
