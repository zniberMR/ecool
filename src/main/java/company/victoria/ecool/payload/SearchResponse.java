package company.victoria.ecool.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SearchResponse {
    private String classType;

    private Object object;

    public SearchResponse(String classType, Object object) {
        this.classType = classType;
        this.object = object;
    }
}
