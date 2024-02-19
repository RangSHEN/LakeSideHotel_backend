package com.rang.lakesidehotel.service.Impl;

import com.rang.lakesidehotel.exception.InvalidBookingRequestException;
import com.rang.lakesidehotel.exception.ResourceNotFoundException;
import com.rang.lakesidehotel.model.BookedRoom;
import com.rang.lakesidehotel.model.Room;
import com.rang.lakesidehotel.repository.BookingRepository;
import com.rang.lakesidehotel.security.HotelSecurityConfig;
import com.rang.lakesidehotel.service.BookingService;
import com.rang.lakesidehotel.service.RoomService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomService roomService;
    @Override
    public List<BookedRoom> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public BookedRoom findByBookingConfirmationCode(String confirmationCode) {
        return bookingRepository.findByBookingConfirmationCode(confirmationCode)
                .orElseThrow(()-> new ResourceNotFoundException("No booking found with booking code:" +confirmationCode));
    }

    @Override
    public String saveBooking(Long roomId, BookedRoom bookingRequest) {
        if (bookingRequest.getCheckOutDate().isBefore(bookingRequest.getCheckInDate())){
            throw new InvalidBookingRequestException("Check-in date must come before check-out date");
        }
        //首先获取room的book信息
        Room room = roomService.getRoomById(roomId).get();
        List<BookedRoom> existingBookings = room.getBookings();
        boolean roomIsAvailable = roomsIsAvailable(bookingRequest,existingBookings);
        if (roomIsAvailable){
            room.addBooking(bookingRequest);
            bookingRepository.save(bookingRequest);
        }else {
            throw new InvalidBookingRequestException("Sorry, This room is not available for the selected dates;");
        }

        return bookingRequest.getBookingConfirmationCode();
    }


    @Override
    public void cancelBooking(Long bookingId) {
        bookingRepository.deleteById(bookingId);
    }

    @Override
    public List<BookedRoom> getBookingsByUserEmail(String email) {
        return bookingRepository.findByGuestEmail(email);
    }

    @Override
    public List<BookedRoom> getAllBookingsByRoomId(Long roomId) {
        return bookingRepository.findByRoomId(roomId);
    }

    // if the room has booked from 10/08/2024 to 15/08/2024 another book from 12/08/2024 to 14/08/2024 that is not available
    private boolean roomsIsAvailable(BookedRoom bookingRequest, List<BookedRoom> existingBookings) {
        //noneMatch都不满足返回 true（负负得正）  || 有一个条件为真 则noneMatch返回false  && 两个条件都是真 noneMatch返回false
        //  && 其中一个可以为假返回true
        return existingBookings.stream()
                .noneMatch(existingBooking ->
                    bookingRequest.getCheckInDate().equals(existingBooking.getCheckInDate())
                        || bookingRequest.getCheckOutDate().isBefore(existingBooking.getCheckOutDate())
                        || (bookingRequest.getCheckInDate().isAfter(existingBooking.getCheckInDate()))
                        && bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckOutDate())
                        || (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate()))

                        && bookingRequest.getCheckOutDate().equals(existingBooking.getCheckOutDate())
                        || (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate()))

                        && bookingRequest.getCheckOutDate().isAfter(existingBooking.getCheckOutDate())

                        || (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate())
                        && bookingRequest.getCheckOutDate().equals(existingBooking.getCheckInDate()))

                        || (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate())
                        && bookingRequest.getCheckOutDate().equals(bookingRequest.getCheckInDate()))

                );

    }

}
