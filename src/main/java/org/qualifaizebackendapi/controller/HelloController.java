package org.qualifaizebackendapi.controller;

import org.qualifaizebackendapi.utils.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class HelloController {

    @GetMapping("/")
    public String hello(Principal principal) {
        return "Hello World";
    }

    @GetMapping("/user")
    public String helloUser() {
        return "Hello User " + SecurityUtils.getCurrentUserId();
    }

    @GetMapping("/admin")
    public String helloAdmin(Principal principal) {
        return "Hello Admin";
    }
}
