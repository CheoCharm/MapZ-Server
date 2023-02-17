package com.cheocharm.MapZ.diary.domain;

import com.cheocharm.MapZ.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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

    private Integer imageOrder;

    @Builder
    public DiaryImageEntity(DiaryEntity diaryEntity, String diaryImageUrl, Integer imageOrder) {
        this.diaryEntity = diaryEntity;
        this.diaryImageUrl = diaryImageUrl;
        this.imageOrder = imageOrder;
    }
}
