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
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.test.web.servlet.MockMvc;
import site.caboomlog.authservice.advice.CommonAdvice;
import site.caboomlog.authservice.advice.MemberRegisterControllerAdvice;
import site.caboomlog.authservice.dto.RegisterRequest;
import site.caboomlog.authservice.dto.SendVerificationCodeRequest;
import site.caboomlog.authservice.exception.DuplicateBlogException;
import site.caboomlog.authservice.exception.DuplicateMemberException;
import site.caboomlog.authservice.security.CustomAuthenticationFilter;
import site.caboomlog.authservice.service.MemberRegisterService;

import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = MemberRegisterController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        },
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                                CustomAuthenticationFilter.class
                        })
        }
)
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
    void sendVerificationCodeFail_BodyIsBlank() throws Exception {
        mockMvc.perform(post("/auth/send-verification-code")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andDo(print());
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
    void verifyEmailFail_CodeMismatch() throws Exception {
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
    void verifyEmailFail_BadRequest(Map<String, String> input) throws Exception {
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

    @Test
    @DisplayName("회원가입 성공")
    void register() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest(
                "caboom@test.com",
                "세연",
                "asdf1234",
                "asdf1234",
                "010-1234-5678",
                "caboooom"
        );

        // when & then
        mockMvc.perform(post("/auth/register")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("회원가입 실패 - 필수 값 누락")
    void registerFail_MissingFields() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest(
                "",
                "",
                "asdf1234",
                "asdf1234",
                "010-1234-5678",
                "caboooom"
        );

        // when & then
        mockMvc.perform(post("/auth/register")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 불일치")
    void registerFail_PasswordMismatch() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest(
                "caboom@test.com",
                "세연",
                "asdf1234",
                "aaaa1111",
                "010-1234-5678",
                "caboooom"
        );
        Mockito.doThrow(IllegalArgumentException.class).when(memberRegisterService).register(any());

        // when & then
        mockMvc.perform(post("/auth/register")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 형식 오류")
    void registerFail_PasswordFormat() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest(
                "caboom@test.com",
                "세연",
                "aaa",
                "aaa",
                "010-1234-5678",
                "caboooom"
        );

        // when & then
        mockMvc.perform(post("/auth/register")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void registerFail_DuplicateEmail() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest(
                "caboom@test.com",
                "세연",
                "asdf1234",
                "asdf1234",
                "010-1234-5678",
                "caboooom"
        );

        doThrow(new DuplicateMemberException("해당 이메일 또는 전화번호를 가진 회원이 이미 존재합니다."))
                .when(memberRegisterService).register(any());

        // when & then
        mockMvc.perform(post("/auth/register")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 실패 - 블로그 ID 중복")
    void registerFail_DuplicateBlogId() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest(
                "caboom@test.com",
                "세연",
                "asdf1234",
                "asdf1234",
                "010-1234-5678",
                "caboooom"
        );

        doThrow(new DuplicateBlogException("해당 fid를 가진 블로그가 이미 존재합니다."))
                .when(memberRegisterService).register(any());

        // when & then
        mockMvc.perform(post("/auth/register")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andDo(print());
    }
}