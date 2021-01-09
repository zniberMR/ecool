package company.victoria.ecool.payload.course;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ArticleResponse {
    private Long id;

    private String title;

    private Long paragraphsCount;

    private List<ParagraphResponse> paragraphs;
}
