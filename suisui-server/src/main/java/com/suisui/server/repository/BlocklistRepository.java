package com.suisui.server.repository;

import com.suisui.server.entity.Blocklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlocklistRepository extends JpaRepository<Blocklist, Long> {

    List<Blocklist> findByBlockerId(Long blockerId);

    Optional<Blocklist> findByBlockerIdAndBlockedUserId(Long blockerId, Long blockedUserId);

    boolean existsByBlockerIdAndBlockedUserId(Long blockerId, Long blockedUserId);

    @Query("SELECT b.blockedUserId FROM Blocklist b WHERE b.blockerId = :blockerId")
    List<Long> findBlockedUserIdsByBlockerId(@Param("blockerId") Long blockerId);
}
