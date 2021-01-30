package com.example.oidcexample;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/secure")
public class SecureController {

    @GetMapping("endpoint")
    public String index(@AuthenticationPrincipal final OAuth2User user) {

        final Object favNum = user.getAttribute("fav-number");
        return "Hello " + user.getName() + "! Your fav is " + favNum + ".";
    }
}
