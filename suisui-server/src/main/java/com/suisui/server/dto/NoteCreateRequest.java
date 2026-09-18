package com.suisui.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteCreateRequest {

    @NotBlank(message = "内容不能为空")
    @Size(max = 5000, message = "内容不能超过5000字")
    private String content;

    private List<String> images;

    private List<String> tags;
}
