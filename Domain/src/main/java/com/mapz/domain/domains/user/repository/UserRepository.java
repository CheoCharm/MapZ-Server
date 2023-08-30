package com.mapz.domain.domains.user.repository;

import com.mapz.domain.domains.user.entity.User;
import com.mapz.domain.domains.user.enums.UserProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    Optional<User> findByEmailAndUserProvider(String email, UserProvider userProvider);

    Optional<User> findByUsername(String username);
}
