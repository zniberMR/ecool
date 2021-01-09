package company.victoria.ecool.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KeyIdClass {
    private Long id;

    private Class classType;

    public KeyIdClass(Long id, Class classType) {
        this.id = id;
        this.classType = classType;
    }
}
