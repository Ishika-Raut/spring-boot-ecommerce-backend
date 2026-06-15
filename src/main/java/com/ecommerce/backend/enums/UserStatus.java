package com.ecommerce.backend.enums;

public enum UserStatus {

    // Common statuses for both User and Seller
	// Fully functional account 
    ACTIVE,

    // Account exists but login is disabled (self-deactivated or dormant) 
    INACTIVE,
    
    //Soft delete
    DEACTIVATE;
    // Optional: Helper method
    public boolean isActive() {
        return this == ACTIVE;
    }

    
}
