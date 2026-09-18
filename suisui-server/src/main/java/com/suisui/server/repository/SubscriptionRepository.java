package com.suisui.server.repository;

import com.suisui.server.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findBySubscriberId(Long subscriberId);

    Optional<Subscription> findBySubscriberIdAndTargetUserId(Long subscriberId, Long targetUserId);

    boolean existsBySubscriberIdAndTargetUserId(Long subscriberId, Long targetUserId);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.targetUserId = :targetUserId")
    long countByTargetUserId(@Param("targetUserId") Long targetUserId);
}
