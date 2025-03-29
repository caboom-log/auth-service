package site.caboomlog.authservice.security.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
}
