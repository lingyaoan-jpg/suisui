package com.suisui.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteData {

    private long id;
    private long userId;
    private String content;
    private List<String> images;
    private List<String> tags;
    private int likeCount;
    private int commentCount;

    @JsonProperty("isLiked")
    private boolean isLiked;

    private String createdAt;
    private String updatedAt;

    // 关联用户信息（展示用）
    private String username;
    private String userNickname;
    private String userAvatar;
}
