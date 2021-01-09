package company.victoria.ecool.payload.course;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ParagraphRequest {

    @NotBlank(message = "Content is required")
    private String content;

    private String imageUrl;
}
