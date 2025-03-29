package site.caboomlog.authservice.security;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import site.caboomlog.authservice.security.dto.TokenIssueRequest;
import site.caboomlog.authservice.security.dto.TokenResponse;

@FeignClient(name="token-service")
public interface JwtTokenAdaptor {

    @PostMapping("/token/issue")
    TokenResponse issueToken(@RequestBody TokenIssueRequest request);
}
