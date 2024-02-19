package com.rang.lakesidehotel.repository;

import com.rang.lakesidehotel.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room,Long> {

    @Query("SELECT DISTINCT r.roomType FROM Room r")
    List<String> findDistinctRoomTypes();


    @Query("SELECT r from Room r where r.roomType like %:roomType% " +
            "And r.id not in( SELECT br.room.id from BookedRoom br" +
            " WHERE ((br.checkInDate <= :checkOutDate) AND (br.checkOutDate >= :checkInDate)))")
    List<Room> findAvailableRoomsByDatesAndType(LocalDate checkInDate, LocalDate checkOutDate, String roomType);
}
