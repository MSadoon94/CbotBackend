package com.sadoon.cbotback.security;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;

@RestController
public class SecurityController {

    @RolesAllowed("USER")
    @RequestMapping("/*")
    public String getUser(){
        return "Welcome User";
    }

    @RolesAllowed({"USER", "ADMIN"})
    @RequestMapping("/admin")
    public String getAdmin(){
        return "Welcome Admin";
    }
}
