package site.caboomlog.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.caboomlog.authservice.entity.Blog;

public interface BlogRepository extends JpaRepository<Blog, String> {
    boolean existsByBlogFid(String blogFid);
}
