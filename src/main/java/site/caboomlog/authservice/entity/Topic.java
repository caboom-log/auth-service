package site.caboomlog.authservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "topics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topic_id")
    private Integer topicId;

    @JoinColumn(name = "topic_pid")
    @ManyToOne(fetch = FetchType.LAZY)
    private Topic parentTopic;

    @OneToMany(mappedBy = "parentTopic", fetch = FetchType.LAZY)
    private List<Topic> children = new ArrayList<>();


    @Column(name = "topic_name")
    private String topicName;

    @Column(name = "topic_seq")
    private Integer topicSeq;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
