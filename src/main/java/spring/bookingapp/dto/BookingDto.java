package spring.bookingapp.dto;

import java.time.LocalDate;
import lombok.Data;
import spring.bookingapp.model.BookingStatus;

@Data
public class BookingDto {
    private Long id;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Long accommodationId;
    private Long userId;
    private BookingStatus status;
}
