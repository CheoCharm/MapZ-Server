package com.cheocharm.MapZ.user.domain.repository;

import com.cheocharm.MapZ.user.domain.User;
import com.cheocharm.MapZ.user.domain.UserProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    Optional<User> findByEmailAndUserProvider(String email, UserProvider userProvider);

    Optional<User> findByUsername(String username);
}
