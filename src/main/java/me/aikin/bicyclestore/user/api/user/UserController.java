package me.aikin.bicyclestore.user.api.user;

import me.aikin.bicyclestore.user.api.user.playload.UserSummary;
import me.aikin.bicyclestore.user.security.CurrentUser;
import me.aikin.bicyclestore.user.security.UserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER')")
    public UserSummary getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        UserSummary userSummary = UserSummary.builder()
            .id(currentUser.getId())
            .name(currentUser.getName())
            .username(currentUser.getUsername())
            .build();
        return userSummary;
    }
}
