package company.victoria.ecool.payload.course;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CourseResponse extends CourseSummary {

    private Long partsCount;

    private List<PartResponse> parts;

    public CourseResponse(CourseSummary courseSummary) {
        super(courseSummary);
    }

}
