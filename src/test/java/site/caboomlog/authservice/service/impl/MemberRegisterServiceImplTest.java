package site.caboomlog.authservice.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import site.caboomlog.authservice.dto.RegisterRequest;
import site.caboomlog.authservice.entity.Blog;
import site.caboomlog.authservice.entity.BlogMemberMapping;
import site.caboomlog.authservice.entity.Member;
import site.caboomlog.authservice.entity.Role;
import site.caboomlog.authservice.exception.*;
import site.caboomlog.authservice.repository.BlogMemberMappingRepository;
import site.caboomlog.authservice.repository.BlogRepository;
import site.caboomlog.authservice.repository.MemberRepository;
import site.caboomlog.authservice.repository.RoleRepository;

import java.time.Duration;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberRegisterServiceImplTest {

    @Mock
    JavaMailSender javaMailSender;
    @Mock
    RedisTemplate<String, String> redisTemplate;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    MemberRepository memberRepository;
    @Mock
    BlogRepository blogRepository;
    @Mock
    BlogMemberMappingRepository blogMemberMappingRepository;
    @Mock
    RoleRepository roleRepository;
    @Mock
    MimeMessage mimeMessage;
    @Mock
    ValueOperations<String, String> valueOperations;
    @InjectMocks
    MemberRegisterServiceImpl memberRegisterService;

    @Test
    @DisplayName("이메일 인증코드 전송 - 성공")
    void sendVerificationCode() throws MessagingException {
        // given
        Mockito.when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.doNothing().when(valueOperations)
                .set(anyString(), anyString(), any(Duration.class));

        // when
        memberRegisterService.sendVerificationCode("caboom@test.com");

        // then
        Mockito.verify(javaMailSender, Mockito.times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("이메일 인증코드 전송 - 메일 전송중 예외")
    void sendVerificationCodeFail_SendMail() throws MessagingException {
        // given
        Mockito.when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.doNothing().when(valueOperations)
                .set(anyString(), anyString(), any(Duration.class));
        Mockito.doAnswer(invocation -> {
            throw new MessagingException("메일 전송 실패");
        }).when(javaMailSender).send(any(MimeMessage.class));

        // when & then
        Assertions.assertThrows(
                EmailSendException.class, () -> memberRegisterService.sendVerificationCode("caboom@test.com"));
        Mockito.verify(redisTemplate, Mockito.times(1)).delete(anyString());
    }

    @Test
    @DisplayName("이메일 인증 성공")
    void verifyEmail() {
        // given
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.get(anyString())).thenReturn("abc123");

        // when
        boolean verified = memberRegisterService.verifyEmail("caboom@test.com", "abc123");

        // then
        Assertions.assertTrue(verified);
        Mockito.verify(redisTemplate, Mockito.times(1)).delete(anyString());
    }

    @Test
    @DisplayName("이메일 인증 실패 - 인증 코드 발급 x")
    void verifyEmailFail_CodeNotIssued() {
        // given
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.get(anyString())).thenReturn(null);

        // when & then
        Assertions.assertThrows(VerificationCodeExpiredException.class, () ->
                memberRegisterService.verifyEmail("caboom@test.com", "abc123"));
    }

    @Test
    @DisplayName("이메일 인증 실패 - 인증 코드 다름")
    void verifyEmailFail_CodeMismatch() {
        // given
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.get(anyString())).thenReturn("bbb777");

        // when & then
        Assertions.assertFalse(memberRegisterService.verifyEmail("caboom@test.com", "abc123"));
        Mockito.verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 검증 x")
    void registerFail_PasswordConfirm() {
        // given
        RegisterRequest request = new RegisterRequest(
                "caboom@test.com", "caboom", "asdf1234",
                "aaaabbbb", "010-0000-1111", "caboom"
        );

        // when & then
        Assertions.assertThrows(IllegalArgumentException.class, () -> memberRegisterService.register(request));
    }

    @Test
    @DisplayName("회원가입 실패 - ROLE_OWNER 권한이 DB에 없음")
    void registerFail_DbRole() {
        // given
        RegisterRequest request = new RegisterRequest(
                "caboom@test.com", "caboom", "asdf1234",
                "asdf1234", "010-0000-1111", "caboom"
        );
        Mockito.when(roleRepository.findById("ROLE_OWNER")).thenReturn(Optional.empty());

        // when & then
        Assertions.assertThrows(ServerErrorException.class, () -> memberRegisterService.register(request));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void registerFail_DuplicateEmail() {
        // given
        RegisterRequest request = new RegisterRequest(
                "caboom@test.com", "caboom", "asdf1234",
                "asdf1234", "010-0000-1111", "caboom"
        );
        Role roleOwner = Role.ofNewRole("ROLE_OWNER", "블로그_소유자", null);
        Mockito.when(roleRepository.findById("ROLE_OWNER")).thenReturn(Optional.of(roleOwner));
        Mockito.when(memberRepository.existsByMbEmail(anyString())).thenReturn(true);

        // when & then
        Assertions.assertThrows(DuplicateMemberException.class, () -> memberRegisterService.register(request));
    }

    @Test
    @DisplayName("회원가입 실패 - 전화번호 중복")
    void registerFail_DuplicateMobile() {
        // given
        RegisterRequest request = new RegisterRequest(
                "caboom@test.com", "caboom", "asdf1234",
                "asdf1234", "010-0000-1111", "caboom"
        );
        Role roleOwner = Role.ofNewRole("ROLE_OWNER", "블로그_소유자", null);
        Mockito.when(roleRepository.findById("ROLE_OWNER")).thenReturn(Optional.of(roleOwner));
        Mockito.when(memberRepository.existsByMbEmail(anyString())).thenReturn(false);
        Mockito.when(memberRepository.existsByMbMobile(anyString())).thenReturn(true);

        // when & then
        Assertions.assertThrows(DuplicateMemberException.class, () -> memberRegisterService.register(request));
    }

    @Test
    @DisplayName("회원가입 실패 - blog fid 중복")
    void registerFail_DuplicateBlogFid() {
        // given
        RegisterRequest request = new RegisterRequest(
                "caboom@test.com", "caboom", "asdf1234",
                "asdf1234", "010-0000-1111", "caboom"
        );
        Role roleOwner = Role.ofNewRole("ROLE_OWNER", "블로그_소유자", null);
        Mockito.when(roleRepository.findById("ROLE_OWNER")).thenReturn(Optional.of(roleOwner));
        Mockito.when(memberRepository.existsByMbEmail(anyString())).thenReturn(false);
        Mockito.when(memberRepository.existsByMbMobile(anyString())).thenReturn(false);
        Mockito.when(blogRepository.existsByBlogFid(anyString())).thenReturn(true);

        // when & then
        Assertions.assertThrows(DuplicateBlogException.class, () -> memberRegisterService.register(request));
    }

    @Test
    @DisplayName("회원가입 성공")
    void register() {
        // given
        RegisterRequest request = new RegisterRequest(
                "caboom@test.com", "caboom", "asdf1234",
                "asdf1234", "010-0000-1111", "caboom"
        );
        Role roleOwner = Role.ofNewRole("ROLE_OWNER", "블로그_소유자", null);
        Mockito.when(roleRepository.findById("ROLE_OWNER")).thenReturn(Optional.of(roleOwner));
        Mockito.when(memberRepository.existsByMbEmail(anyString())).thenReturn(false);
        Mockito.when(memberRepository.existsByMbMobile(anyString())).thenReturn(false);
        Mockito.when(blogRepository.existsByBlogFid(anyString())).thenReturn(false);

        // when
        memberRegisterService.register(request);

        // then
        Mockito.verify(memberRepository, Mockito.times(1)).save(any(Member.class));
        Mockito.verify(blogRepository, Mockito.times(1)).save(any(Blog.class));
        Mockito.verify(blogMemberMappingRepository, Mockito.times(1)).save(any(BlogMemberMapping.class));
    }
}