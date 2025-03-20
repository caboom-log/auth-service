package site.caboomlog.authservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class RegisterRequest {
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private final String email;

    @NotBlank(message = "사용자 이름은 필수 입력 항목입니다.")
    private final String name;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "비밀번호는 최소 8글자이며, 영문과 숫자를 포함해야 합니다.")
    private final String password;

    @JsonProperty("password-confirm")
    private final String passwordConfirm;

    @NotBlank(message = "휴대폰 번호는 필수 입력 항목입니다.")
    @Pattern(
            regexp = "^010-\\d{4}-\\d{4}$",
            message = "휴대폰 번호는 010-0000-0000 형식이어야 합니다."
    )
    private final String mobile;

    @JsonProperty("blog-fid")
    @NotBlank(message = "블로그 아이디는 필수 입력 항목입니다.")
    @Pattern(regexp = "^[a-z][a-z0-9]{3,19}$",
            message = "블로그 아이디는 영문 소문자로 시작해야 하며, 영문 소문자와 숫자로 4~20자리여야 합니다.")
    private final String blogFid;
}