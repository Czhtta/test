import lombok.Data;

import java.io.Serializable;

@Data
public class EmailRequest implements Serializable {
    private String to;
    private String subject;
    private String body;
}
