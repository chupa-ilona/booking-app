package spring.bookingapp.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import spring.bookingapp.model.AccommodationType;

@Data
public class AccommodationDto {
    private Long id;
    private AccommodationType type;
    private String location;
    private String size;
    private List<String> amenities;
    private BigDecimal dailyRate;
    private Integer availability;
}
