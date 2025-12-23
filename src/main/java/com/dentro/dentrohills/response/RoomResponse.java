package com.dentro.dentrohills.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class RoomResponse {

    private Long id;
    private String roomType;
    private BigDecimal roomPrice;
    private boolean isBooked;

    // MULTIPLE photos (Base64 strings or S3 URLs later)
    private List<String> photos;

    private List<BookingResponse> bookings;

    // Used when creating a room
    public RoomResponse(Long id, String roomType, BigDecimal roomPrice) {
        this.id = id;
        this.roomType = roomType;
        this.roomPrice = roomPrice;
    }

    // Used when fetching room details
    public RoomResponse(
            Long id,
            String roomType,
            BigDecimal roomPrice,
            boolean isBooked,
            List<String> photos,
            List<BookingResponse> bookings
    ) {
        this.id = id;
        this.roomType = roomType;
        this.roomPrice = roomPrice;
        this.isBooked = isBooked;
        this.photos = photos;
        this.bookings = bookings;
    }
}
