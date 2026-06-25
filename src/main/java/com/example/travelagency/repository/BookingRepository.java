package com.example.travelagency.repository;

import com.example.travelagency.domain.AppUser;
import com.example.travelagency.domain.Booking;
import com.example.travelagency.domain.BookingStatus;
import com.example.travelagency.domain.Tour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @EntityGraph(attributePaths = {"user", "tour"})
    @Query("""
            select b from Booking b
            join b.user u
            join b.tour t
            where (:status is null or b.status = :status)
              and (
                    :keyword is null
                    or :keyword = ''
                    or lower(u.email) like lower(concat('%', :keyword, '%'))
                    or lower(u.firstName) like lower(concat('%', :keyword, '%'))
                    or lower(u.lastName) like lower(concat('%', :keyword, '%'))
                    or lower(t.title) like lower(concat('%', :keyword, '%'))
                    or lower(t.country) like lower(concat('%', :keyword, '%'))
                    or lower(t.city) like lower(concat('%', :keyword, '%'))
              )
            """)
    Page<Booking> search(
            @Param("keyword") String keyword,
            @Param("status") BookingStatus status,
            Pageable pageable
    );
}