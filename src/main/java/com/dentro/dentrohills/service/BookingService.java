package com.dentro.dentrohills.service;

import com.dentro.dentrohills.exception.InvalidBookingRequestException;
import com.dentro.dentrohills.exception.ResourceNotFoundException;
import com.dentro.dentrohills.model.BookedRoom;
import com.dentro.dentrohills.model.Room;
import com.dentro.dentrohills.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService implements IBookingService {
    private final BookingRepository bookingRepository;
    private final IRoomService roomService;

    @Override
    public List<BookedRoom> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public List<BookedRoom> getBookingsByUserEmail(String email) {
        return bookingRepository.findByGuestEmail(email);
    }

    @Override
    public void cancelBooking(Long bookingId) {
        bookingRepository.deleteById(bookingId);
    }

    @Override
    public List<BookedRoom> getAllBookingsByRoomId(Long roomId) {
        return bookingRepository.findByRoomId(roomId);
    }

    @Override
    public String saveBooking(Long roomId, BookedRoom bookingRequest) {
        // Validate dates
        if (bookingRequest.getCheckOutDate().isBefore(bookingRequest.getCheckInDate()) ||
                bookingRequest.getCheckInDate().isEqual(bookingRequest.getCheckOutDate())) {
            throw new InvalidBookingRequestException("Check-in date must be before check-out date");
        }

        // Check if check-in is in the past
        if (bookingRequest.getCheckInDate().isBefore(LocalDate.now())) {
            throw new InvalidBookingRequestException("Check-in date cannot be in the past");
        }

        // Get room
        Room room = roomService.getRoomById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));

        // Set room relationship
        bookingRequest.setRoom(room);

        // Generate confirmation code if not already set
        if (bookingRequest.getBookingConfirmationCode() == null ||
                bookingRequest.getBookingConfirmationCode().isEmpty()) {
            String confirmationCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            bookingRequest.setBookingConfirmationCode(confirmationCode);
        }

        // Calculate total guests
        bookingRequest.setTotalNumOfGuest(
                bookingRequest.getNumOfAdults() + bookingRequest.getNumOfChildren()
        );

        // Check availability
        List<BookedRoom> existingBookings = bookingRepository.findByRoomId(roomId);
        if (!isRoomAvailable(bookingRequest, existingBookings)) {
            throw new InvalidBookingRequestException("Sorry, this room is not available for the selected dates");
        }

        // Save booking
        bookingRepository.save(bookingRequest);

        return bookingRequest.getBookingConfirmationCode();
    }

    @Override
    public BookedRoom findByBookingConfirmationCode(String confirmationCode) {
        return bookingRepository.findByBookingConfirmationCode(confirmationCode)
                .orElseThrow(() -> new ResourceNotFoundException("No booking found with booking code: " + confirmationCode));
    }

    /**
     * Simplified and corrected room availability check
     */
    private boolean isRoomAvailable(BookedRoom newBooking, List<BookedRoom> existingBookings) {
        // If no existing bookings, room is available
        if (existingBookings == null || existingBookings.isEmpty()) {
            return true;
        }

        // Check for date overlap
        for (BookedRoom existingBooking : existingBookings) {
            if (datesOverlap(newBooking, existingBooking)) {
                return false; // Room is booked for these dates
            }
        }

        return true; // No overlap found, room is available
    }

    /**
     * Check if two booking periods overlap
     * Two periods overlap if:
     * - New check-in is before existing check-out AND new check-out is after existing check-in
     */
    private boolean datesOverlap(BookedRoom newBooking, BookedRoom existingBooking) {
        return !(newBooking.getCheckOutDate().isBefore(existingBooking.getCheckInDate()) ||
                newBooking.getCheckInDate().isAfter(existingBooking.getCheckOutDate()));
    }

    /**
     * Alternative simpler implementation using LocalDate ranges
     */
    private boolean isRoomAvailableSimple(BookedRoom newBooking, List<BookedRoom> existingBookings) {
        return existingBookings.stream()
                .noneMatch(existing ->
                        // Check for overlap
                        (newBooking.getCheckInDate().isBefore(existing.getCheckOutDate()) &&
                                newBooking.getCheckOutDate().isAfter(existing.getCheckInDate()))
                );
    }
}