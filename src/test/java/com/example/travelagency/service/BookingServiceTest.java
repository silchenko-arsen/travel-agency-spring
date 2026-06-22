package com.example.travelagency.service;

import com.example.travelagency.domain.AppUser;
import com.example.travelagency.domain.Booking;
import com.example.travelagency.domain.BookingStatus;
import com.example.travelagency.domain.Tour;
import com.example.travelagency.exception.BusinessException;
import com.example.travelagency.repository.BookingRepository;
import com.example.travelagency.repository.TourRepository;
import com.example.travelagency.repository.UserRepository;
import com.example.travelagency.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TourRepository tourRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void bookTour_whenValid_shouldCreateBookingAndDecreasePlaces() {
        AppUser user = user();
        Tour tour = tour();

        when(userRepository.findByEmailIgnoreCase("user@email.com")).thenReturn(Optional.of(user));
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(bookingRepository.existsByUserAndTourAndStatusNot(user, tour, BookingStatus.CANCELED))
                .thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = bookingService.bookTour("user@email.com", 1L);

        assertThat(booking.getUser()).isEqualTo(user);
        assertThat(booking.getTour()).isEqualTo(tour);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.REGISTERED);
        assertThat(booking.getPriceAtBooking()).isEqualByComparingTo("1000");
        assertThat(tour.getAvailablePlaces()).isEqualTo(4);
    }

    @Test
    void bookTour_whenNoPlaces_shouldThrowBusinessException() {
        AppUser user = user();

        Tour tour = tour();
        tour.setAvailablePlaces(0);

        when(userRepository.findByEmailIgnoreCase("user@email.com")).thenReturn(Optional.of(user));
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));

        assertThatThrownBy(() -> bookingService.bookTour("user@email.com", 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("booking.error.noPlaces");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void bookTour_whenTourAlreadyStarted_shouldThrowBusinessException() {
        AppUser user = user();

        Tour tour = tour();
        tour.setStartDate(LocalDate.now());

        when(userRepository.findByEmailIgnoreCase("user@email.com")).thenReturn(Optional.of(user));
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));

        assertThatThrownBy(() -> bookingService.bookTour("user@email.com", 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("booking.error.tourAlreadyStarted");
    }

    @Test
    void bookTour_whenAlreadyBooked_shouldThrowBusinessException() {
        AppUser user = user();
        Tour tour = tour();

        when(userRepository.findByEmailIgnoreCase("user@email.com")).thenReturn(Optional.of(user));
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(bookingRepository.existsByUserAndTourAndStatusNot(user, tour, BookingStatus.CANCELED))
                .thenReturn(true);

        assertThatThrownBy(() -> bookingService.bookTour("user@email.com", 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("booking.error.alreadyBooked");
    }

    @Test
    void payForBooking_whenEnoughBalance_shouldPayAndSubtractBalance() {
        AppUser user = user();
        user.setBalance(new BigDecimal("1500"));

        Booking booking = booking(user, tour(), BookingStatus.REGISTERED);

        when(bookingRepository.findByIdAndUserEmail(10L, "user@email.com"))
                .thenReturn(Optional.of(booking));

        bookingService.payForBooking("user@email.com", 10L);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PAID);
        assertThat(user.getBalance()).isEqualByComparingTo("500");
    }

    @Test
    void payForBooking_whenStatusIsNotRegistered_shouldThrowBusinessException() {
        Booking booking = booking(user(), tour(), BookingStatus.PAID);

        when(bookingRepository.findByIdAndUserEmail(10L, "user@email.com"))
                .thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.payForBooking("user@email.com", 10L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("booking.error.onlyRegisteredCanBePaid");
    }

    @Test
    void payForBooking_whenNotEnoughBalance_shouldThrowBusinessException() {
        AppUser user = user();
        user.setBalance(new BigDecimal("100"));

        Booking booking = booking(user, tour(), BookingStatus.REGISTERED);

        when(bookingRepository.findByIdAndUserEmail(10L, "user@email.com"))
                .thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.payForBooking("user@email.com", 10L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("booking.error.notEnoughBalance");

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.REGISTERED);
    }

    @Test
    void cancelUserBooking_whenPaid_shouldCancelReturnMoneyAndIncreasePlaces() {
        AppUser user = user();
        user.setBalance(new BigDecimal("500"));

        Tour tour = tour();
        tour.setAvailablePlaces(4);

        Booking booking = booking(user, tour, BookingStatus.PAID);

        when(bookingRepository.findByIdAndUserEmail(10L, "user@email.com"))
                .thenReturn(Optional.of(booking));

        bookingService.cancelUserBooking("user@email.com", 10L);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELED);
        assertThat(user.getBalance()).isEqualByComparingTo("1500");
        assertThat(tour.getAvailablePlaces()).isEqualTo(5);
    }

    @Test
    void cancelUserBooking_whenTourAlreadyStarted_shouldThrowBusinessException() {
        Tour tour = tour();
        tour.setStartDate(LocalDate.now());

        Booking booking = booking(user(), tour, BookingStatus.REGISTERED);

        when(bookingRepository.findByIdAndUserEmail(10L, "user@email.com"))
                .thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelUserBooking("user@email.com", 10L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("booking.error.cancelNotAllowed");
    }

    @Test
    void changeStatus_whenNewStatusIsNull_shouldThrowBusinessException() {
        Booking booking = booking(user(), tour(), BookingStatus.REGISTERED);

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.changeStatus(10L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("booking.error.statusRequired");
    }

    @Test
    void changeStatus_fromCanceledToRegistered_shouldDecreasePlaces() {
        Tour tour = tour();
        tour.setAvailablePlaces(3);

        Booking booking = booking(user(), tour, BookingStatus.CANCELED);

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        bookingService.changeStatus(10L, BookingStatus.REGISTERED);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.REGISTERED);
        assertThat(tour.getAvailablePlaces()).isEqualTo(2);
    }

    @Test
    void changeStatus_fromPaidToCanceled_shouldReturnMoneyAndIncreasePlaces() {
        AppUser user = user();
        user.setBalance(new BigDecimal("100"));

        Tour tour = tour();
        tour.setAvailablePlaces(2);

        Booking booking = booking(user, tour, BookingStatus.PAID);

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        bookingService.changeStatus(10L, BookingStatus.CANCELED);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELED);
        assertThat(tour.getAvailablePlaces()).isEqualTo(3);
        assertThat(user.getBalance()).isEqualByComparingTo("1100");
    }

    private AppUser user() {
        AppUser user = new AppUser();
        user.setEmail("user@email.com");
        user.setBalance(new BigDecimal("2000"));
        return user;
    }

    private Tour tour() {
        return Tour.builder()
                .title("Paris")
                .description("Paris tour")
                .country("France")
                .city("Paris")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(15))
                .price(new BigDecimal("1000"))
                .availablePlaces(5)
                .build();
    }

    private Booking booking(AppUser user, Tour tour, BookingStatus status) {
        return Booking.builder()
                .user(user)
                .tour(tour)
                .status(status)
                .priceAtBooking(new BigDecimal("1000"))
                .build();
    }
}