package com.cheocharm.MapZ.user.domain.repository;

import com.cheocharm.MapZ.user.domain.UserEntity;
import com.cheocharm.MapZ.user.domain.UserProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long>, UserRepositoryCustom {
    Optional<UserEntity> findByEmailAndUserProvider(String email, UserProvider userProvider);

    Optional<UserEntity> findByUsername(String username);
}
