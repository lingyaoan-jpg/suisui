package com.suisui.server.repository;

import com.suisui.server.entity.NoteImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteImageRepository extends JpaRepository<NoteImage, Long> {

    List<NoteImage> findByNoteIdOrderBySortOrderAsc(Long noteId);
}
