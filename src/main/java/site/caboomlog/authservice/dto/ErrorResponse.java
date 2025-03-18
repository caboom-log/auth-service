package site.caboomlog.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor @Getter
public class ErrorResponse {
    private String errorCode;
    private String errorMessage;
}
