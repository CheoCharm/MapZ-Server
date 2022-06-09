package com.cheocharm.MapZ.agreement;

import com.cheocharm.MapZ.common.domain.BaseEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Table(name = "Agreement")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class AgreementEntity extends BaseEntity {

    @JoinColumn(name = "user_id")
    @OneToOne(fetch = FetchType.LAZY)
    private UserEntity userEntity;

    private Boolean userInfoAgreement;

    private Boolean pushAgreement;
}
