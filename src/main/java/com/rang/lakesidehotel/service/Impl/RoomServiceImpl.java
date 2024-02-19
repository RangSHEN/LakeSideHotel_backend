package com.rang.lakesidehotel.service.Impl;

import com.rang.lakesidehotel.exception.InternalServerException;
import com.rang.lakesidehotel.exception.ResourceNotFoundException;
import com.rang.lakesidehotel.model.Room;
import com.rang.lakesidehotel.repository.RoomRepository;
import com.rang.lakesidehotel.service.RoomService;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@AllArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    @Override
    public Room addNewRoom(MultipartFile photo, String roomType, BigDecimal roomPrice) throws SQLException, IOException {
        Room room = new Room();
        room.setRoomType(roomType);
        room.setRoomPrice(roomPrice);

        if(!photo.isEmpty()){
            byte[] photoBytes = photo.getBytes();
            Blob photoBlob = new SerialBlob(photoBytes);
            room.setPhoto(photoBlob);
        }

        return roomRepository.save(room);

    }

    @Override
    public List<String> getAllRoomTypes() {
        return roomRepository.findDistinctRoomTypes();
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public byte[] getRoomPhotoByRoomId(Long roomId) throws SQLException {
        Optional<Room> theRoom = roomRepository.findById(roomId);
        if (theRoom.isEmpty()) {
            throw new ResourceNotFoundException("Sorry, Room not found!");
        }
        Blob photoBlob = theRoom.get().getPhoto();
        if (photoBlob != null) {
            return photoBlob.getBytes(1, (int) photoBlob.length());
        }


        return null;
    }

    @Override
    public void deleteRoom(Long id) {
        Optional<Room> roomOptional = roomRepository.findById(id);
        if(roomOptional.isPresent()){
            roomRepository.deleteById(id);
        }
    }

    @Override
    public Room updateRoom(Long roomId, String roomType, BigDecimal roomPrice, byte[] photoBytes) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        if (roomType != null){
            room.setRoomType(roomType);
        }
        if (roomPrice != null){
            room.setRoomPrice(roomPrice);
        }
        if (photoBytes.length > 0 && photoBytes != null){
            try {
                room.setPhoto(new SerialBlob(photoBytes));
            }catch (SQLException ex){
                throw new InternalServerException("Error updating room");
            }
        }

        return roomRepository.save(room);
    }

    @Override
    public Optional<Room> getRoomById(Long id) {
        return Optional.of(roomRepository.findById(id).get());
    }

    @Override
    public List<Room> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate, String roomType) {
        return roomRepository.findAvailableRoomsByDatesAndType(checkInDate,checkOutDate,roomType);
    }
}
