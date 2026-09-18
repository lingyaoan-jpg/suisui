package com.suisui.server.repository;

import com.suisui.server.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    Page<Note> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT n FROM Note n LEFT JOIN n.tags t " +
           "WHERE n.userId = :userId AND n.isDeleted = false " +
           "AND (:tag IS NULL OR t.tagName = :tag) " +
           "GROUP BY n.id ORDER BY n.createdAt DESC")
    Page<Note> findByUserIdAndTag(@Param("userId") Long userId, @Param("tag") String tag, Pageable pageable);

    Optional<Note> findByIdAndIsDeletedFalse(Long id);

    @Query("SELECT COUNT(n) FROM Note n WHERE n.userId = :userId AND n.isDeleted = false")
    long countActiveByUserId(@Param("userId") Long userId);

    Optional<Note> findFirstByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId);
}
