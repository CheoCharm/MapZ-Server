package com.cheocharm.MapZ.diary.domain;

import com.cheocharm.MapZ.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Table(name = "Diary_Image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class DiaryImageEntity extends BaseEntity {

    @JoinColumn(name = "diary_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private DiaryEntity diaryEntity;

    @Column(name = "diary_image_url")
    private String diaryImageUrl;
}
