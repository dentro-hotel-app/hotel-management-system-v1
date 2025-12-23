package com.dentro.dentrohills.controller;

import com.dentro.dentrohills.exception.ResourceNotFoundException;
import com.dentro.dentrohills.model.BookedRoom;
import com.dentro.dentrohills.model.Room;
import com.dentro.dentrohills.response.BookingResponse;
import com.dentro.dentrohills.response.RoomResponse;
import com.dentro.dentrohills.service.IBookingService;
import com.dentro.dentrohills.service.IRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {

    private final IRoomService roomService;
    private final IBookingService bookingService;

    /* ===================== ADD ROOM (MULTIPLE PHOTOS) ===================== */

    @PostMapping("/add/new-room")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<RoomResponse> addNewRoom(
            @RequestParam("photo") MultipartFile[] photos,
            @RequestParam("roomType") String roomType,
            @RequestParam("roomPrice") BigDecimal roomPrice
    ) throws IOException {

        Room savedRoom = roomService.addNewRoom(photos, roomType, roomPrice);

        RoomResponse response = new RoomResponse(
                savedRoom.getId(),
                savedRoom.getRoomType(),
                savedRoom.getRoomPrice()
        );

        return ResponseEntity.ok(response);
    }

    /* ===================== ROOM TYPES ===================== */

    @GetMapping("/room/types")
    public List<String> getRoomTypes() {
        return roomService.getAllRoomTypes();
    }

    /* ===================== GET ALL ROOMS ===================== */

    @GetMapping("/all-rooms")
    public ResponseEntity<List<RoomResponse>> getAllRooms() {

        List<Room> rooms = roomService.getAllRooms();
        List<RoomResponse> responses = new ArrayList<>();

        for (Room room : rooms) {
            responses.add(buildRoomResponse(room));
        }

        return ResponseEntity.ok(responses);
    }

    /* ===================== GET ROOM BY ID ===================== */

    @GetMapping("/room/{roomId}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long roomId) {

        Room room = roomService.getRoomById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        return ResponseEntity.ok(buildRoomResponse(room));
    }

    /* ===================== AVAILABLE ROOMS ===================== */

    @GetMapping("/available-rooms")
    public ResponseEntity<List<RoomResponse>> getAvailableRooms(
            @RequestParam("checkInDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam("checkOutDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam("roomType") String roomType
    ) {

        List<Room> rooms = roomService.getAvailableRooms(checkInDate, checkOutDate, roomType);
        List<RoomResponse> responses = new ArrayList<>();

        for (Room room : rooms) {
            responses.add(buildRoomResponse(room));
        }

        if (responses.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(responses);
    }

    /* ===================== DELETE ROOM ===================== */

    @DeleteMapping("/delete/room/{roomId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    /* ===================== UPDATE ROOM (NO PHOTO UPDATE HERE) ===================== */
    /*
       Recommended:
       - Handle photo updates via separate endpoint
       - Keeps logic clean
    */

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

    /* ===================== PRIVATE MAPPER ===================== */

    private RoomResponse buildRoomResponse(Room room) {

        List<String> photos = room.getImages()
                .stream()
                .map(img -> java.util.Base64.getEncoder().encodeToString(img.getImage()))
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

        return new RoomResponse(
                room.getId(),
                room.getRoomType(),
                room.getRoomPrice(),
                room.isBooked(),
                photos,
                bookings
        );
    }

}
