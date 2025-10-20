package com.ssh.smartServiceHub.security;

import com.ssh.smartServiceHub.entity.User;
import com.ssh.smartServiceHub.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String emailOrUserame) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(emailOrUserame)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + emailOrUserame));

        String role = user.getRole() == null ? "USER" : user.getRole();

        SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_" + role);
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(grantedAuthority)
        );
    }
}
