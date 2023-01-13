package com.cheocharm.MapZ;

import com.cheocharm.MapZ.common.config.JpaAuditConfig;
import com.cheocharm.MapZ.common.domain.BaseEntity;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.*;

import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ActiveProfiles("test")
@DataJpaTest(includeFilters = @ComponentScan.Filter(
        type = ASSIGNABLE_TYPE,
        classes = {JpaAuditConfig.class, BaseEntity.class}))
public @interface RepositoryTest {
}
