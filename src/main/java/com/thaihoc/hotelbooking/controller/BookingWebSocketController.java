package com.thaihoc.hotelbooking.controller;

import com.thaihoc.hotelbooking.dto.response.BookingListItemResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class BookingWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public BookingWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Khi có booking mới, gọi hàm này để gửi tới client
    public void notifyNewBooking(BookingListItemResponse bookingResponse) {
        messagingTemplate.convertAndSend("/topic/bookings", bookingResponse);
    }
}

