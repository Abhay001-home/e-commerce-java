package com.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * UserAdminDTO — detailed user representation for Admin user management views.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserAdminDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Boolean isActive;
    private Boolean isVerified;
    private Set<String> roles;
    private Integer addressCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
