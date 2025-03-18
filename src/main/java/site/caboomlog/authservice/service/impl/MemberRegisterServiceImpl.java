package site.caboomlog.authservice.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import site.caboomlog.authservice.service.MemberRegisterService;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberRegisterServiceImpl implements MemberRegisterService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final BlogRepository blogRepository;
    private final BlogMemberMappingRepository blogMemberMappingRepository;
    private final RoleRepository roleRepository;

    /**
     * 이메일 인증 코드 전송
     *
     * 주어진 이메일 주소로 6자리 랜덤 인증 코드를 생성해 전송하고,
     * Redis에 저장하여 일정 시간 동안 유효하도록 설정합니다.
     *
     * @param email 인증 코드를 전송할 이메일 주소
     * @throws EmailSendException 이메일 발송 실패 시 발생
     */
    @Override
    public void sendVerificationCode(String email) {
        MimeMessage message = mailSender.createMimeMessage();
        int codeExpirationMinutes = 10;
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("caboom-log 인증 코드");

            String verificationCode = generateVerificationCode();
            redisTemplate.opsForValue()
                    .set(email, verificationCode, Duration.ofMinutes(codeExpirationMinutes));

            helper.setText(String.format("""
                    <h1>Caboom-log 인증 코드</h1>
                        <p>안녕하세요. 아래 코드를 회원가입 화면에 입력해 주세요.</p>
                        <h2 style='color: blue;'> %s </h2>
                    """, verificationCode));

            mailSender.send(message);
        } catch (MessagingException e) {
            redisTemplate.delete(email);
            log.error("sendVerificationCode - 인증 코드 이메일 발송 실패", e);
            throw new EmailSendException("인증 코드 발송 실패했습니다.", e);
        }
    }

    /**
     * 이메일 인증 코드 검증
     *
     * Redis에 저장된 인증 코드와 사용자가 입력한 코드를 비교합니다.
     * 인증에 성공하면 Redis에서 해당 코드를 삭제합니다.
     *
     * @param email 인증을 시도한 이메일 주소
     * @param code 사용자가 입력한 인증 코드
     * @return 인증 성공 여부
     * @throws VerificationCodeExpiredException 인증 코드가 만료된 경우 발생
     */
    @Override
    @Transactional
    public boolean verifyEmail(String email, String code) {
        String verificationCode = redisTemplate.opsForValue().get(email);
        if (verificationCode == null) {
            throw new VerificationCodeExpiredException("인증 코드가 만료되었습니다. 다시 시도해 주세요.");
        }
        boolean verified = code.equals(verificationCode);
        if (verified) {
            redisTemplate.delete(email);
        }
        return verified;
    }

    /**
     * 회원 가입 처리
     *
     * 1. 이메일 중복 확인
     * 2. 휴대폰 번호 중복 확인
     * 3. 블로그 식별자 중복 확인
     * 4. 사용자, 블로그, 블로그 매핑 정보 저장
     *
     * @param request 회원 가입 요청 정보 객체
     * @throws IllegalArgumentException 비밀번호가 일치하지 않을 경우 발생
     * @throws DuplicateMemberException 중복된 이메일 또는 휴대폰 번호가 있을 경우 발생
     * @throws DuplicateBlogException 중복된 블로그 식별자가 있을 경우 발생
     * @throws ServerErrorException 필수 권한이 설정되지 않았을 경우 발생
     */
    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호를 확인해주세요.");
        }

        Member member = Member.ofNewMember(
                request.getEmail(),
                request.getUsername(),
                encodeRawPassword(request.getPassword()),
                request.getMobile()
        );
        Blog blog = Blog.ofNewBlog(
                request.getBlogFid(),
                true,
                request.getUsername() + "'s blog",
                request.getUsername(),
                null
        );
        Optional<Role> roleOwner = roleRepository.findById("ROLE_OWNER");
        if (roleOwner.isEmpty()) {
            log.error("roleRepository.findById(\"ROLE_OWNER\") IS NULL!!");
            throw new ServerErrorException("권한이 존재하지 않음: ROLE_OWNER");
        }

        if (memberRepository.existsByMbEmail(request.getEmail()) ||
                memberRepository.existsByMbMobile(request.getMobile())) {
            throw new DuplicateMemberException("해당 이메일 또는 전화번호를 가진 회원이 이미 존재합니다.");
        }
        if (blogRepository.existsByBlogFid(request.getBlogFid())) {
            throw new DuplicateBlogException("해당 fid를 가진 블로그가 이미 존재합니다.");
        }

        try {
            memberRepository.save(member);
            blogRepository.save(blog);
            blogMemberMappingRepository.save(
                    BlogMemberMapping.ofNewBlogMemberMapping(blog, member, roleOwner.get())
            );
        } catch (ConstraintViolationException e) {
            throw new DuplicateException("해당 데이터가 이미 존재합니다.", e);
        }
    }

    /**
     * 비밀번호 인코딩
     *
     * 사용자 입력 비밀번호를 인코딩합니다.
     *
     * @param rawPassword 사용자 입력 비밀번호
     * @return 인코딩된 비밀번호
     */
    private String encodeRawPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 랜덤 인증 코드 생성
     *
     * 6자리 영문 및 숫자 조합의 인증 코드를 생성합니다.
     *
     * @return 생성된 인증 코드
     */
    private String generateVerificationCode() {
        int codeLength = 6;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder builder = new StringBuilder(codeLength);
        for (int i = 0; i < codeLength; i++) {
            builder.append(chars.charAt(random.nextInt(chars.length())));
        }
        return builder.toString();
    }

}
