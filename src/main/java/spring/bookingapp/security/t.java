package spring.bookingapp.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import spring.bookingapp.dto.UserLoginRequestDto;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public String authenticate(UserLoginRequestDto requestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestDto.getEmail(),
                        requestDto.getPassword()
                )
        );
        return jwtUtil.generateToken(authentication.getName());
    }
}