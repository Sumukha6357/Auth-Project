package com.example.idp.web;

import com.example.idp.service.UserAdminService;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public")
public class PublicController {
    private final UserAdminService userAdminService;

    public PublicController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @PostMapping("/verify-email")
    public Map<String, Object> verifyEmail(@RequestParam String token) {
        return Map.of("verified", userAdminService.verifyEmail(token));
    }
}
