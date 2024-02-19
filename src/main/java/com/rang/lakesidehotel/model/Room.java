package com.rang.lakesidehotel.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String roomType;

    private BigDecimal roomPrice;

    private boolean isBooked = false;

    //photo
    @Lob
    private Blob photo;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BookedRoom> bookings;

//    when a new room is been added to database to our hotel, at that initial stage the room has not be booked
//    if we tend to query all rooms at that moment we're going to run into a NullPointerException because we're
//    going to be pointing to these bookings as well and these bookings at that moment is going to be null
    public Room() {
        //hold an empty array of which we can query to avoid pointer exception
        this.bookings = new ArrayList<>();
    }

    public void addBooking(BookedRoom booking){
        if (bookings == null){
            bookings = new ArrayList<>();
        }
        bookings.add(booking);
        booking.setRoom(this);
        isBooked = true;
        String bookingCode = RandomStringUtils.randomNumeric(10);
        booking.setBookingConfirmationCode(bookingCode);
    }

}
