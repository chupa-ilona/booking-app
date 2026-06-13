package spring.bookingapp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import spring.bookingapp.dto.AccommodationDto;
import spring.bookingapp.dto.CreateAccommodationRequestDto;
import spring.bookingapp.model.Accommodation;

@Mapper(config = spring.bookingapp.config.MapperConfig.class)
public interface AccommodationMapper {

    AccommodationDto toDto(Accommodation accommodation);

    Accommodation toModel(CreateAccommodationRequestDto requestDto);

    void update(CreateAccommodationRequestDto requestDto,
                @MappingTarget Accommodation accommodation);
}
