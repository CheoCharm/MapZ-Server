package com.cheocharm.MapZ.user.domain;

import com.cheocharm.MapZ.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;

@Getter
@AttributeOverride(name = "id", column = @Column(name = "user_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class UserEntity extends BaseEntity {

    private String email;

    private String username;

    private String password;

    private String userImageUrl;

    private String bio;

    private String fcmToken;

    private String refreshToken;

}
