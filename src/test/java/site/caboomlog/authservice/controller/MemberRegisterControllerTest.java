package site.caboomlog.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import site.caboomlog.authservice.service.MemberRegisterService;

import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(value = MemberRegisterController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
class MemberRegisterControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    MemberRegisterService memberRegisterService;

    @Test
    @DisplayName("이메일 인증코드 발송 요청 - 성공")
    void sendVerificationCode() throws Exception {
        // given
        Map<String, String> request = Map.of("email", "caboom@test.com");

        // when & then
        mockMvc.perform(post("/auth/send-verification-code")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("이메일 인증코드 발송 실패 - 잘못된 요청")
    void sendVerificationCodeFail() throws Exception {
        mockMvc.perform(post("/auth/send-verification-code")
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이메일 인증 성공")
    void verifyEmail() throws Exception {
        // given
        Map<String, String> request = Map.of("email", "caboom@test.com", "code", "abc123");
        Mockito.when(memberRegisterService.verifyEmail(anyString(), anyString()))
                        .thenReturn(true);

        // when & then
        mockMvc.perform(post("/auth/verify-email")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true));
    }

    @Test
    @DisplayName("이메일 인증 실패")
    void verifyEmailFail1() throws Exception {
        // given
        Map<String, String> request = Map.of("email", "caboom@test.com", "code", "abc123");
        Mockito.when(memberRegisterService.verifyEmail(anyString(), anyString()))
                .thenReturn(false);

        // when & then
        mockMvc.perform(post("/auth/verify-email")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(false));
    }

    @ParameterizedTest
    @MethodSource("verifyEmailFailInputs")
    @DisplayName("이메일 인증 실패 - 잘못된 요청")
    void verifyEmailFail2(Map<String, String> input) throws Exception {
        mockMvc.perform(post("/auth/verify-email")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> verifyEmailFailInputs() {
        return Stream.of(
                Arguments.of(Map.of("email", "test@test.com")),
                Arguments.of(Map.of("code", "")),
                Arguments.of(Map.of()),
                Arguments.of(Map.of("email", "test", "code", "abc123")),
                Arguments.of(Map.of("email", "test@test.com", "code", "100"))
                );
    }


}