package site.caboomlog.authservice.security.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TokenIssueRequest {
    private Long mbNo;
}
