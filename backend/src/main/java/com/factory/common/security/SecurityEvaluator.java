package com.factory.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("securityEvaluator")
public class SecurityEvaluator {

    public boolean canAccessOffice(Authentication authentication, String officeId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        String role = user.getRole();

        // Owner and Admin have unrestricted office access
        if ("OWNER".equals(role) || "ADMIN".equals(role)) {
            return true;
        }

        // Check if officeId matches primary office or is in allowed offices
        if (officeId == null) {
            return false;
        }

        if (officeId.equals(user.getOfficeId())) {
            return true;
        }

        List<String> allowed = user.getAllowedOffices();
        return allowed != null && allowed.contains(officeId);
    }

    public boolean canAccessState(Authentication authentication, String state) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        String role = user.getRole();

        if ("OWNER".equals(role) || "ADMIN".equals(role)) {
            return true;
        }

        if (state == null) {
            return false;
        }

        return state.equalsIgnoreCase(user.getState());
    }

    public boolean isOwner(Authentication authentication) {
        return hasRole(authentication, "ROLE_OWNER");
    }

    public boolean isAdmin(Authentication authentication) {
        return hasRole(authentication, "ROLE_ADMIN");
    }

    public boolean isGodownManager(Authentication authentication) {
        return hasRole(authentication, "ROLE_GODOWN_MANAGER");
    }

    public boolean isMainOffice(Authentication authentication) {
        return hasRole(authentication, "ROLE_MAIN_OFFICE");
    }

    private boolean hasRole(Authentication authentication, String roleWithPrefix) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(granted -> granted.getAuthority().equals(roleWithPrefix));
    }
}
