package com.thaihoc.hotelbooking.dto.request;

import com.thaihoc.hotelbooking.entity.Permission;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleCreationRequest {
    private String name;
    private String description;
    private Set<String> permissions;
}
