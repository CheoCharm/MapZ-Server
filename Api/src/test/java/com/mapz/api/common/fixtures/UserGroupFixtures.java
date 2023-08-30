package com.mapz.api.common.fixtures;

import com.mapz.domain.domains.group.entity.Group;
import com.mapz.domain.domains.user.entity.User;
import com.mapz.domain.domains.usergroup.enums.InvitationStatus;
import com.mapz.domain.domains.usergroup.entity.UserGroup;
import com.mapz.domain.domains.usergroup.enums.UserRole;

import static com.mapz.api.common.fixtures.GroupFixtures.오픈된_그룹;
import static com.mapz.api.common.fixtures.UserFixtures.googleSignUpUser;

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
