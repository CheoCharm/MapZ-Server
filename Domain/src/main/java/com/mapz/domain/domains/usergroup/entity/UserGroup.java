package com.mapz.domain.domains.usergroup.entity;

import com.mapz.domain.domains.BaseEntity;
import com.mapz.domain.domains.group.entity.Group;
import com.mapz.domain.domains.user.entity.User;
import com.mapz.domain.domains.usergroup.enums.InvitationStatus;
import com.mapz.domain.domains.usergroup.enums.UserRole;
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
public class UserGroup extends BaseEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @JoinColumn(name = "group_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Group group;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(name = "invitation_status")
    @Enumerated(value = EnumType.STRING)
    private InvitationStatus invitationStatus;

    @Enumerated(value = EnumType.STRING)
    private UserRole userRole;

    @Builder
    public UserGroup(Group group, User user, InvitationStatus invitationStatus, UserRole userRole) {
        this.group = group;
        this.user = user;
        this.invitationStatus = invitationStatus;
        this.userRole = userRole;
    }

    public static UserGroup of(Group group, User user, InvitationStatus invitationStatus, UserRole userRole) {
        return UserGroup.builder()
                .group(group)
                .user(user)
                .invitationStatus(invitationStatus)
                .userRole(userRole)
                .build();
    }
    public void updateInvitationStatus() {
        this.invitationStatus = InvitationStatus.ACCEPT;
    }

    public void updateChief(UserGroup chiefUser, UserGroup targetUser) {
        chiefUser.userRole = UserRole.MEMBER;
        targetUser.userRole = UserRole.CHIEF;
    }
}
