package com.mapz.api.group.presentation.dto.response;

import com.mapz.domain.domains.usergroup.vo.MyInvitationVO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class MyInvitationResponse {

    private boolean hasNext;
    private List<Invitation> invitationList;

    @Getter
    @AllArgsConstructor
    public static class Invitation {
        private Long groupId;
        private String userName;
        private LocalDateTime createdAt;
    }

    public static MyInvitationResponse of(List<MyInvitationVO> myInvitations, boolean hasNext) {
        final List<MyInvitationResponse.Invitation> list = myInvitations.stream()
                .map(myInvitationVO -> new MyInvitationResponse.Invitation(
                        myInvitationVO.getDiaryId(),
                        myInvitationVO.getGroupName(),
                        myInvitationVO.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new MyInvitationResponse(hasNext, list);
    }
}
