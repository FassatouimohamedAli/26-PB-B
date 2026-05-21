package com.example.pfebtk.auth.service;

import com.example.pfebtk.auth.entity.User;
import com.example.pfebtk.auth.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService  implements UserDetailsService {

    @Autowired
    private UserRepo userRepository;

    @Override
    public UserDetails loadUserByUsername(String unix) throws UsernameNotFoundException {

        User u  = userRepository.findByUnix(unix).orElse(null);
        if (u != null) {
            return u;
        }

        throw new UsernameNotFoundException("User not found with email: " + unix);
    }
}

