package spring.bookingapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import spring.bookingapp.dto.UserRegistrationRequestDto;
import spring.bookingapp.dto.UserResponseDto;
import spring.bookingapp.exception.RegistrationException;
import spring.bookingapp.mapper.UserMapper;
import spring.bookingapp.model.Role;
import spring.bookingapp.model.RoleName;
import spring.bookingapp.model.User;
import spring.bookingapp.repository.RoleRepository;
import spring.bookingapp.repository.UserRepository;
import spring.bookingapp.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Register user - Success")
    void register_ValidRequest_ReturnsUserResponseDto() {
        // Given
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("newuser@example.com");
        requestDto.setPassword("SecurePass123!");
        requestDto.setFirstName("John");
        requestDto.setLastName("Doe");

        User user = new User();
        user.setEmail(requestDto.getEmail());
        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());

        Role customerRole = new Role();
        customerRole.setName(RoleName.CUSTOMER);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail(user.getEmail());
        savedUser.setRoles(Set.of(customerRole));

        UserResponseDto expectedDto = new UserResponseDto();
        expectedDto.setId(1L);
        expectedDto.setEmail(savedUser.getEmail());

        when(userRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.empty());
        when(userMapper.toModel(requestDto)).thenReturn(user);
        when(passwordEncoder.encode(requestDto.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.CUSTOMER)).thenReturn(customerRole);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(expectedDto);

        // When
        UserResponseDto actualDto = userService.register(requestDto);

        // Then
        assertEquals(expectedDto.getId(), actualDto.getId());
        assertEquals(expectedDto.getEmail(), actualDto.getEmail());
        verify(userRepository).findByEmail(requestDto.getEmail());
        verify(userRepository).save(user);
        verify(passwordEncoder).encode(requestDto.getPassword());
    }

    @Test
    @DisplayName("Register user - User already exists - Throws RegistrationException")
    void register_UserAlreadyExists_ThrowsException() {
        // Given
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("existing@example.com");

        User existingUser = new User();
        existingUser.setEmail(requestDto.getEmail());

        when(userRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.of(existingUser));

        // When & Then
        Exception exception = assertThrows(RegistrationException.class,
                () -> userService.register(requestDto));

        assertEquals("User with email "
                + requestDto.getEmail() + " already exists", exception.getMessage());
        verify(userRepository).findByEmail(requestDto.getEmail());
        verify(userRepository, times(0)).save(any(User.class));
    }
}
