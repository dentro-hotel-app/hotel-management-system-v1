package com.dentro.dentrohills.service;

import com.dentro.dentrohills.model.Room;
import com.dentro.dentrohills.response.RoomResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IRoomService {

    Room addNewRoom(MultipartFile[] photo, String roomType, BigDecimal roomPrice, Long hospitalId) throws IOException;

    List<String> getAllRoomTypes();

    List<RoomResponse> getAllRooms();

    //byte[] getRoomPhotoByRoomId(Long roomId) throws SQLException;

    void deleteRoom(Long roomId);

    Room updateRoom(Long roomId, String roomType, BigDecimal roomPrice);

    Optional<Room> getRoomById(Long roomId);

    List<Room> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate, String roomType);

    List<RoomResponse> getRoomsByHospital(String hospitalName);
}
