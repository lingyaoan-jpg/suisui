package com.suisui.server.repository;

import com.suisui.server.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByNoteIdAndUserId(Long noteId, Long userId);

    boolean existsByNoteIdAndUserId(Long noteId, Long userId);
}
