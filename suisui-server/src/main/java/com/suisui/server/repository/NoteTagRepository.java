package com.suisui.server.repository;

import com.suisui.server.entity.NoteTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteTagRepository extends JpaRepository<NoteTag, Long> {

    List<NoteTag> findByNoteId(Long noteId);

    @Query("SELECT nt.tagName, COUNT(nt) FROM NoteTag nt " +
           "JOIN Note n ON nt.noteId = n.id " +
           "WHERE n.userId = :userId AND n.isDeleted = false " +
           "GROUP BY nt.tagName ORDER BY COUNT(nt) DESC")
    List<Object[]> countTagsByUserId(@Param("userId") Long userId);
}
