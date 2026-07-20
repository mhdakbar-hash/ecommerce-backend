package com.akbar.ecommerce_backend.user.security;


import com.akbar.ecommerce_backend.user.entity.UserEntity;
import com.akbar.ecommerce_backend.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity user = userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(()-> new UsernameNotFoundException("User not found with email: " + username));

        return new CustomUserDetails(user);

    }
}
