package com.thaihoc.hotelbooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomTypeResponse {

    private Long id;

    private BranchResponse branch;

    private String typeName;

    private Integer capacity;

    private String description;

}
