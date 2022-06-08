package com.cheocharm.MapZ.usergroup;

import com.cheocharm.MapZ.common.domain.BaseEntity;
import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@AttributeOverride(name = "id", column = @Column(name = "user_group_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class UserGroupEntity extends BaseEntity {

    @JoinColumn(name = "group_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private GroupEntity groupEntity;

    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity userEntity;

    @Column(name = "invitation_status")
    @Enumerated(value = EnumType.STRING)
    private InvitationStatus invitationStatus;

    @Enumerated(value = EnumType.STRING)
    private UserRole userRole;
}
