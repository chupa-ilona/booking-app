package spring.bookingapp.dto;

import java.math.BigDecimal;
import lombok.Data;
import spring.bookingapp.model.PaymentStatus;

@Data
public class PaymentDto {
    private Long id;
    private PaymentStatus status;
    private Long bookingId;
    private String sessionUrl;
    private String sessionId;
    private BigDecimal amountToPay;
}
