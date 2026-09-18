package com.suisui.server.repository;

import com.suisui.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.id <> :currentUserId AND u.isPublic = true AND u.id NOT IN :blockedIds")
    List<User> findDiscoverUsers(@Param("currentUserId") Long currentUserId, @Param("blockedIds") List<Long> blockedIds);
}
