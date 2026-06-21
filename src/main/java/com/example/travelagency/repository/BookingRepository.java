package com.example.travelagency.repository;

import com.example.travelagency.domain.Booking;
import com.example.travelagency.domain.BookingStatus;
import com.example.travelagency.domain.Tour;
import com.example.travelagency.domain.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @EntityGraph(attributePaths = {"user", "tour"})
    Page<Booking> findByUserEmail(String email, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "tour"})
    Optional<Booking> findByIdAndUserEmail(Long id, String email);

    @Override
    @EntityGraph(attributePaths = {"user", "tour"})
    Page<Booking> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"user", "tour"})
    Optional<Booking> findById(Long id);

    boolean existsByUserAndTourAndStatusNot(AppUser user, Tour tour, BookingStatus status);
}
