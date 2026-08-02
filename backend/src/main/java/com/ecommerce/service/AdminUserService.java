package com.ecommerce.service;

import com.ecommerce.dto.request.UserRoleUpdateRequest;
import com.ecommerce.dto.request.UserStatusUpdateRequest;
import com.ecommerce.dto.response.PagedResponse;
import com.ecommerce.dto.response.UserAdminDTO;
import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.RoleRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AdminUserService — business logic for Admin user management, role assignments, and status toggles.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public PagedResponse<UserAdminDTO> getAllUsers(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> userPage = userRepository.findAll(pageable);
        return PagedResponse.from(userPage.map(this::mapToAdminDTO));
    }

    @Transactional(readOnly = true)
    public UserAdminDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return mapToAdminDTO(user);
    }

    @Transactional
    public UserAdminDTO updateUserStatus(Long userId, UserStatusUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setIsActive(request.getIsActive());
        User saved = userRepository.save(user);
        log.info("User ID {} active status updated to {}", userId, request.getIsActive());
        return mapToAdminDTO(saved);
    }

    @Transactional
    public UserAdminDTO updateUserRoles(Long userId, UserRoleUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Set<Role> roles = new HashSet<>();
        for (String roleName : request.getRoles()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new BadRequestException("Role '" + roleName + "' does not exist"));
            roles.add(role);
        }

        user.setRoles(roles);
        User saved = userRepository.save(user);
        log.info("User ID {} roles updated to {}", userId, request.getRoles());
        return mapToAdminDTO(saved);
    }

    public UserAdminDTO mapToAdminDTO(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return UserAdminDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .isActive(user.getIsActive())
                .isVerified(user.getIsVerified())
                .roles(roleNames)
                .addressCount(user.getAddresses() != null ? user.getAddresses().size() : 0)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
