package com.example.travelagency.service;

import com.example.travelagency.domain.Booking;
import com.example.travelagency.domain.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {

    Page<Booking> getUserBookings(String email, Pageable pageable);

    Page<Booking> searchAllBookings(String keyword, BookingStatus status, Pageable pageable);

    Booking bookTour(String email, Long tourId);

    void payForBooking(String email, Long bookingId);

    void cancelUserBooking(String email, Long bookingId);

    void changeStatus(Long bookingId, BookingStatus newStatus);
}