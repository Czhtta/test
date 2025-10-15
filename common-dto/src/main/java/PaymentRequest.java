import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PaymentRequest implements Serializable {

    private Long orderId;
    private BigDecimal amount;

//    @Data已经生成了这些方法，相当于加了@Getter @Setter ToString
//    @Override
//    public String toString() {
//        return "PaymentRequest{" +
//                "orderId=" + orderId +
//                ", amount=" + amount +
//                '}';
//    }

}
