package com.comp5348.dto;
import lombok.Data;
import java.io.Serializable;

@Data
public class DeliveryCancellationRequest implements Serializable {
    private Long orderId;

}