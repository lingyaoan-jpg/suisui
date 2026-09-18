package com.suisui.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentData {

    private long id;
    private long noteId;
    private long userId;
    private String username;
    private String userAvatar;
    private String content;
    private String createdAt;
}
