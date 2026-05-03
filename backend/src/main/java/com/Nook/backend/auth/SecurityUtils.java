package com.Nook.backend.auth;

import com.Nook.backend.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

// A static utility so any controller can call SecurityUtils.getCurrentUserId()
// without repeating the same boilerplate everywhere
public class SecurityUtils {

    private SecurityUtils() {} // prevent instantiation — this is a utility class

    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Not authenticated");
        }

        // In JwtFilter we set the username to the userId — so getName() gives us the userId
        return authentication.getName();
    }
}