package com.rang.lakesidehotel.repository;

import com.rang.lakesidehotel.model.BookedRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<BookedRoom,Long> {
    List<BookedRoom> findByRoomId(Long roomId);

    //make BookedRoom optional so we can actually return a message when this booking is not found
    Optional<BookedRoom> findByBookingConfirmationCode(String confirmationCode);

    List<BookedRoom> findByGuestEmail(String email);
}
