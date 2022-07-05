package com.cheocharm.MapZ.group.domain;

import com.cheocharm.MapZ.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Table(name = "Groups")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class GroupEntity extends BaseEntity {

    @Column(name = "group_name", unique = true)
    private String groupName;

    private String bio;

    @Column(name = "group_image_url")
    private String groupImageUrl;

    private String groupUUID;

    @Builder
    public GroupEntity(String groupName, String bio, String groupImageUrl, String groupUUID) {
        this.groupName = groupName;
        this.bio = bio;
        this.groupImageUrl = groupImageUrl;
        this.groupUUID = groupUUID;
    }
}
