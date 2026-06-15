package com.ecommerce.backend.seller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerUpdateProfileResponseDTO {
	private String name;
    private String phone;
    private String shopName;
    private String address;
    private String gstNumber;
}
