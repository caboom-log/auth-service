package site.caboomlog.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.caboomlog.authservice.entity.Role;

public interface RoleRepository extends JpaRepository<Role, String> {
}
