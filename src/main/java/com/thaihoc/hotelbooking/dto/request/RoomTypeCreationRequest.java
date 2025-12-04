package com.thaihoc.hotelbooking.dto.request;

import com.thaihoc.hotelbooking.entity.Branch;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomTypeCreationRequest {

    private String branchId;

    private String typeName;

    private Integer capacity;

    private String description;

}
