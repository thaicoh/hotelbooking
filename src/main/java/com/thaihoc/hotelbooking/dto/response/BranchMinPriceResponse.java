package com.thaihoc.hotelbooking.dto.response;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BranchMinPriceResponse {
    private String branchId;
    private String branchName;
    private String address;
    private String photoUrl;

    private Long roomTypeId;
    private String roomTypeName;

    private String bookingTypeCode;
    private BigDecimal minPrice;
    private String currency;
}