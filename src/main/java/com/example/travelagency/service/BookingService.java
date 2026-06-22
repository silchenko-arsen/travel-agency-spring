package com.example.travelagency.service;

import com.example.travelagency.domain.AppUser;
import com.example.travelagency.domain.Booking;
import com.example.travelagency.domain.BookingStatus;
import com.example.travelagency.domain.Tour;
import com.example.travelagency.exception.BusinessException;
import com.example.travelagency.exception.NotFoundException;
import com.example.travelagency.repository.BookingRepository;
import com.example.travelagency.repository.TourRepository;
import com.example.travelagency.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TourRepository tourRepository;

    @Transactional(readOnly = true)
    public Page<Booking> getUserBookings(String email, Pageable pageable) {
        return bookingRepository.findByUserEmail(email, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Booking> getAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Booking> searchAllBookings(
            String keyword,
            BookingStatus status,
            Pageable pageable
    ) {
        return bookingRepository.searchAll(keyword, status, pageable);
    }

    @Transactional
    public Booking bookTour(String email, Long tourId) {
        AppUser user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new NotFoundException("Tour not found"));

        if (tour.getAvailablePlaces() <= 0) {
            throw new BusinessException("booking.error.noPlaces");
        }

        if (!tour.getStartDate().isAfter(LocalDate.now())) {
            throw new BusinessException("booking.error.tourAlreadyStarted");
        }

        if (bookingRepository.existsByUserAndTourAndStatusNot(
                user,
                tour,
                BookingStatus.CANCELED
        )) {
            throw new BusinessException("booking.error.alreadyBooked");
        }

        tour.setAvailablePlaces(tour.getAvailablePlaces() - 1);

        Booking booking = Booking.builder()
                .user(user)
                .tour(tour)
                .status(BookingStatus.REGISTERED)
                .priceAtBooking(tour.getPrice())
                .build();

        Booking saved = bookingRepository.save(booking);

        log.info(
                "Tour booked: bookingId={} user={} tour={}",
                saved.getId(),
                email,
                tourId
        );

        return saved;
    }

    @Transactional
    public void payForBooking(String email, Long bookingId) {
        Booking booking = bookingRepository.findByIdAndUserEmail(bookingId, email)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (booking.getStatus() != BookingStatus.REGISTERED) {
            throw new BusinessException("booking.error.onlyRegisteredCanBePaid");
        }

        AppUser user = booking.getUser();

        BigDecimal balance = user.getBalance() == null
                ? BigDecimal.ZERO
                : user.getBalance();

        if (balance.compareTo(booking.getPriceAtBooking()) < 0) {
            throw new BusinessException("booking.error.notEnoughBalance");
        }

        user.setBalance(balance.subtract(booking.getPriceAtBooking()));
        booking.setStatus(BookingStatus.PAID);

        log.info("Booking paid: bookingId={} user={}", bookingId, email);
    }

    @Transactional
    public void cancelUserBooking(String email, Long bookingId) {
        Booking booking = bookingRepository.findByIdAndUserEmail(bookingId, email)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (!booking.getTour().getStartDate().isAfter(LocalDate.now())) {
            throw new BusinessException("booking.error.cancelNotAllowed");
        }

        if (booking.getStatus() == BookingStatus.CANCELED) {
            return;
        }

        if (booking.getStatus() == BookingStatus.PAID) {
            AppUser user = booking.getUser();

            BigDecimal balance = user.getBalance() == null
                    ? BigDecimal.ZERO
                    : user.getBalance();

            user.setBalance(balance.add(booking.getPriceAtBooking()));
        }

        booking.getTour().setAvailablePlaces(
                booking.getTour().getAvailablePlaces() + 1
        );

        booking.setStatus(BookingStatus.CANCELED);

        log.info("Booking canceled by user: bookingId={} user={}", bookingId, email);
    }

    @Transactional
    public void changeStatus(Long bookingId, BookingStatus newStatus) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (newStatus == null) {
            throw new BusinessException("booking.error.statusRequired");
        }

        BookingStatus oldStatus = booking.getStatus();

        if (oldStatus == newStatus) {
            return;
        }

        Tour tour = booking.getTour();

        if (oldStatus == BookingStatus.CANCELED
                && newStatus != BookingStatus.CANCELED) {

            if (tour.getAvailablePlaces() <= 0) {
                throw new BusinessException("booking.error.noPlaces");
            }

            tour.setAvailablePlaces(tour.getAvailablePlaces() - 1);
        }

        if (oldStatus != BookingStatus.CANCELED
                && newStatus == BookingStatus.CANCELED) {

            tour.setAvailablePlaces(tour.getAvailablePlaces() + 1);
        }

        if (oldStatus == BookingStatus.PAID
                && newStatus == BookingStatus.CANCELED) {

            AppUser user = booking.getUser();

            BigDecimal balance = user.getBalance() == null
                    ? BigDecimal.ZERO
                    : user.getBalance();

            user.setBalance(balance.add(booking.getPriceAtBooking()));
        }

        if (oldStatus == BookingStatus.CANCELED
                && newStatus == BookingStatus.PAID) {

            AppUser user = booking.getUser();

            BigDecimal balance = user.getBalance() == null
                    ? BigDecimal.ZERO
                    : user.getBalance();

            if (balance.compareTo(booking.getPriceAtBooking()) < 0) {
                throw new BusinessException("booking.error.notEnoughBalance");
            }

            user.setBalance(balance.subtract(booking.getPriceAtBooking()));
        }

        booking.setStatus(newStatus);

        log.info(
                "Booking status changed: bookingId={} oldStatus={} newStatus={}",
                bookingId,
                oldStatus,
                newStatus
        );
    }
}