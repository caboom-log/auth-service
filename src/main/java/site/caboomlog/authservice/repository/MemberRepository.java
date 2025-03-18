package site.caboomlog.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.caboomlog.authservice.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByMbEmail(String mbEmail);
    boolean existsByMbMobile(String mbMobile);
}
