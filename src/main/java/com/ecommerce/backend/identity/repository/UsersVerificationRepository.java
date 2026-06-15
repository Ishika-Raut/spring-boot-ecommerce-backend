package com.ecommerce.backend.identity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.backend.enums.VerificationStatus;
import com.ecommerce.backend.enums.VerificationType;
import com.ecommerce.backend.identity.entity.UsersVerification;

import jakarta.transaction.Transactional;

public interface UsersVerificationRepository
        extends JpaRepository<UsersVerification, Long>
{

    //VERIFY EMAIL - Give me the latest record for this EXACT token/code.
	//User clicks email link. We receive only token And jsut ValidateCheckDigit it.
	Optional<UsersVerification> findTopByCodeOrderByCreatedAtDesc( String code);
	
	 // SEND VERIFICATION OTP - Give me latest OTP/token for this USER + TYPE
    Optional<UsersVerification> findTopByUserUserIdAndVerificationTypeOrderByCreatedAtDesc( Long userId, VerificationType verificationType);
    
	//VERIFY PHONE OTP
	Optional<UsersVerification> findTopByTargetAndVerificationTypeAndVerificationStatusOrderByCreatedAtDesc(
	        String target,
	        VerificationType verificationType,
	        VerificationStatus verificationStatus
	);
	
	//RESEND OTP
	List<UsersVerification> findByTargetAndVerificationTypeAndVerificationStatus(
            String phone,
            VerificationType verificationType,
            VerificationStatus verificationStatus
    );

    //RESET PASSWORD TOKEN CHECKING
    Optional<UsersVerification> findTopByCodeAndVerificationTypeAndVerificationStatusOrderByCreatedAtDesc(
            String code,
            VerificationType verificationType,
            VerificationStatus verificationStatus
    );
    // INVALIDATE OLD TOKENS/OTPS
    @Modifying //tells spring that this is update query
    @Transactional //update is db operation hence it should be completed - either successful or fail
    @Query("""
        UPDATE UsersVerification uv SET  uv.verificationStatus = VerificationStatus.INVALIDATED 
        WHERE uv.user.userId = :userId AND uv.verificationType = :verificationType AND  uv.verificationStatus = VerificationStatus.ACTIVE
    """)
    void invalidateOldTokens( @Param("userId") Long userId, @Param("verificationType") VerificationType verificationType);
    //mark all old unsused tokens as true  
    //where: only for specific user AND only for specific verificationType AND sirf active unused tokens invalidated honge not used token
}