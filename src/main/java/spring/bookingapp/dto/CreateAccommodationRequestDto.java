package spring.bookingapp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import spring.bookingapp.model.AccommodationType;

@Data
public class CreateAccommodationRequestDto {
    @NotNull(message = "Type cannot be null")
    private AccommodationType type;

    @NotBlank(message = "Location cannot be blank")
    private String location;

    @NotBlank(message = "Size cannot be blank")
    private String size;

    @NotEmpty(message = "Amenities list cannot be empty")
    private List<String> amenities;

    @NotNull(message = "Daily rate cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Daily rate must be greater than 0")
    private BigDecimal dailyRate;

    @NotNull(message = "Availability cannot be null")
    @Min(value = 0, message = "Availability cannot be negative")
    private Integer availability;
}
