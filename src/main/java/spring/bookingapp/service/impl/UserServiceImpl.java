package spring.bookingapp.service.impl;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.bookingapp.dto.UserRegistrationRequestDto;
import spring.bookingapp.dto.UserResponseDto;
import spring.bookingapp.exception.RegistrationException;
import spring.bookingapp.mapper.UserMapper;
import spring.bookingapp.model.Role;
import spring.bookingapp.model.RoleName;
import spring.bookingapp.model.User;
import spring.bookingapp.repository.RoleRepository;
import spring.bookingapp.repository.UserRepository;
import spring.bookingapp.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponseDto register(UserRegistrationRequestDto requestDto) {
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new RegistrationException("User with email "
                    + requestDto.getEmail() + " already exists");
        }

        User user = userMapper.toModel(requestDto);
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));

        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER);
        user.setRoles(Set.of(customerRole));

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }
}
