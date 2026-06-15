package com.ecommerce.backend.address.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ecommerce.backend.address.dto.AddressRequestDTO;
import com.ecommerce.backend.address.dto.AddressResponseDTO;
import com.ecommerce.backend.address.entity.Address;
import com.ecommerce.backend.address.repository.AddressRepository;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.identity.entity.User;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressService 
{
    private final AddressRepository addressRepository;

    @Transactional
    public AddressResponseDTO addAddress(User user, AddressRequestDTO addressRequestDTO)
    {
        Address address = Address.builder()
        		 .userId(user.getUserId())  
                .fullName(addressRequestDTO.getFullName())
                .phone(addressRequestDTO.getPhone())
                .addressLine1(addressRequestDTO.getAddressLine1())
                .addressLine2(addressRequestDTO.getAddressLine2())
                .city(addressRequestDTO.getCity())
                .state(addressRequestDTO.getState())
                .pincode(addressRequestDTO.getPincode())
                .addressType(addressRequestDTO.getAddressType())
                .isDefault(false)
                .build();
        
        boolean firstAddress = addressRepository.findByUserUserId(user.getUserId()).isEmpty();

        // IF USER HAS NO ADDRESS MAKE FIRST ADDRESS AS DEFAULT
        if(firstAddress)
        {
            address.setIsDefault(true);
        }

        //USER ALREADY HAD ADDRESS AND USER WANTS TO MAKE NEW ADDRESS AS DEFAULT 
        //THEN MAKE ALREADY STORED DEFAULT ADDRESS AS FALSE AND SET DEFAULT AS TRUE FOR NEW ADDRESS
        if(Boolean.TRUE.equals(addressRequestDTO.getIsDefault()))
        {
            addressRepository.clearDefaultForUser(user.getUserId());
            address.setIsDefault(true);
        }
        //USER ALREADY HAD ADDRESS AND USER DOES NOT WANTS TO MAKE NEW ADDRESS AS DEFAULT THEN SET DEFAULT AS TRUE FOR NEW ADDRESS
        
        Address saved = addressRepository.save(address);
        
        // no address line from summary view
        return AddressResponseDTO.builder()
                .addressId(saved.getAddressId())
                .fullName(saved.getFullName())
                .phone(saved.getPhone())
                .addressLine1(saved.getAddressLine1())
                .addressLine2(saved.getAddressLine2())
                .city(saved.getCity())
                .state(saved.getState())
                .pincode(saved.getPincode())
               // .addressType(saved.getAddressType())
                .isDefault(saved.getIsDefault())
                .build();
    }
    
    public List<AddressResponseDTO> getUserAddresses(User user)
    {
        return addressRepository.findByUserUserId(user.getUserId())
                .stream()
                .map(address -> AddressResponseDTO.builder() 	// no address line from summary view
                        .addressId(address.getAddressId())
                        .fullName(address.getFullName())
                        .phone(address.getPhone())
                        .addressLine1(address.getAddressLine1())
                        .addressLine2(address.getAddressLine2())
                        .city(address.getCity())
                        .state(address.getState())
                        .pincode(address.getPincode())
                        .isDefault(address.getIsDefault())
                        .build()
                )
                .toList();
    }
    
    public AddressResponseDTO getAddressById(User user, Long addressId)
    {
        Address address = addressRepository.findByAddressIdAndUserUserId(addressId, user.getUserId())
        									.orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        return AddressResponseDTO.builder()
                .addressId(address.getAddressId())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .isDefault(address.getIsDefault())
                .build();
    }
    
    @Transactional
    public AddressResponseDTO updateAddress(User user, Long addressId, AddressRequestDTO addressRequestDTO)
    {
        Address address = addressRepository
                .findByAddressIdAndUserUserId(addressId, user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        System.out.println("address line 1 = " + addressRequestDTO.getAddressLine1());
        System.out.println("address line 2 = " + addressRequestDTO.getAddressLine2());
        if (addressRequestDTO.getFullName() != null) {
            address.setFullName(addressRequestDTO.getFullName());
        }

        if (addressRequestDTO.getPhone() != null) {
            address.setPhone(addressRequestDTO.getPhone());
        }

        if (addressRequestDTO.getAddressLine1() != null) {
            address.setAddressLine1(addressRequestDTO.getAddressLine1());
        }

        if (addressRequestDTO.getAddressLine2() != null) {
            address.setAddressLine2(addressRequestDTO.getAddressLine2());
        }

        if (addressRequestDTO.getCity() != null) {
            address.setCity(addressRequestDTO.getCity());
        }

        if (addressRequestDTO.getState() != null) {
            address.setState(addressRequestDTO.getState());
        }

        if (addressRequestDTO.getPincode() != null) {
            address.setPincode(addressRequestDTO.getPincode());
        }
        
        if (addressRequestDTO.getAddressType() != null) {
            address.setAddressType(addressRequestDTO.getAddressType());
        }
        
        //IF USER WANTS TO MAKE ADDRESS AS DEFAULT 
        if (Boolean.TRUE.equals(addressRequestDTO.getIsDefault())) 
        {
            // CHEKC IF THIS ADDRESS IS ALREADY DEFAULT OR NOT
            if (!Boolean.TRUE.equals(address.getIsDefault())) 
            {
                addressRepository.clearDefaultForUser(user.getUserId());
                address.setIsDefault(true);
            }
        }

        Address saved = addressRepository.save(address);

       return AddressResponseDTO.builder()
                .addressId(saved.getAddressId())
                .fullName(saved.getFullName())
                .phone(saved.getPhone())
                .city(saved.getCity())
                .state(saved.getState())
                .pincode(saved.getPincode())
                .isDefault(saved.getIsDefault())
                .build();
    }
    
    @Transactional
    public void setDefaultAddress(User user, Long addressId)
    {
        Address address = addressRepository.findByAddressIdAndUserUserId(addressId, user.getUserId() )
        					.orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        
        addressRepository.clearDefaultForUser(user.getUserId());

        address.setIsDefault(true);

        addressRepository.save(address);
    }
    
    @Transactional
    public void deleteAddress(User user, Long addressId)
    {
        Address address = addressRepository.findByAddressIdAndUserUserId(addressId, user.getUserId())
        					.orElseThrow(() -> new ResourceNotFoundException("Address not found") );

        addressRepository.delete(address);
    }
}