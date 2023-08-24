package com.cheocharm.MapZ.common.fixtures;

import com.cheocharm.MapZ.group.domain.Group;
import com.cheocharm.MapZ.user.domain.User;
import com.cheocharm.MapZ.usergroup.domain.InvitationStatus;
import com.cheocharm.MapZ.usergroup.domain.UserGroup;
import com.cheocharm.MapZ.usergroup.domain.UserRole;

import static com.cheocharm.MapZ.common.fixtures.GroupFixtures.오픈된_그룹;
import static com.cheocharm.MapZ.common.fixtures.UserFixtures.googleSignUpUser;

public class UserGroupFixtures {

    public static UserGroup 오픈된그룹_구글가입유저() {
        return UserGroup.of(
                오픈된_그룹, googleSignUpUser(), InvitationStatus.ACCEPT, UserRole.MEMBER
        );
    }

    public static UserGroup userGroupChief(User user, Group group) {
        return UserGroup.builder()
                .user(user)
                .group(group)
                .invitationStatus(InvitationStatus.ACCEPT)
                .userRole(UserRole.CHIEF)
                .build();
    }

    public static UserGroup userGroupMember(User user, Group group) {
        return UserGroup.builder()
                .user(user)
                .group(group)
                .invitationStatus(InvitationStatus.ACCEPT)
                .userRole(UserRole.MEMBER)
                .build();
    }

    public static UserGroup userGroupPendingMember(User user, Group group) {
        return UserGroup.builder()
                .user(user)
                .group(group)
                .invitationStatus(InvitationStatus.PENDING)
                .userRole(UserRole.MEMBER)
                .build();
    }

}
