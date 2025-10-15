import lombok.Data;

import java.io.Serializable;
@Data
public class RefundResponse implements Serializable {
    private Long orderId;
    private String refundStatus;
    private String refundTransactionId;

}
