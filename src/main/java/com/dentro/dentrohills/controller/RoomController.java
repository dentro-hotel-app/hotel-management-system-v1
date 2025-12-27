package com.dentro.dentrohills.controller;

import com.dentro.dentrohills.exception.ResourceNotFoundException;
import com.dentro.dentrohills.model.Room;
import com.dentro.dentrohills.response.BookingResponse;
import com.dentro.dentrohills.response.RoomResponse;
import com.dentro.dentrohills.service.IBookingService;
import com.dentro.dentrohills.service.IRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
@CrossOrigin(origins = "http://localhost:5173")
public class RoomController {

    private final IRoomService roomService;
    private final IBookingService bookingService;

    /* ===================== ADD ROOM ===================== */

    @PostMapping("/add/new-room")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<RoomResponse> addNewRoom(
            @RequestParam("photo") MultipartFile[] photos,
            @RequestParam("roomType") String roomType,
            @RequestParam("roomPrice") BigDecimal roomPrice,
            @RequestParam("hospitalId") Long hospitalId
    ) throws IOException {

        Room savedRoom = roomService.addNewRoom(
                photos,
                roomType,
                roomPrice,
                hospitalId
        );

        return ResponseEntity.ok(buildRoomResponse(savedRoom));
    }

    /* ===================== GET ROOMS BY HOSPITAL ===================== */
    // Used when user clicks hospital on homepage

    @GetMapping("/by-hospital")
    public ResponseEntity<List<RoomResponse>> getRoomsByHospital(
            @RequestParam String hospitalName) {

        List<RoomResponse> rooms =
                roomService.getRoomsByHospital(hospitalName);

        if (rooms.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(rooms);
    }

    /* ===================== ROOM TYPES ===================== */

    @GetMapping("/room/types")
    public ResponseEntity<List<String>> getRoomTypes() {
        return ResponseEntity.ok(roomService.getAllRoomTypes());
    }

    /* ===================== GET ALL ROOMS ===================== */

    @GetMapping("/all-rooms")
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        List<RoomResponse> responses = roomService.getAllRooms(); // already RoomResponse
        return ResponseEntity.ok(responses);
    }

    /* ===================== GET ROOM BY ID ===================== */

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long roomId) {

        Room room = roomService.getRoomById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        return ResponseEntity.ok(buildRoomResponse(room));
    }

    /* ===================== AVAILABLE ROOMS ===================== */

    @GetMapping("/available-rooms")
    public ResponseEntity<List<RoomResponse>> getAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkInDate,

            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkOutDate,

            @RequestParam String roomType
    ) {

        List<RoomResponse> responses = roomService
                .getAvailableRooms(checkInDate, checkOutDate, roomType)
                .stream()
                .map(this::buildRoomResponse)
                .toList();

        if (responses.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(responses);
    }

    /* ===================== UPDATE ROOM ===================== */

    @PutMapping("/update/{roomId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable Long roomId,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) BigDecimal roomPrice
    ) {

        Room updatedRoom = roomService.updateRoom(roomId, roomType, roomPrice);
        return ResponseEntity.ok(buildRoomResponse(updatedRoom));
    }

    /* ===================== DELETE ROOM ===================== */

    @DeleteMapping("/delete/{roomId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    /* ===================== RESPONSE MAPPER ===================== */

    private RoomResponse buildRoomResponse(Room room) {

        List<String> photos = room.getImages()
                .stream()
                .map(img -> Base64.getEncoder().encodeToString(img.getImage()))
                .toList();

        List<BookingResponse> bookings = bookingService
                .getAllBookingsByRoomId(room.getId())
                .stream()
                .map(b -> new BookingResponse(
                        b.getBookingId(),
                        b.getCheckInDate(),
                        b.getCheckOutDate(),
                        b.getBookingConfirmationCode()
                ))
                .toList();

        String hospitalName = room.getNearestHospitalEntity() != null
                ? room.getNearestHospitalEntity().getName()
                : "N/A";

        return new RoomResponse(
                room.getId(),
                room.getRoomType(),
                room.getRoomPrice(),
                room.isBooked(),
                photos,
                bookings,
                hospitalName
        );
    }
}
