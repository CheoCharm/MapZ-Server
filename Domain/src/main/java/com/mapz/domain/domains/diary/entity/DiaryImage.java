package com.mapz.domain.domains.diary.entity;

import com.mapz.domain.domains.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Table(name = "Diary_Image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class DiaryImage extends BaseEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @JoinColumn(name = "diary_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Diary diary;

    @Column(name = "diary_image_url")
    private String diaryImageUrl;

    private Integer imageOrder;

    @Builder
    public DiaryImage(Diary diary, String diaryImageUrl, Integer imageOrder) {
        this.diary = diary;
        this.diaryImageUrl = diaryImageUrl;
        this.imageOrder = imageOrder;
    }

    public static DiaryImage of(Diary diary, String imageURL, int imageOrder) {
        return DiaryImage.builder()
                .diary(diary)
                .diaryImageUrl(imageURL)
                .imageOrder(imageOrder)
                .build();
    }
}
