package com.example.travelagency.service;

import com.example.travelagency.domain.AppUser;
import com.example.travelagency.dto.user.BalanceTopUpRequest;
import com.example.travelagency.dto.user.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    AppUser getByEmail(String email);

    AppUser updateProfile(String email, UserUpdateRequest request);

    AppUser topUpBalance(String email, BalanceTopUpRequest request);

    Page<AppUser> searchUsers(String keyword, Pageable pageable);

    void setBlocked(Long userId, boolean blocked);

    void changePassword(String email, String oldPassword, String newPassword);
}