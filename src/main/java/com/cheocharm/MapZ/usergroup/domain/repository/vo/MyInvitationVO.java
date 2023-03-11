package com.cheocharm.MapZ.usergroup.domain.repository.vo;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MyInvitationVO {

    private Long diaryId;
    private String groupName;
    private LocalDateTime createdAt;

    @QueryProjection
    public MyInvitationVO(Long diaryId, String groupName, LocalDateTime createdAt) {
        this.diaryId = diaryId;
        this.groupName = groupName;
        this.createdAt = createdAt;
    }
}
