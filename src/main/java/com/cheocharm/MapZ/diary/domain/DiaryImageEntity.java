package com.cheocharm.MapZ.diary.domain;

import com.cheocharm.MapZ.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@AttributeOverride(name = "id", column = @Column(name = "diary_image_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class DiaryImageEntity extends BaseEntity {

    @Column(name = "diary_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private DiaryEntity diaryEntity;

    @Column(name = "diary_image_url")
    private String diaryImageUrl;
}
