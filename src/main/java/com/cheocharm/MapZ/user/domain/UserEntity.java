package com.cheocharm.MapZ.user.domain;

import com.cheocharm.MapZ.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Table(name = "User")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class UserEntity extends BaseEntity {

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String username;

    private String password;

    private String userImageUrl;

    private String bio;

    private String fcmToken;

    @Column(length = 600)
    private String refreshToken;

    @Builder
    public UserEntity(String email, String username, String password, String userImageUrl, String bio, String fcmToken, String refreshToken) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.userImageUrl = userImageUrl;
        this.bio = bio;
        this.fcmToken = fcmToken;
        this.refreshToken = refreshToken;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void updateUserImageUrl(String url) { this.userImageUrl = url; }

    public void updatePassword(String password) { this.password = password; }
}
