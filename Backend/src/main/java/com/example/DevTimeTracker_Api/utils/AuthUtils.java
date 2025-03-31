package com.example.DevTimeTracker_Api.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Slf4j
public class AuthUtils {

    public static String getAuthenticatedUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            log.warn("User is not authenticated");
            throw new SecurityException("You are not authorized");
        }
        String userEmail = ((UserDetails) principal).getUsername();
        log.debug("Authenticated user email: {}", userEmail);
        return userEmail;
    }
}