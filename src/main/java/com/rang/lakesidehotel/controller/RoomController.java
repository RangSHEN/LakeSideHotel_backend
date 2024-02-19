package com.rang.lakesidehotel.controller;

import com.rang.lakesidehotel.exception.ResourceNotFoundException;
import com.rang.lakesidehotel.model.BookedRoom;
import com.rang.lakesidehotel.model.Room;
import com.rang.lakesidehotel.response.RoomDto;
import com.rang.lakesidehotel.service.BookingService;
import com.rang.lakesidehotel.service.RoomService;
import lombok.AllArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/rooms")
//@CrossOrigin("*")
public class RoomController {

    private final RoomService roomService;
    private final BookingService bookingService;

    @PostMapping("/add/new-room")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RoomDto> addNewRoom(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("roomType") String roomType,
            @RequestParam("roomPrice") BigDecimal roomPrice) throws SQLException, IOException {

        Room savedRoom = roomService.addNewRoom(photo, roomType, roomPrice);
        RoomDto roomDto = new RoomDto(savedRoom.getId(), savedRoom.getRoomType(), savedRoom.getRoomPrice());

        return new ResponseEntity<>(roomDto, HttpStatus.OK);
    }

    @GetMapping("/room/types")
    public ResponseEntity<List<String>> getRoomTypes(){
        List<String> roomTypes = roomService.getAllRoomTypes();


        return new ResponseEntity<>(roomTypes, HttpStatus.OK);
    }

    @GetMapping("/all-rooms")
    public ResponseEntity<List<RoomDto>> getAllRooms() throws SQLException {
        List<Room> rooms = roomService.getAllRooms();
        List<RoomDto> roomDtos = new ArrayList<>();
        // RoomDto photo type is string

        for (Room room : rooms) {
            byte[] photoBytes = roomService.getRoomPhotoByRoomId(room.getId());
            if(photoBytes != null && photoBytes.length>0){
                String base64Photo = Base64.encodeBase64String(photoBytes);
                RoomDto roomDto = getRoomDto(room);
                roomDto.setPhoto(base64Photo);
                roomDtos.add(roomDto);
            }
        }

        return new ResponseEntity<>(roomDtos,HttpStatus.OK);
    }

    @DeleteMapping("/delete/room/{roomId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable("roomId") Long id){
        roomService.deleteRoom(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/update/{roomId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RoomDto> updateRoom(@PathVariable Long roomId,
                                              @RequestParam(required = false) String roomType,
                                              @RequestParam(required = false) BigDecimal roomPrice,
                                              @RequestParam(required = false) MultipartFile photo) throws IOException, SQLException {
        byte[] photoBytes;
        if(photo != null && !photo.isEmpty()){
            photoBytes = photo.getBytes();
        } else {
            photoBytes = roomService.getRoomPhotoByRoomId(roomId); //防止用户先添加又删除了照片
        }
        //Blob photoBlob = new SerialBlob(photoBytes);

        Room theRoom = roomService.updateRoom(roomId, roomType, roomPrice, photoBytes);
        //theRoom.setPhoto(photoBlob);

        RoomDto roomDto = getRoomDto(theRoom);
        String base64Photo = Base64.encodeBase64String(photoBytes);
        roomDto.setPhoto(base64Photo);

        return new ResponseEntity<>(roomDto,HttpStatus.OK);
    }

    @GetMapping("/room/{roomId}")
    //ResponseEntity<Optional<RoomDto>>Optional 类型是Java中用于表示可能为null的值的容器。
    public ResponseEntity<Optional<RoomDto>> getRoomById(@PathVariable("roomId") Long id) throws SQLException {
        Optional<Room> theRoom = roomService.getRoomById(id);
        byte [] photoBytes = roomService.getRoomPhotoByRoomId(id);
        String base64Photo = Base64.encodeBase64String(photoBytes);
        return theRoom.map(room -> {
            RoomDto roomDto = getRoomDto(room);
            roomDto.setPhoto(base64Photo);
            return ResponseEntity.ok(Optional.of(roomDto));
        }).orElseThrow(()-> new ResourceNotFoundException("Room not found"));

    }

    @GetMapping("/available-rooms")
    public ResponseEntity<List<RoomDto>> getAvailableRooms(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate, //yyyy-mm-dd
                                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
                                                           @RequestParam String roomType) throws SQLException {
        List<Room> availableRooms = roomService.getAvailableRooms(checkInDate,checkOutDate,roomType);
        List<RoomDto> roomDtos = new ArrayList<>();

        for (Room room : availableRooms) {
            byte[] photoByte = roomService.getRoomPhotoByRoomId(room.getId());
            if(photoByte != null && photoByte.length>0){
                String photoBased64 = Base64.encodeBase64String(photoByte);
                RoomDto roomDto = getRoomDto(room);
                roomDto.setPhoto(photoBased64);
                roomDtos.add(roomDto);
            }
        }

        if (roomDtos.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }else {
            return new ResponseEntity<>(roomDtos,HttpStatus.OK);
        }
    }


    private RoomDto getRoomDto(Room room) {

        List<BookedRoom> bookings = getAllBookingsByRoomId(room.getId());
//        List<BookingDto> bookingInfo = bookings.stream().map(
//                booking -> new BookingDto(booking.getBookingId(),
//                        booking.getCheckInDate(),
//                        booking.getCheckOutDate(),
//                        booking.getBookingConfirmationCode())).collect(Collectors.toList());

        RoomDto roomDto = new RoomDto();
        BeanUtils.copyProperties(room, roomDto);
//        roomDto.setBookings(bookingInfo);
        return roomDto;
    }

    private List<BookedRoom> getAllBookingsByRoomId(Long roomId) {
        return bookingService.getAllBookingsByRoomId(roomId);

    }
}
