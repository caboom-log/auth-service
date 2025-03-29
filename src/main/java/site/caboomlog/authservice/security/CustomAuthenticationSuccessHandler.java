package site.caboomlog.authservice.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import site.caboomlog.authservice.security.dto.TokenIssueRequest;
import site.caboomlog.authservice.security.dto.TokenResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenAdaptor jwtTokenAdaptor;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        Long mbNo = ((PrincipalDetails) authentication.getPrincipal())
                .getMember().getMbNo();
        log.debug("Send request to token-service/token/issue");
        TokenResponse tokenResponse = jwtTokenAdaptor.issueToken(new TokenIssueRequest(mbNo));
        String accessToken = tokenResponse.getAccessToken();
        String refreshToken = tokenResponse.getRefreshToken();

        log.info("login success: access={}, refresh={}", accessToken, refreshToken);

        String jsonResponse = String.format("{\"accessToken\":\"%s\", \"refreshToken\":\"%s\"}",
                accessToken, refreshToken);

        response.setContentLength(jsonResponse.getBytes(StandardCharsets.UTF_8).length);
        response.setHeader("Connection", "close");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}
