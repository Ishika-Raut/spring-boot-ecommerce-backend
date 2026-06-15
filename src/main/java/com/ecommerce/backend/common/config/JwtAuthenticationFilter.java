package com.ecommerce.backend.common.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.ecommerce.backend.identity.service.JwtService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

//This JWT filter checks every incoming request "Does user sending request with valid JWT token or not?"
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter //It means: For every HTTP request this filter will execute exactly once
{
	//If exception occurs inside this filter then it will delegate exception to global exception handler
    private final HandlerExceptionResolver handlerExceptionResolver;
    //JWT operations: extract username, validate token, parse token
    private final JwtService jwtService;
    //It loads user form database 
    private final UserDetailsService userDetailsService;

    //This method executes for every request
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException 
    {
    	//Extracting Authorization header from the request. Authorization is String format
        final String authHeader = request.getHeader("Authorization");
        
        System.out.println("authHeader = " + authHeader);

        //Check: is header present? and starting with "Bearer "?
		//Standard JWT format: Authorization: Bearer TOKEN
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
        	//If no token then forward the request further. No authentication
            filterChain.doFilter(request, response); 
            return;
        }

        try 
        {
        	//removing "Bearer" and extracting only token part
            final String jwt = authHeader.substring(7); 
            System.out.println("jwt = " + jwt);
            //JWT decodes the token and extracts username/email from the token.
            final String userEmail = jwtService.extractUsername(jwt); 
            System.out.println("userEamil = " + userEmail);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("authentication = " + authentication);
            //If token do have email/username AND user is not authenticated => then authenticate the user
            if(userEmail != null && authentication == null){
            	//fetching user details from db using loadUserByUsername() method
            	//UserDetails: Spring Security's standard authenticated user object: contains: username, password, roles, authorities, account, status
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                System.out.println("userDateils = " + userDetails);
                //checK token expired ? , signature valid ? , username same?
                if(jwtService.isTokenValid(jwt, userDetails))
                {
//                	Yahan tum Spring ko bol rahi ho: "Maine JWT verify kar liya hai. User already authenticated hai."
//					Yahan password check nahi ho raha. Yahan object ke andar hota hai:
//                		principal   = UserDetails
//                		credentials = null
//                		authorities = ROLE_USER
//                		authenticated = true
                	//Extract token -->  Validate token --> Load user --> Create UsernamePasswordAuthenticationToken --> SecurityContextHolder.setAuthentication() --> Controller executes
            
                	//UsernamePasswordAuthenticationToken: spring's authentication object means "This user is authenticated".
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, //Authenticated user.
                            null, // JWT auth me password dubara verify nahi karna. Hence Password is null
                            userDetails.getAuthorities() //Roles
                    );
                    //Sets the extra request details: IP address, session id
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    System.out.println("authToken = " + authToken);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            
           
            //forward the request further. If we don't write it here then request will stop here
            filterChain.doFilter(request, response);
        } 
        catch (ExpiredJwtException ex) {

            System.out.println("JWT expired: " + ex.getMessage());

            SecurityContextHolder.clearContext();

            handlerExceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    ex
            );

            return;

        } catch (JwtException ex) {

            System.out.println("Invalid JWT: " + ex.getMessage());

            SecurityContextHolder.clearContext();

            handlerExceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    ex
            );

            return;

        } catch (Exception ex) {

            System.out.println("Exception: " + ex.getMessage());

            SecurityContextHolder.clearContext();

            handlerExceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    ex
            );

            return;
        

    }
}
}
