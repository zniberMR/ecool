package company.victoria.ecool.payload.course;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Data
public class ArticleRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private List<ParagraphRequest> paragraphs = new ArrayList();

}
