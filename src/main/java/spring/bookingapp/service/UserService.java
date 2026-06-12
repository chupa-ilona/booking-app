package spring.bookingapp.service;

import spring.bookingapp.dto.UserRegistrationRequestDto;
import spring.bookingapp.dto.UserResponseDto;

public interface UserService {

    UserResponseDto register(UserRegistrationRequestDto requestDto);

}
