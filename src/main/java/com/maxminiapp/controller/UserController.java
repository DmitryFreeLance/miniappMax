package com.maxminiapp.controller;

import com.maxminiapp.dto.CurrentUserResponse;
import com.maxminiapp.model.AppUser;
import com.maxminiapp.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public CurrentUserResponse me(
            @RequestHeader(name = "X-User-Id", required = false) Long maxUserId
    ) {
        if (maxUserId == null) {
            return new CurrentUserResponse(false, null, false, null, null);
        }

        AppUser user = userService.getOrCreateByMaxUserId(maxUserId);
        return new CurrentUserResponse(
                true,
                user.getMaxUserId(),
                user.isAdmin(),
                user.getFullName(),
                user.getPhone()
        );
    }
}
