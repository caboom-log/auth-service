package site.caboomlog.authservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "blog_member_mappings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
public class BlogMemberMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blog_member_mapping_id")
    private Long blogMemberMappingId;

    @JoinColumn(name = "blog_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Blog blog;

    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @JoinColumn(name = "role_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Role role;

    private BlogMemberMapping(Long blogMemberMappingId, Blog blog, Member member, Role role) {
        this.blogMemberMappingId = blogMemberMappingId;
        this.blog = blog;
        this.member = member;
        this.role = role;
    }

    public static BlogMemberMapping ofNewBlogMemberMapping(Blog blog, Member member, Role role) {
        return new BlogMemberMapping(null, blog, member, role);
    }
}
