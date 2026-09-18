package com.suisui.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequest {

    private long noteId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论不能超过500字")
    private String content;
}
