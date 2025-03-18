package site.caboomlog.authservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "blogs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blog_id")
    private Long blogId;

    @Column(name = "blog_fid")
    private String blogFid;

    @Column(name = "blog_main", columnDefinition = "tinyint")
    private Boolean blogMain;

    @Column(name = "blog_name")
    private String blogName;

    @Column(name = "blog_member_nickname")
    private String blogMbNickname;

    @Column(name = "blog_description", columnDefinition = "text")
    private String blogDescription;

    @Column(name = "blog_is_public", columnDefinition = "tinyint")
    private Boolean blogIsPublic = true;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private  LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Blog(Long blogId, String blogFid, Boolean blogMain, String blogName, String blogMbNickname, String blogDescription, Boolean blogIsPublic, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.blogId = blogId;
        this.blogFid = blogFid;
        this.blogMain = blogMain;
        this.blogName = blogName;
        this.blogMbNickname = blogMbNickname;
        this.blogDescription = blogDescription;
        this.blogIsPublic = blogIsPublic;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Blog ofNewBlog(String blogFid, Boolean blogMain, String blogName,
                                 String blogMbNickname, String blogDescription) {
        return new Blog(null, blogFid, blogMain, blogName, blogMbNickname, blogDescription, true, null, null);
    }
}
