package com.cheocharm.MapZ.diary.domain;


import com.cheocharm.MapZ.comment.domain.CommentEntity;
import com.cheocharm.MapZ.common.domain.BaseEntity;
import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Table(name = "Diary")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class DiaryEntity extends BaseEntity {

    private String title;

    private String content;

    private Point point;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity userEntity;

    @JoinColumn(name = "group_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private GroupEntity groupEntity;

    @OneToMany(mappedBy = "diaryEntity", cascade = CascadeType.REMOVE)
    private List<CommentEntity> commentEntityList = new ArrayList<>();

    @OneToMany(mappedBy = "diaryEntity", cascade = CascadeType.REMOVE)
    private List<DiaryLikeEntity> diaryLikeEntityList = new ArrayList<>();

    @Builder
    public DiaryEntity(String title, String content, Point point, UserEntity userEntity, GroupEntity groupEntity) {
        this.title = title;
        this.content = content;
        this.point = point;
        this.userEntity = userEntity;
        this.groupEntity = groupEntity;
    }
}
