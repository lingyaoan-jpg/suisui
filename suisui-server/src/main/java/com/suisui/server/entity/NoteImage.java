package com.suisui.server.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "note_images")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class NoteImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "note_id", nullable = false)
    private Long noteId;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
}
