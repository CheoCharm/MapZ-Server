package com.cheocharm.MapZ.usergroup;

import com.cheocharm.MapZ.common.domain.BaseEntity;
import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Table(name = "Group_Management")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class UserGroupEntity extends BaseEntity {

    @JoinColumn(name = "group_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private GroupEntity groupEntity;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity userEntity;

    @Column(name = "invitation_status")
    @Enumerated(value = EnumType.STRING)
    private InvitationStatus invitationStatus;

    @Enumerated(value = EnumType.STRING)
    private UserRole userRole;

    @Builder
    public UserGroupEntity(GroupEntity groupEntity, UserEntity userEntity, InvitationStatus invitationStatus, UserRole userRole) {
        this.groupEntity = groupEntity;
        this.userEntity = userEntity;
        this.invitationStatus = invitationStatus;
        this.userRole = userRole;
    }

    public void acceptUser() {
        this.invitationStatus = InvitationStatus.ACCEPT;
    }

    public void refuseUser() {
        this.invitationStatus = InvitationStatus.REFUSE;
    }
}
