package com.cheocharm.MapZ.like.domain;

import com.cheocharm.MapZ.common.domain.BaseEntity;
import com.cheocharm.MapZ.diary.domain.Diary;
import com.cheocharm.MapZ.user.domain.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Table(name = "Diary_Like")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class DiaryLike extends BaseEntity {

    @JoinColumn(name = "diary_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Diary diary;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Builder
    public DiaryLike(Diary diary, User user) {
        this.diary = diary;
        this.user = user;
    }

    public static DiaryLike of(Diary diary, User user) {
        return DiaryLike.builder()
                .diary(diary)
                .user(user)
                .build();
    }
}
