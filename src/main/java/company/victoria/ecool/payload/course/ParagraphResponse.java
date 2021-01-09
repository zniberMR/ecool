package company.victoria.ecool.payload.course;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ParagraphResponse {
    private Long id;

    private String content;

    private String imageUrl;
}
