package com.example.travelagency.repository;

import com.example.travelagency.domain.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
            select u from AppUser u
            where :keyword is null or :keyword = ''
               or lower(u.email) like lower(concat('%', :keyword, '%'))
               or lower(u.firstName) like lower(concat('%', :keyword, '%'))
               or lower(u.lastName) like lower(concat('%', :keyword, '%'))
            """)
    Page<AppUser> search(@Param("keyword") String keyword, Pageable pageable);
}
