package com.suisui.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthData {

    private long userId;
    private String token;
    private String username;
    private String nickname;
    private String avatarUrl;
}
