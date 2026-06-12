package spring.bookingapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class UserRegistrationRequestDto {
    @Email
    private String email;

    @NotBlank
    @Length(min = 6, max = 20)
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

}
