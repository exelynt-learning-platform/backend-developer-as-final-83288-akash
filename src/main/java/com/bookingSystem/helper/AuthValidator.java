package com.bookingSystem.helper;

import com.bookingSystem.entity.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Slf4j
public class AuthValidator
{
    public static UserRole getCleanRole(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        boolean hasUserRole = authorities.stream()
                .anyMatch(auth -> "ROLE_USER".equals(auth.getAuthority()));

        if (hasUserRole)
            return UserRole.USER;
        return UserRole.ADMIN;
    }

    public static void validAdminAuth(Authentication authentication){
        if (authentication == null)
            throw new AuthenticationCredentialsNotFoundException("User is not authenticated!");

        UserRole userRole = getCleanRole(authentication);
        if (userRole.equals(UserRole.USER))
            throw new AuthorizationDeniedException("Access Denied!");
    }

}
