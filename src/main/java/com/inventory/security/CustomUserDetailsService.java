package com.inventory.security;

import com.inventory.entity.UserAccount;
import com.inventory.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        log.debug("Loading user details for email: {}, user ID: {}", email, user.getId());
        return UserPrincipal.create(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new UsernameNotFoundException("User not found with ID: " + id);
                });

        log.debug("Loading user details for ID: {}, email: {}", id, user.getEmail());
        return UserPrincipal.create(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserByEmailAndTenant(String email, Long tenantId) {
        UserAccount user = userAccountRepository.findByEmailIgnoreCaseAndTenantId(email, tenantId)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {} and tenant ID: {}", email, tenantId);
                    return new UsernameNotFoundException("User not found with email: " + email + 
                                                       " and tenant ID: " + tenantId);
                });

        log.debug("Loading user details for email: {}, tenant ID: {}, user ID: {}", 
                 email, tenantId, user.getId());
        return UserPrincipal.create(user);
    }
}