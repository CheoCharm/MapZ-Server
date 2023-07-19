package com.cheocharm.MapZ.diary.domain;


import com.cheocharm.MapZ.comment.domain.CommentEntity;
import com.cheocharm.MapZ.common.domain.BaseEntity;
import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.like.domain.DiaryLikeEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
@Table(name = "Diary")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class DiaryEntity extends BaseEntity {

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Point point;

    private String address;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity userEntity;

    @JoinColumn(name = "group_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private GroupEntity groupEntity;

    @OneToMany(mappedBy = "diaryEntity")
    private List<CommentEntity> commentEntities = new ArrayList<>();

    @OneToMany(mappedBy = "diaryEntity")
    private List<DiaryLikeEntity> diaryLikeEntities = new ArrayList<>();

    @OneToMany(mappedBy = "diaryEntity")
    private List<DiaryImageEntity> diaryImageEntities = new ArrayList<>();

    @Builder
    public DiaryEntity(String title, String content, Point point, String address, UserEntity userEntity, GroupEntity groupEntity) {
        this.title = title;
        this.content = content;
        this.point = point;
        this.address = address;
        this.userEntity = userEntity;
        this.groupEntity = groupEntity;
    }

    public static DiaryEntity of(UserEntity userEntity, GroupEntity groupEntity, @NotNull String address, Point point) {
        return DiaryEntity.builder()
                .userEntity(userEntity)
                .groupEntity(groupEntity)
                .address(address)
                .point(point)
                .build();
    }

    public void write(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
