package site.caboomlog.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class VerifyEmailRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9]{6}$")
    private String code;
}
