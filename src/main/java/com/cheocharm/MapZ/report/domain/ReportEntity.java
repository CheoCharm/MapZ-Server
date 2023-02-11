package com.cheocharm.MapZ.report.domain;

import com.cheocharm.MapZ.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Table(name = "Report")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ReportEntity extends BaseEntity {

    private Long userId;

    private Long diaryId;

    @Builder
    public ReportEntity(Long userId, Long diaryId) {
        this.userId = userId;
        this.diaryId = diaryId;
    }
}
