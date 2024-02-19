package com.rang.lakesidehotel.controller;

import com.rang.lakesidehotel.exception.InvalidBookingRequestException;
import com.rang.lakesidehotel.exception.ResourceNotFoundException;
import com.rang.lakesidehotel.model.BookedRoom;
import com.rang.lakesidehotel.model.Room;
import com.rang.lakesidehotel.response.BookingDto;
import com.rang.lakesidehotel.response.RoomDto;
import com.rang.lakesidehotel.service.BookingService;
import com.rang.lakesidehotel.service.RoomService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/bookings")
//@CrossOrigin("*")
public class BookingRoomController {

    private final BookingService bookingService;
    private final RoomService roomService;

    @GetMapping("/all-bookings")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<BookingDto>> getAllBookings(){

        List<BookedRoom> bookings = bookingService.getAllBookings();

        List<BookingDto> bookingDtos = new ArrayList<>();
        for (BookedRoom booking : bookings) {
            BookingDto bookingDto = getBookingDto(booking);
            bookingDtos.add(bookingDto);
        }

        return new ResponseEntity<>(bookingDtos, HttpStatus.OK);
    }


    @GetMapping("/confirmation/{confirmationCode}")
    public ResponseEntity<?> getBookingByConfirmationCode(@PathVariable String confirmationCode){

        try {
            BookedRoom booking = bookingService.findByBookingConfirmationCode(confirmationCode);
            BookingDto bookingDto = getBookingDto(booking);
            return new ResponseEntity<>(bookingDto,HttpStatus.OK);
        }catch (ResourceNotFoundException ex){
            return new ResponseEntity<>(ex.getMessage(),HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{userId}/bookings")
    public ResponseEntity<List<BookingDto>> getBookingsByUserEmail(@PathVariable("userId") String email){
        List<BookedRoom> bookings = bookingService.getBookingsByUserEmail(email);
        List<BookingDto> bookingDtos = new ArrayList<>();
        for (BookedRoom booking : bookings) {
            BookingDto bookingDto = getBookingDto(booking);
            bookingDtos.add(bookingDto);
        }
        return new ResponseEntity<>(bookingDtos, HttpStatus.OK);
    }

    @PostMapping("/room/{roomId}/booking")
    public ResponseEntity<?> saveBooking(@PathVariable Long roomId, @RequestBody BookedRoom bookingRequest){
        try{
            String confirmationCode = bookingService.saveBooking(roomId,bookingRequest);
            return new ResponseEntity<>("Room booked successfully, Your booking confirmation Code is :" + confirmationCode, HttpStatus.OK);
        }catch (InvalidBookingRequestException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/booking/{bookingId}/delete")
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and #email == principal.username)")
    public void cancelBooking(@PathVariable Long bookingId){
        bookingService.cancelBooking(bookingId);
    }


    private BookingDto getBookingDto(BookedRoom booking) {
        Room theRoom = roomService.getRoomById(booking.getRoom().getId()).get();
        RoomDto room = new RoomDto(theRoom.getId(), theRoom.getRoomType(), theRoom.getRoomPrice());
        return new BookingDto(
                booking.getBookingId(), booking.getCheckInDate(),
                booking.getCheckOutDate(), booking.getGuestFullName(),
                booking.getGuestEmail(), booking.getNumOfAdults(),
                booking.getNumOfChildren(), booking.getTotalNumOfGuest(),
                booking.getBookingConfirmationCode(), room
        );
    }
}
