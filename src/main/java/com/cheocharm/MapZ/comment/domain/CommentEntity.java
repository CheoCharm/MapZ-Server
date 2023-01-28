package com.cheocharm.MapZ.comment.domain;

import com.cheocharm.MapZ.common.domain.BaseEntity;
import com.cheocharm.MapZ.diary.domain.DiaryEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
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
public class CommentEntity extends BaseEntity {

    private String content;

    @ColumnDefault("0")
    private Long parentId;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity userEntity;

    @JoinColumn(name = "diary_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private DiaryEntity diaryEntity;

    @Builder
    public CommentEntity(String content, Long parentId, UserEntity userEntity, DiaryEntity diaryEntity) {
        this.content = content;
        this.parentId = parentId;
        this.userEntity = userEntity;
        this.diaryEntity = diaryEntity;
    }

}
