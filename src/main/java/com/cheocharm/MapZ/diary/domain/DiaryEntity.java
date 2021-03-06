package com.cheocharm.MapZ.diary.domain;


import com.cheocharm.MapZ.common.domain.BaseEntity;
import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Table(name = "Diary")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class DiaryEntity extends BaseEntity {

    private String title;

    private String content;

    private Double latitude;

    private Double longitude;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity userEntity;

    @JoinColumn(name = "group_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private GroupEntity groupEntity;

}
