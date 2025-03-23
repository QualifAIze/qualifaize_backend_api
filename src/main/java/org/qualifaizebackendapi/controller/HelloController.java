package org.qualifaizebackendapi.controller;

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
    public String helloUser(Principal principal) {
        return "Hello User";
    }

    @GetMapping("/admin")
    public String helloAdmin(Principal principal) {
        return "Hello Admin";
    }
}
