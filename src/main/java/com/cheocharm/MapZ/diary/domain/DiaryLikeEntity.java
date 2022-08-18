package com.cheocharm.MapZ.diary.domain;

import com.cheocharm.MapZ.common.domain.BaseEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Table(name = "Diary_Like")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class DiaryLikeEntity extends BaseEntity {

    @JoinColumn(name = "diary_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private DiaryEntity diaryEntity;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity userEntity;

    @Builder
    public DiaryLikeEntity(DiaryEntity diaryEntity, UserEntity userEntity) {
        this.diaryEntity = diaryEntity;
        this.userEntity = userEntity;
    }
}
