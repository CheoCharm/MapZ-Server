package com.cheocharm.MapZ.group.domain;

import com.cheocharm.MapZ.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;

@Getter
@AttributeOverride(name = "id", column = @Column(name = "group_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class GroupEntity extends BaseEntity {

    @Column(name = "group_name", unique = true)
    private String groupName;

    private String bio;

    @Column(name = "group_image_url")
    private String groupImageUrl;

    private String groupUUID;

}
