package com.moveinsync.sentiment.security;

import com.moveinsync.sentiment.model.User;
import com.moveinsync.sentiment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom UserDetailsService implementation
 * Loads user from database for Spring Security authentication
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> 
                    new UsernameNotFoundException("User not found with email: " + email)
                );

        return UserPrincipal.create(user);
    }

    /**
     * Load user by ID (used by JWT filter)
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> 
                    new UsernameNotFoundException("User not found with id: " + id)
                );

        return UserPrincipal.create(user);
    }

    /**
     * UserPrincipal - Spring Security UserDetails implementation
     */
    public static class UserPrincipal implements UserDetails {
        private final Long id;
        private final String email;
        private final String name;
        private final String password;
        private final boolean isActive;
        private final Collection<? extends GrantedAuthority> authorities;

        public UserPrincipal(
                Long id,
                String email,
                String name,
                String password,
                boolean isActive,
                Collection<? extends GrantedAuthority> authorities
        ) {
            this.id = id;
            this.email = email;
            this.name = name;
            this.password = password;
            this.isActive = isActive;
            this.authorities = authorities;
        }

        public static UserPrincipal create(User user) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            
            // Add role as authority
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
            
            // Add permissions as authorities
            authorities.addAll(user.getPermissions().stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()));

            return new UserPrincipal(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getPassword(),
                    user.getIsActive(),
                    authorities
            );
        }

        // Getters
        public Long getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }

        @Override
        public String getUsername() {
            return email; // Use email as username
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return isActive;
        }
    }
}
