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
    private List<String> photos;
    private List<BookingResponse> bookings;
    private String nearestHospital;

    // ✅ OLD constructor (KEEP for backward compatibility)
    public RoomResponse(Long id, String roomType, BigDecimal roomPrice) {
        this.id = id;
        this.roomType = roomType;
        this.roomPrice = roomPrice;
    }

    // ✅ NEW constructor (used when adding room)
    public RoomResponse(Long id, String roomType, BigDecimal roomPrice, String nearestHospital) {
        this.id = id;
        this.roomType = roomType;
        this.roomPrice = roomPrice;
        this.nearestHospital = nearestHospital;
    }

    // ✅ FULL constructor (used when fetching room)
    public RoomResponse(
            Long id,
            String roomType,
            BigDecimal roomPrice,
            boolean isBooked,
            List<String> photos,
            List<BookingResponse> bookings,
            String nearestHospital
    ) {
        this.id = id;
        this.roomType = roomType;
        this.roomPrice = roomPrice;
        this.isBooked = isBooked;
        this.photos = photos;
        this.bookings = bookings;
        this.nearestHospital = nearestHospital;
    }
}
