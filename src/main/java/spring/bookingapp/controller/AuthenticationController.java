package spring.bookingapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import spring.bookingapp.dto.UserLoginRequestDto;
import spring.bookingapp.dto.UserLoginResponseDto;
import spring.bookingapp.dto.UserRegistrationRequestDto;
import spring.bookingapp.dto.UserResponseDto;
import spring.bookingapp.security.AuthenticationService;
import spring.bookingapp.service.UserService;

@Tag(name = "Authentication",
        description = "Endpoints for user registration and authentication")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PostMapping
    @RequestMapping("/registration")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    @Operation(summary = "Register a new user",
            description = "Register a new user in the system")
    public UserResponseDto register(@RequestBody @Valid UserRegistrationRequestDto requestDto) {
        return userService.register(requestDto);
    }

    @PostMapping
    @RequestMapping("/login")
    @Operation(summary = "Login user",
            description = "Authenticate a user and return a JWT token")
    public UserLoginResponseDto login(@RequestBody @Valid UserLoginRequestDto requestDto) {
        String token = authenticationService.authenticate(requestDto);
        return new UserLoginResponseDto(token);
    }

}
