package company.victoria.ecool.payload.course;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PartResponse {
    private Long id;

    private String title;

    private Long sectionsCount;

    private List<SectionResponse> sections;
}
