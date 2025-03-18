package site.caboomlog.authservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import site.caboomlog.authservice.dto.RegisterRequest;
import site.caboomlog.authservice.service.MemberRegisterService;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
public class MemberRegisterController {

    private final MemberRegisterService memberRegisterService;

    public MemberRegisterController(MemberRegisterService memberRegisterService) {
        this.memberRegisterService = memberRegisterService;
    }

    /**
     * 이메일 인증 코드 전송
     *
     * @param request 이메일 정보를 담은 Map 객체 (key: "email")
     * @return 인증 코드 발송 성공 메시지를 담은 ResponseEntity 객체
     * @throws IllegalArgumentException 이메일이 비어있거나 형식이 올바르지 않을 경우 발생
     */
    @PostMapping("/auth/send-verification-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null) {
            throw new IllegalArgumentException("이메일을 입력해 주세요.");
        }
        memberRegisterService.sendVerificationCode(email);
        return ResponseEntity.status(200).body("인증 코드 발송 완료");
    }

    /**
     * 이메일 인증 확인
     *
     * @param request 이메일 및 인증 코드를 담은 Map 객체 (key: "email", "code")
     * @return 인증 성공 여부를 담은 ResponseEntity 객체 (key: "verified")
     * @throws IllegalArgumentException 이메일 또는 코드가 비어있거나 형식이 올바르지 않을 경우 발생
     */
    @PostMapping("/auth/verify-email")
    public ResponseEntity<Map<String, Boolean>> verifyEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        if (email == null || !isValidEmail(email) ||
                code == null || !isValidCode(code)) {
            throw new IllegalArgumentException("요청 형식이 잘못되었습니다.");
        }
        boolean verified = memberRegisterService.verifyEmail(email, code);
        return ResponseEntity.status(200).body(Map.of("verified", verified));
    }

    /**
     * 회원가입 처리
     *
     * @param request 회원가입 정보를 담은 RegisterRequest 객체
     * @param bindingResult 요청 값 검증 결과를 담은 BindingResult 객체
     * @return 회원가입 성공 메시지를 담은 ResponseEntity 객체
     * @throws IllegalArgumentException 요청 값이 올바르지 않거나 필수 값이 누락된 경우 발생
     */
    @PostMapping("/auth/register")
    public ResponseEntity<String> register(@RequestBody @Validated RegisterRequest request,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder builder = new StringBuilder();
            bindingResult.getAllErrors()
                    .forEach(e -> {
                        builder.append(e.getDefaultMessage()).append("\n");
                        log.error("회원가입 요청 오류: {}", e.getDefaultMessage());
                    });
            throw new IllegalArgumentException(builder.toString());
        }
        memberRegisterService.register(request);

        return ResponseEntity.status(201).body("회원가입 성공");
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isValidCode(String code) {
        String codeRegex = "^[a-zA-Z0-9]{6}$";
        Pattern pattern = Pattern.compile(codeRegex);
        Matcher matcher = pattern.matcher(code);
        return matcher.matches();
    }

}
