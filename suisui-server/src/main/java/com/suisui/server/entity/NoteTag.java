package com.suisui.server.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "note_tags")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class NoteTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "note_id", nullable = false)
    private Long noteId;

    @Column(name = "tag_name", nullable = false, length = 30)
    private String tagName;
}
