package com.thaihoc.hotelbooking.mapper;

import com.thaihoc.hotelbooking.dto.response.ReviewResponse;
import com.thaihoc.hotelbooking.entity.Review;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    ReviewResponse toReviewResponse(Review review);
    List<ReviewResponse> toReviewResponseList(List<Review> reviews);
}
