package com.mapz.domain.domains.comment.entity;

import com.mapz.domain.domains.BaseEntity;
import com.mapz.domain.domains.diary.entity.Diary;
import com.mapz.domain.domains.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Getter
@Table(name = "Comment")
@Where(clause = "deleted=0")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Comment extends BaseEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String content;

    @ColumnDefault("0")
    private Long parentId;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name = "diary_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Diary diary;

    @Builder
    public Comment(String content, Long parentId, User user, Diary diary) {
        this.content = content;
        this.parentId = parentId;
        this.user = user;
        this.diary = diary;
    }

    public static Comment of(String content, Long parentId, User user, Diary diary) {
        return Comment.builder()
                .content(content)
                .parentId(parentId)
                .user(user)
                .diary(diary)
                .build();
    }

}
