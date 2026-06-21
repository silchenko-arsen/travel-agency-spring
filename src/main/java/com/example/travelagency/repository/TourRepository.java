package com.example.travelagency.repository;

import com.example.travelagency.domain.Tour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TourRepository extends JpaRepository<Tour, Long> {

    @Query("""
            select t from Tour t
            where :keyword is null or :keyword = ''
               or lower(t.title) like lower(concat('%', :keyword, '%'))
               or lower(t.country) like lower(concat('%', :keyword, '%'))
               or lower(t.city) like lower(concat('%', :keyword, '%'))
            """)
    Page<Tour> search(@Param("keyword") String keyword, Pageable pageable);
}
