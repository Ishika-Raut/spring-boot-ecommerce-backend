package com.ecommerce.backend.identity.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ecommerce.backend.common.util.TokenAndOtpGenerator;
import com.ecommerce.backend.enums.CodeType;
import com.ecommerce.backend.enums.VerificationChannel;
import com.ecommerce.backend.enums.VerificationStatus;
import com.ecommerce.backend.enums.VerificationType;
import com.ecommerce.backend.exception.InvalidVerificationTokenException;
import com.ecommerce.backend.exception.VerificationTokenExpiredException;
import com.ecommerce.backend.identity.dto.AuthenticationResponseDTO;
import com.ecommerce.backend.identity.entity.RefreshToken;
import com.ecommerce.backend.identity.entity.User;
import com.ecommerce.backend.identity.entity.UsersVerification;
import com.ecommerce.backend.identity.repository.RefreshTokenRepository;
import com.ecommerce.backend.identity.repository.UserRepository;
import com.ecommerce.backend.identity.repository.UsersVerificationRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsersVerificationRepository usersVerificationRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    public RefreshToken saveRefreshToken(User user, String refreshToken, long refreshExpirationMs) 
    {
    	System.out.println("saveRefresh toekn service");
        RefreshToken tokenEntity = RefreshToken.builder()
                .refreshToken(refreshToken)
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(tokenEntity);
    }
    
    
    //GENERIC MTHOD TO CREATE TOKEN FOR EMAIL VERIFICATION AND PASSWORD RESET
    public UsersVerification generateToken(User user,  String target, VerificationType verificationType, long expiryAmount, ChronoUnit chronoUnit)
	{
	    // INVALIDATE OLD TOKENS
	    usersVerificationRepository.invalidateOldTokens(user.getUserId(), verificationType);
	
	    // GENERATE NEW TOKEN
	    String token = TokenAndOtpGenerator.generateRandomToken();
	
	    UsersVerification verification = UsersVerification.builder()
	                    .user(user)
	                    .verificationType(verificationType)
	                    .channel(VerificationChannel.EMAIL)
	                    .target(target)
	                    .code(token)
	                    .codeType(CodeType.TOKEN)
	                    .expiryTime(LocalDateTime.now().plus(expiryAmount, chronoUnit)) //set expire time for  token)
	                    .verificationStatus(VerificationStatus.ACTIVE) //generate new otp or token
	                    .attempts(0)
	                    .build();
	    
	    return usersVerificationRepository.save(verification);
	}
    
    
    @Transactional
    public void logout(String refreshToken)
    {
        RefreshToken token = refreshTokenRepository.findByRefreshTokenAndRevokedFalse(refreshToken)
                        .orElseThrow(() -> new InvalidVerificationTokenException("Invalid refresh token" ) );

        token.setRevoked(true);

        refreshTokenRepository.save(token);
    }
    
    // REFRESH ACCESS TOKEN
    public AuthenticationResponseDTO refreshAccessToken(String refreshToken)
    {
        RefreshToken token = refreshTokenRepository.findByRefreshToken( refreshToken )
                        .orElseThrow(() -> new InvalidVerificationTokenException("Invalid refresh token" ));

        // REVOKED CHECK
        if(token.isRevoked())
        {
            throw new InvalidVerificationTokenException("Refresh token revoked");
        }

        // EXPIRY CHECK
        if(token.isExpired())
        {
            throw new VerificationTokenExpiredException("Refresh token expired");
        }

        Long userId = token.getUser().getUserId();

        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<String> roles = user.getRoles()
                .stream()
                .map(r -> r.getName().name())
                .toList();

        String newAccessToken =  jwtService.generateAccessToken( user.getUserId(), user.getEmail(), roles);

        return AuthenticationResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .name(user.getName())
                .email(user.getEmail())
                .role(
                        user.getRoles()
                                .stream()
                                .findFirst()
                                .get()
                                .getName()
                                .name()
                )
                .build();
    }
    
    @Transactional
    public void revokedAllRefreshTokens(User user)
    {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUserAndRevokedFalse(user);

        for(RefreshToken token : tokens)
        {
            token.setRevoked(true);
        }

        refreshTokenRepository.saveAll(tokens);
    }
    

}