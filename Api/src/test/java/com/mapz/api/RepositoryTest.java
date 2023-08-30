package com.mapz.api;

import com.mapz.domain.config.JpaConfig;
import com.mapz.domain.domains.BaseEntity;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
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
        classes = {JpaConfig.class, BaseEntity.class}))
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public @interface RepositoryTest {
}
