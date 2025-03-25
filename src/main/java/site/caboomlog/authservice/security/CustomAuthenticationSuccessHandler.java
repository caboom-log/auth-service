package site.caboomlog.authservice.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        Long mbNo = ((PrincipalDetails) authentication.getPrincipal())
                .getMember().getMbNo();
        String token = jwtTokenProvider.generateToken(mbNo);

        String jsonResponse = "{\"token\":\"" + token + "\"}";

        response.setContentLength(jsonResponse.getBytes(StandardCharsets.UTF_8).length);
        response.setHeader("Connection", "close");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}
