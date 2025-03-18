package site.caboomlog.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.caboomlog.authservice.entity.BlogMemberMapping;

public interface BlogMemberMappingRepository extends JpaRepository<BlogMemberMapping, Long> {
}
