package site.caboomlog.authservice.service;

import site.caboomlog.authservice.dto.RegisterRequest;

public interface MemberRegisterService {
    void sendVerificationCode(String email);

    boolean verifyEmail(String email, String code);

    void register(RegisterRequest request);
}
