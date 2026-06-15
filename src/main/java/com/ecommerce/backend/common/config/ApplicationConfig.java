package com.ecommerce.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.ecommerce.backend.identity.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig 
{
    private final UserRepository userRepository;

  //How user login ?
    //User login with the help of DaoAuthenticationProvider. This class tells to verify password
    //DaoAuthenticationProvider: internally uses: UserDetailsService and PasswordEncoder
    //This provider is mainly used during: /login  NOT during JWT validation.
    @Bean
    AuthenticationProvider authenticationProvider(){
    	System.out.println("authencationProvider");
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        System.out.println("auth = "+ authProvider);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    //UserDetailsService - 
    //Spring calls loadUserByUsername("username@gmail.com") --> search in db if user found then call 
    @Bean
    UserDetailsService userDetailsService() {
    	System.out.println("UserDetailsService");
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    
    //BCryptPasswordEncoder - stores passowrd in encrypted form 
    //Sring compares Entered Password vs encrypted password --> if successful then JWT is generated
  
    @Bean
    BCryptPasswordEncoder passwordEncoder() {
    	System.out.println("passwordEncoder");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager( AuthenticationConfiguration config) throws Exception {
    	System.out.println("authenticationManager");
        return config.getAuthenticationManager();
    }
}