package site.caboomlog.authservice.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import site.caboomlog.authservice.entity.Member;
import site.caboomlog.authservice.repository.MemberRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByMbEmail(username).orElse(null);
        if (member == null) {
            log.info("Member not found: {}", username);
            return null;
        }
        return new PrincipalDetails(member);
    }
}
