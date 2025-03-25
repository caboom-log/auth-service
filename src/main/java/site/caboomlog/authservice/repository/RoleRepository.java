package site.caboomlog.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.caboomlog.authservice.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByRoleId(String roleId);
}
