package site.caboomlog.authservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import site.caboomlog.authservice.dto.LoginRequest;

import java.io.IOException;

@Slf4j
@Component
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper;
    private final AuthenticationSuccessHandler successHandler;
    private final AuthenticationFailureHandler failureHandler;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager,
                                      ObjectMapper objectMapper,
                                      AuthenticationSuccessHandler successHandler,
                                      AuthenticationFailureHandler failureHandler) {
        super.setAuthenticationManager(authenticationManager);
        this.objectMapper = objectMapper;
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        log.debug("Request received in attemptAuthentication");
        if (!request.getMethod().equalsIgnoreCase("POST")) {
            throw new IllegalArgumentException("Request method not supported");
        }
        try (ServletInputStream inputStream = request.getInputStream()) {
            if (inputStream.available() == 0) {
                log.error("Request body is empty");
                throw new IllegalArgumentException("Request body is empty");
            }
            LoginRequest loginRequest = objectMapper.readValue(inputStream, LoginRequest.class);

            log.debug("Login request received: email={}, password={}", loginRequest.getEmail(), loginRequest.getPassword());

            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

            return this.getAuthenticationManager().authenticate(authRequest);
        } catch (IOException e) {
            log.error("Error reading input stream", e);
            throw new RuntimeException("Failed to parse request body");
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException, ServletException {
        log.info("로그인 성공: {}", authResult.getPrincipal());
        SecurityContextHolder.getContext().setAuthentication(authResult);
        successHandler.onAuthenticationSuccess(request, response, authResult);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        log.error("로그인 실패: {}", failed.getMessage());
        failureHandler.onAuthenticationFailure(request, response, failed);
    }

}