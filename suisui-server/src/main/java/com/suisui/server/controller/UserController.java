package com.suisui.server.controller;

import com.suisui.server.dto.ApiResponse;
import com.suisui.server.dto.UpdateProfileRequest;
import com.suisui.server.dto.WorldData;
import com.suisui.server.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/user/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@RequestBody UpdateProfileRequest request) {
        userService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/world/{userId}")
    public ResponseEntity<ApiResponse<WorldData>> getWorld(@PathVariable long userId) {
        WorldData world = userService.getWorld(userId);
        return ResponseEntity.ok(ApiResponse.success(world));
    }

    @PutMapping("/world/public")
    public ResponseEntity<ApiResponse<Void>> toggleWorldPublic() {
        userService.toggleWorldPublic();
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/world/discover")
    public ResponseEntity<ApiResponse<List<WorldData>>> getDiscover(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<WorldData> list = userService.getDiscover(page, size);
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
