package com.dentro.dentrohills.controller;

import com.dentro.dentrohills.exception.InvalidBookingRequestException;
import com.dentro.dentrohills.exception.ResourceNotFoundException;
import com.dentro.dentrohills.model.BookedRoom;
import com.dentro.dentrohills.model.Room;
import com.dentro.dentrohills.response.BookingResponse;
import com.dentro.dentrohills.response.RoomResponse;
import com.dentro.dentrohills.service.IBookingService;
import com.dentro.dentrohills.service.IRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
@CrossOrigin(origins = "http://localhost:5173")
public class BookingController {
    private final IBookingService bookingService;
    private final IRoomService roomService;

    @Autowired
    public BookingController(IBookingService bookingService, IRoomService roomService) {
        this.bookingService = bookingService;
        this.roomService = roomService;  // Initialize roomService through constructor
    }

    @GetMapping("/all-bookings")
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<BookingResponse>> getAllBookings(){
        List<BookedRoom> bookings = bookingService.getAllBookings();
        List<BookingResponse> bookingResponses = new ArrayList<>();
        for (BookedRoom booking : bookings){
            BookingResponse bookingResponse = getBookingResponse(booking);
            bookingResponses.add(bookingResponse);
        }
        return ResponseEntity.ok(bookingResponses);
    }

    @PostMapping("/room/{roomId}/booking")
    public ResponseEntity<?> saveBooking(
            @PathVariable Long roomId,
            @RequestBody BookedRoom bookingRequest
    ) {
        try {
            String confirmationCode = bookingService.saveBooking(roomId, bookingRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Room booked successfully");
            response.put("confirmationCode", confirmationCode);

            return ResponseEntity.ok(response);

        } catch (InvalidBookingRequestException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }


    @GetMapping("/confirmation/{confirmationCode}")
    public ResponseEntity<?> getBookingByConfirmationCode(@PathVariable String confirmationCode){
        try{
            BookedRoom booking = bookingService.findByBookingConfirmationCode(confirmationCode);
            BookingResponse bookingResponse = getBookingResponse(booking);
            return ResponseEntity.ok(bookingResponse);
        }catch (ResourceNotFoundException ex){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @GetMapping("/user/{email}/bookings")
    public ResponseEntity<List<BookingResponse>> getBookingsByUserEmail(@PathVariable String email) {
        List<BookedRoom> bookings = bookingService.getBookingsByUserEmail(email);
        List<BookingResponse> bookingResponses = new ArrayList<>();
        for (BookedRoom booking : bookings) {
            BookingResponse bookingResponse = getBookingResponse(booking);
            bookingResponses.add(bookingResponse);
        }
        return ResponseEntity.ok(bookingResponses);
    }

    @DeleteMapping("/booking/{bookingId}/delete")
    public void cancelBooking(@PathVariable Long bookingId){
        bookingService.cancelBooking(bookingId);
    }

    private BookingResponse getBookingResponse(BookedRoom booking) {
        Room theRoom = roomService.getRoomById(booking.getRoom().getId()).get();
        RoomResponse room = new RoomResponse(
                theRoom.getId(),
                theRoom.getRoomType(),
                theRoom.getRoomPrice());
        return new BookingResponse(
                booking.getBookingId(), booking.getCheckInDate(),
                booking.getCheckOutDate(),booking.getGuestFullName(),
                booking.getGuestEmail(), booking.getNumOfAdults(),
                booking.getNumOfChildren(), booking.getTotalNumOfGuest(),
                booking.getBookingConfirmationCode(), room);
    }

    @GetMapping("/room/{roomId}/booked-dates")
    public ResponseEntity<List<Map<String, String>>> getBookedDates(@PathVariable Long roomId) {
        List<BookedRoom> bookings = bookingService.getAllBookingsByRoomId(roomId);

        List<Map<String, String>> response = bookings.stream().map(b -> {
            Map<String, String> dateMap = new HashMap<>();
            dateMap.put("start", b.getCheckInDate().toString());
            dateMap.put("end", b.getCheckOutDate().toString());
            return dateMap;
        }).toList();

        return ResponseEntity.ok(response);
    }

}
