package com.ecommerce.backend.identity.service;


import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService 
{
	// Reads secret key from application.properties
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.access-expiration-time}")
    private long jwtExpiration;
    
    @Value("${security.jwt.refresh-expiration-time}")
    private long refreshExpiration;

    // EXTRACTION METHODS
    
    //Extract username(email) from JWT
    // JWT contains: subject = user email
    public String extractUsername(String token){
    	// Claims::getSubject => this is method reference.
    	// Equivalent: claims -> claims.getSubject()
    	// It means: from Claims object call getSubject()
        return extractClaim(token, Claims::getSubject); //returns
    }
    
    // Generic method to extract any claim from JWT
    // <T> - Generic return type
    // Can extract: subject, expiration, issuedAt
    // claimsResolver decides WHICH claim to extract.
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

//    private Claims extractAllClaims(String token){
//    	 try {
//	        return Jwts
//	                .parserBuilder()
//	                .setSigningKey(getSignInKey())
//	                .build()
//	                .parseClaimsJws(token)
//	                .getBody();
//    	 } 
//    	 catch (io.jsonwebtoken.ExpiredJwtException ex) {
//             throw new TokenExpiredException("Token expired");
//
//         } 
//    	 catch (Exception ex) {
//             throw new InvalidTokenException("Invalid token");
//         }
//    }
    private Claims extractAllClaims(String token) 
    {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    
    private Key getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // TOKEN GENERATION
    
    // ACCESS TOKEN
    public String generateAccessToken(Long userId, String email, List<String> roles) {

        Map<String, Object> extraClaims = Map.of(
                "userId", userId,
                "roles", roles
        );

        return buildToken(extraClaims, email, jwtExpiration);
    }
    
    public String generateRefreshToken(String email) {
        return buildToken(Map.of(), email, refreshExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    // VALIDATION
    
//    public long getExpirationTime(){
//        return jwtExpiration;
//    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

}