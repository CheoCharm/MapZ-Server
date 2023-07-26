package com.cheocharm.MapZ.comment.domain;

import com.cheocharm.MapZ.comment.presentation.dto.request.CreateCommentRequest;
import com.cheocharm.MapZ.common.domain.BaseEntity;
import com.cheocharm.MapZ.diary.domain.Diary;
import com.cheocharm.MapZ.user.domain.User;
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

    public static Comment of(CreateCommentRequest request, User user, Diary diary) {
        return Comment.builder()
                .content(request.getContent())
                .parentId(request.getParentId())
                .user(user)
                .diary(diary)
                .build();
    }

}
