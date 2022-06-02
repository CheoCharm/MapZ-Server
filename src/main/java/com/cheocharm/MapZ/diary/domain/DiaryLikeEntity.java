package com.cheocharm.MapZ.diary.domain;

import com.cheocharm.MapZ.common.BaseEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@AttributeOverride(name = "id", column = @Column(name = "diary_like_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class DiaryLikeEntity extends BaseEntity {

    @Column(name = "diary_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private DiaryEntity diaryEntity;

    @Column(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity userEntity;
}
