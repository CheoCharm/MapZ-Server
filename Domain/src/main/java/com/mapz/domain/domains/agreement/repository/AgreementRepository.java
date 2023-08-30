package com.mapz.domain.domains.agreement.repository;

import com.mapz.domain.domains.agreement.entity.Agreement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgreementRepository extends JpaRepository<Agreement, Long> {
}
