package com.dentro.dentrohills.service;

import com.dentro.dentrohills.exception.ResourceNotFoundException;
import com.dentro.dentrohills.model.Hospital;
import com.dentro.dentrohills.model.Room;
import com.dentro.dentrohills.model.RoomImage;
import com.dentro.dentrohills.repository.HospitalRepository;
import com.dentro.dentrohills.repository.RoomRepository;
import com.dentro.dentrohills.response.BookingResponse;
import com.dentro.dentrohills.response.RoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService implements IRoomService {

    private final RoomRepository roomRepository;
    private final HospitalRepository hospitalRepository;

    /** Add new room with multiple photos and hospital reference */
    @Override
    public Room addNewRoom(MultipartFile[] files, String roomType, BigDecimal roomPrice, Long hospitalId) throws IOException {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));

        Room room = new Room();
        room.setRoomType(roomType);
        room.setRoomPrice(roomPrice);
        room.setNearestHospitalEntity(hospital);

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                RoomImage img = new RoomImage();
                img.setImage(file.getBytes());
                img.setRoom(room);
                room.getImages().add(img);
            }
        }

        Room savedRoom = roomRepository.save(room);
        return roomRepository.findById(savedRoom.getId()).orElseThrow();
    }

    /** Get rooms by hospital name */
    @Override
    public List<RoomResponse> getRoomsByHospital(String hospitalName) {
        return roomRepository.findByHospitalName(hospitalName)
                .stream()
                .map(room -> {
                    List<String> photos = room.getImages().stream()
                            .map(img -> Base64.getEncoder().encodeToString(img.getImage()))
                            .toList();

                    return new RoomResponse(
                            room.getId(),
                            room.getRoomType(),
                            room.getRoomPrice(),
                            room.isBooked(),
                            photos,
                            List.of(),
                            room.getNearestHospitalEntity().getName()
                    );
                })
                .toList();
    }

    /** Get all room types */
    @Override
    public List<String> getAllRoomTypes() {

        List<String> types = roomRepository.findDistinctRoomTypes();

        if (types == null || types.isEmpty()) {
            return List.of(
                    "Single",
                    "Double",
                    "Deluxe",
                    "ICU Recovery",
                    "Family Stay"
            );
        }

        return types;
    }


    /** Get all rooms */
    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll()
                .stream()
                .map(room -> new RoomResponse(
                        room.getId(),
                        room.getRoomType(),
                        room.getRoomPrice(),
                        room.isBooked(),
                        room.getImages().stream()
                                .map(img -> Base64.getEncoder().encodeToString(img.getImage()))
                                .toList(),
                        List.of(), // ❗ no bookings here
                        room.getNearestHospitalEntity() != null
                                ? room.getNearestHospitalEntity().getName()
                                : "N/A"
                ))
                .toList();
    }
    /** Delete a room by ID */
    @Override
    public void deleteRoom(Long roomId) {
        Optional<Room> room = roomRepository.findById(roomId);
        room.ifPresent(value -> roomRepository.deleteById(roomId));
    }

    /** Update room type and price */
    @Override
    public Room updateRoom(Long roomId, String roomType, BigDecimal roomPrice) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (roomType != null) room.setRoomType(roomType);
        if (roomPrice != null) room.setRoomPrice(roomPrice);

        return roomRepository.save(room);
    }

    /** Get room by ID */
    @Override
    public Optional<Room> getRoomById(Long roomId) {
        return roomRepository.findById(roomId);
    }

    /** Get available rooms by date and type */
    @Override
    public List<Room> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate, String roomType) {
        return roomRepository.findAvailableRoomsByDatesAndType(checkInDate, checkOutDate, roomType);
    }
}
