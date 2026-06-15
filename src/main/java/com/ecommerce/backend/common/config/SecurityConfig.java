package com.ecommerce.backend.common.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

//This configuration is NOT executed per request.
//This method runs: ONLY ON APPLICATION STARTUP to BUILD security system.
//Then Spring internally uses this built configuration for every request.

@Configuration
@EnableWebSecurity // it tells do Spring to dont go with default SS confign go with my custom SS confign
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	
	private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    //This method tells Spring: How should incoming HTTP requests be secured?
    //It builds: SecurityFilterChain which is basically: a chain of security filters that every request passes through.
    
    @Bean //Spring, create and manage and return this object
    //SecurityFilterChain object: This object contains: authentication rules, authorization rules, filters, session policy, csrf policy
    //Spring automatically gives: HttpSecurity object: Security configuration builder used to configure web security.
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
    	System.out.println("Security filter chain");
        return http
        		//CSRF: Cross Site Request Forgery - Attack where browser automatically sends cookies/session to malicious requests.
        		//It is mainly needed for session-based authentication where browser automatically sends session cookie.
        		//But we used JWT, which is manually sent in: Authorization: Bearer token 
        		//browser does NOT automatically attach JWT hence, CSRF risk greatly reduced
                .csrf(AbstractHttpConfigurer::disable) //disable(): is common in stateless JWT systems.
                
                //This configures: Authorization Rules
                //auth is: AuthorizeHttpRequestsConfigurer: used to define URL access rules.
                .authorizeHttpRequests(request -> request
                        .requestMatchers( //requestMatchers(): match these URL patterns
                                "/auth/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/admins/"
                        )
                        .permitAll() //No authentication required. Anyone can access. PAsses request directly to controller
                        
                        //If request is NOT matched by permitAll: then Spring checks :Is user authenticated?
                        //If no: 401 UNAUTORIZED: returned BEFORE controller executes.
                        .anyRequest().authenticated())
                
                //It configures: How Spring manages sessions
                .sessionManagement(session -> session
                		//It means: Do NOT create HttpSession Do NOT store authentication server-Side. Each request must independently provide JWT.
                		//without this, Spring may: create session and becomes STATEFUL authentication
                			
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                //This tells Spring: use this AuthenticationProvider: DaoAuthenticationProvider for authentication logic. 
                //Spring calls it during: login authentication
                .authenticationProvider(authenticationProvider)
                
                //Before controller: request passes through many filters.
                //addFilterBefore(): means Run my JWT filter BEFORE UsernamePasswordAuthenticationFilter
                //We inserted OUR custom filter: jwtAuthenticationFilter - into Spring Security filter chain.
                //This filter will run for each request
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                
                //Builds final: returns SecurityFilterChain object: Spring registers it internally.
                .build();
    }
    
    //whenever we request for update like - put, post, delete where we changing something 
    //the CSRF token we have to send otherwise method wont work and got 401 unauthorized
    //we send key value pair 
    //X-CSRF-TOKEN => key we send in postman
    @Bean
    CorsConfigurationSource corsConfigurationSource(){
    	System.out.println("corsConfiguration");
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8005"));
        configuration.setAllowedMethods(List.of("GET", "POST"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    
    /*
     @getMappping("/get-csrfToken")
     public CsrfToken getCsrfToken(HttpServeltRequest request){
     	return (CsrfToken)request.getAtrribute("_csrf"); //getAttribute() returns objct type we cast it to type CsrfToken 
     	
     }
     
     
     */
    
}