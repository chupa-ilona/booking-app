package spring.bookingapp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import spring.bookingapp.dto.UserRegistrationRequestDto;
import spring.bookingapp.dto.UserResponseDto;
import spring.bookingapp.model.User;

@Mapper(config = spring.bookingapp.config.MapperConfig.class)
public interface UserMapper {
    UserResponseDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    User toModel(UserRegistrationRequestDto userDto);
}