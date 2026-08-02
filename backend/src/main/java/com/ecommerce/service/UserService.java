package com.ecommerce.service;

import com.ecommerce.dto.request.AddressRequest;
import com.ecommerce.dto.request.ChangePasswordRequest;
import com.ecommerce.dto.response.AddressDTO;
import com.ecommerce.dto.response.UserDTO;
import com.ecommerce.entity.Address;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ForbiddenException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.AddressRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UserService — manages user profiles and addresses.
 *
 * Design Pattern: Facade (hides entity/DTO mapping complexity)
 *
 * Data Structures:
 * - List<Address>: ArrayList used for ordered address display
 * - Set<Role>: HashSet for O(1) role lookups
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── Profile ──────────────────────────────────────────────────

    public UserDTO getProfile(String email) {
        User user = findByEmail(email);
        return mapToUserDTO(user);
    }

    @Transactional
    public UserDTO updateProfile(String email, String firstName, String lastName, String phone) {
        User user = findByEmail(email);
        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);
        if (phone != null) {
            if (userRepository.existsByPhone(phone) && !phone.equals(user.getPhone())) {
                throw new BadRequestException("Phone number already in use");
            }
            user.setPhone(phone);
        }
        return mapToUserDTO(userRepository.save(user));
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = findByEmail(email);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New passwords do not match");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", email);
    }

    // ─── Addresses ───────────────────────────────────────────────

    public List<AddressDTO> getAddresses(String email) {
        User user = findByEmail(email);
        // Returns ArrayList — ordered list of addresses
        return addressRepository.findByUserId(user.getId()).stream()
                .map(this::mapToAddressDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressDTO addAddress(String email, AddressRequest request) {
        User user = findByEmail(email);
        Address address = buildAddress(request, user);

        // If this is the first address, make it default
        if (addressRepository.countByUserId(user.getId()) == 0) {
            address.setIsDefault(true);
        } else if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.clearDefaultByUserId(user.getId());
            address.setIsDefault(true);
        }

        return mapToAddressDTO(addressRepository.save(address));
    }

    @Transactional
    public AddressDTO updateAddress(String email, Long addressId, AddressRequest request) {
        User user = findByEmail(email);
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.clearDefaultByUserId(user.getId());
            address.setIsDefault(true);
        }

        address.setAddressType(request.getAddressType() != null ? request.getAddressType() : address.getAddressType());
        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setZipCode(request.getZipCode());
        address.setCountry(request.getCountry() != null ? request.getCountry() : address.getCountry());

        return mapToAddressDTO(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(String email, Long addressId) {
        User user = findByEmail(email);
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        addressRepository.delete(address);
    }

    @Transactional
    public AddressDTO setDefaultAddress(String email, Long addressId) {
        User user = findByEmail(email);
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        addressRepository.clearDefaultByUserId(user.getId());
        address.setIsDefault(true);
        return mapToAddressDTO(addressRepository.save(address));
    }

    // ─── Private Helpers ──────────────────────────────────────────

    private User findByEmail(String email) {
        return userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private UserDTO mapToUserDTO(User user) {
        var roleNames = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());

        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profileImage(user.getProfileImage())
                .isActive(user.getIsActive())
                .isVerified(user.getIsVerified())
                .roles(roleNames)
                .createdAt(user.getCreatedAt())
                .build();
    }

    private AddressDTO mapToAddressDTO(Address address) {
        return AddressDTO.builder()
                .id(address.getId())
                .addressType(address.getAddressType())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .country(address.getCountry())
                .isDefault(address.getIsDefault())
                .build();
    }

    private Address buildAddress(AddressRequest request, User user) {
        return Address.builder()
                .user(user)
                .addressType(request.getAddressType() != null ? request.getAddressType() : Address.AddressType.HOME)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .country(request.getCountry() != null ? request.getCountry() : "India")
                .isDefault(false)
                .build();
    }
}
