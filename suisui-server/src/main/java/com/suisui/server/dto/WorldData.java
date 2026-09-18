package com.suisui.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorldData {

    private long userId;
    private String nickname;
    private String username;
    private String avatarUrl;
    private String bgUrl;
    private String bio;
    private boolean worldPublic;
    private String lastActiveTime;
    private int subscriberCount;
    private int momentCount;
    private boolean subscribed;
    private String latestMomentPreview;
}
