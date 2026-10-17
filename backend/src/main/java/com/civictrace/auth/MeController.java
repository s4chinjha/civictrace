package com.civictrace.auth;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

    @GetMapping("/api/me")
    public UserResponse me(Authentication authentication) {
        return UserResponse.from(((UserPrincipal) authentication.getPrincipal()).user());
    }
}
