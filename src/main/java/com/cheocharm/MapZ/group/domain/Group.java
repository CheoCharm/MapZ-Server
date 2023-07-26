package com.cheocharm.MapZ.group.domain;

import com.cheocharm.MapZ.common.domain.BaseEntity;
import com.cheocharm.MapZ.group.presentation.dto.request.UpdateGroupRequest;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Getter
@Table(name = "DiaryGroup")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Group extends BaseEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "group_name", unique = true)
    private String groupName;

    private String bio;

    @Column(name = "group_image_url")
    private String groupImageUrl;

    private String groupUUID;

    private boolean openStatus;

    @Builder
    public Group(String groupName, String bio, String groupImageUrl, String groupUUID, boolean openStatus) {
        this.groupName = groupName;
        this.bio = bio;
        this.groupImageUrl = groupImageUrl;
        this.groupUUID = groupUUID;
        this.openStatus = openStatus;
    }

    public static Group of(String groupName, String bio, boolean openStatus) {
        return Group.builder()
                .groupName(groupName)
                .bio(bio)
                .groupUUID(UUID.randomUUID().toString())
                .openStatus(openStatus)
                .build();
    }

    public void updateGroupInfo(UpdateGroupRequest updateGroupRequest) {
        this.groupName = updateGroupRequest.getGroupName();
        this.bio = updateGroupRequest.getBio();
        this.openStatus = updateGroupRequest.getIsOpenGroup();
    }

    public void updateGroupImageUrl(String groupImageUrl) {
        this.groupImageUrl = groupImageUrl;
    }
}
