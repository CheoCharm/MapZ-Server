package com.cheocharm.MapZ.usergroup.domain;

import com.cheocharm.MapZ.common.domain.BaseEntity;
import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Getter
@Table(name = "Group_Management")
@Where(clause = "deleted=0")
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

    public static UserGroupEntity of(GroupEntity groupEntity, UserEntity userEntity, InvitationStatus invitationStatus, UserRole userRole) {
        return UserGroupEntity.builder()
                .groupEntity(groupEntity)
                .userEntity(userEntity)
                .invitationStatus(invitationStatus)
                .userRole(userRole)
                .build();
    }
    public void updateInvitationStatus() {
        this.invitationStatus = InvitationStatus.ACCEPT;
    }

    public void updateChief(UserGroupEntity chiefUser, UserGroupEntity targetUser) {
        chiefUser.userRole = UserRole.MEMBER;
        targetUser.userRole = UserRole.CHIEF;
    }
}
