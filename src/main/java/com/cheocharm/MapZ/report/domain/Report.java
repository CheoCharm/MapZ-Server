package com.cheocharm.MapZ.report.domain;

import com.cheocharm.MapZ.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Table(name = "Report")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Report extends BaseEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private Long userId;

    private Long diaryId;

    @Builder
    public Report(Long userId, Long diaryId) {
        this.userId = userId;
        this.diaryId = diaryId;
    }

    public static Report of(Long userId, Long diaryId) {
        return Report.builder()
                .userId(userId)
                .diaryId(diaryId)
                .build();
    }
}
