package company.victoria.ecool.payload.course;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SectionResponse {
    private Long id;

    private String title;

    private Long articlesCount;

    private List<ArticleResponse> articles;
}
