package com.cheocharm.MapZ.agreement;

import com.cheocharm.MapZ.common.BaseEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.OverridesAttribute;

@Getter
@AttributeOverride(name = "id", column = @Column(name = "agreement_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class AgreementEntity extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    private UserEntity userEntity;

    private Boolean userInfoAgreement;

    private Boolean pushAgreement;
}
