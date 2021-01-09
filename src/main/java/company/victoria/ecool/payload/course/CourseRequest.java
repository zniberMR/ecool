package company.victoria.ecool.payload.course;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Data
public class CourseRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Goals are required")
    private String goals;

    private String prerequisites;

    private List<PartRequest> parts = new ArrayList<>();

}
