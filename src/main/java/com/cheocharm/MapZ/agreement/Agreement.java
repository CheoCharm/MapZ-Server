package com.cheocharm.MapZ.agreement;

import com.cheocharm.MapZ.common.domain.BaseEntity;
import com.cheocharm.MapZ.user.domain.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Table(name = "Agreement")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Agreement extends BaseEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @JoinColumn(name = "user_id")
    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    private Boolean userInfoAgreement;

    private Boolean pushAgreement;

    @Builder
    public Agreement(User user, Boolean userInfoAgreement, Boolean pushAgreement) {
        this.user = user;
        this.userInfoAgreement = userInfoAgreement;
        this.pushAgreement = pushAgreement;
    }

    public static Agreement of(boolean pushAgreement, User user) {
        return Agreement.builder()
                .user(user)
                .pushAgreement(pushAgreement)
                .userInfoAgreement(true)
                .build();
    }

}
