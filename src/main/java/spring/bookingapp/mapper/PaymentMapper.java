package spring.bookingapp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import spring.bookingapp.dto.PaymentDto;
import spring.bookingapp.model.Payment;

@Mapper(config = spring.bookingapp.config.MapperConfig.class)
public interface PaymentMapper {

    @Mapping(source = "booking.id", target = "bookingId")
    PaymentDto toDto(Payment payment);
}
