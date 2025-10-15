import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class DeliveryRequest implements Serializable {
    private Long orderId;
    private String customerAddress;
    private Map<Long, Integer> warehouseAllocations;
}
