package com.factory.common.security;

import com.factory.user.entity.User;
import com.factory.user.entity.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {

    private final String id;
    private final String email;
    private final String passwordHash;
    private final String fullName;
    private final String role;
    private final String officeId;
    private final String officeName;
    private final String state;
    private final List<String> allowedOffices;
    private final boolean active;
    private final boolean nonLocked;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.fullName = user.getFullName();
        this.role = user.getRole().name();
        this.officeId = user.getPrimaryOffice() != null ? user.getPrimaryOffice().getId() : "";
        this.officeName = user.getPrimaryOffice() != null ? user.getPrimaryOffice().getName() : "";
        this.state = user.getPrimaryOffice() != null ? user.getPrimaryOffice().getState() : "";
        this.allowedOffices = user.getAllowedOffices() != null
                ? user.getAllowedOffices().stream().map(o -> o.getId()).collect(Collectors.toList())
                : Collections.emptyList();
        this.active = user.getStatus() == UserStatus.ACTIVE;
        this.nonLocked = user.isAccountNonLocked();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return nonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public String getOfficeId() {
        return officeId;
    }

    public String getOfficeName() {
        return officeName;
    }

    public String getState() {
        return state;
    }

    public List<String> getAllowedOffices() {
        return allowedOffices;
    }
}
