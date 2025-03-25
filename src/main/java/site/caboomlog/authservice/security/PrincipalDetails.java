package site.caboomlog.authservice.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import site.caboomlog.authservice.entity.Member;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class PrincipalDetails implements UserDetails {

    private final Member member;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return member.getMbPassword();
    }

    @Override
    public String getUsername() {
        return member.getMbEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return member.getWithdrawalAt() == null;
    }

    @Override
    public boolean isAccountNonLocked() {
        return member.getWithdrawalAt() == null;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return member.getWithdrawalAt() == null;
    }

    @Override
    public boolean isEnabled() {
        return member.getWithdrawalAt() == null;
    }
}
