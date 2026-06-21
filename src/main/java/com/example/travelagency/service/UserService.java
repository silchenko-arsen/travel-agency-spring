package com.example.travelagency.service;

import com.example.travelagency.domain.AppUser;
import com.example.travelagency.dto.user.BalanceTopUpRequest;
import com.example.travelagency.dto.user.UserUpdateRequest;
import com.example.travelagency.exception.BusinessException;
import com.example.travelagency.exception.NotFoundException;
import com.example.travelagency.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AppUser getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional
    public AppUser updateProfile(String email, UserUpdateRequest request) {
        AppUser user = getByEmail(email);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        log.info("User profile updated: {}", email);
        return user;
    }

    @Transactional
    public AppUser topUpBalance(String email, BalanceTopUpRequest request) {
        AppUser user = getByEmail(email);
        BigDecimal currentBalance = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        user.setBalance(currentBalance.add(request.getAmount()));
        log.info("User balance topped up: {} amount={}", email, request.getAmount());
        return user;
    }

    @Transactional(readOnly = true)
    public Page<AppUser> searchUsers(String keyword, Pageable pageable) {
        return userRepository.search(keyword, pageable);
    }

    @Transactional
    public void setBlocked(Long userId, boolean blocked) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getRole().name().equals("ROLE_ADMIN")) {
            throw new BusinessException("Admin cannot be blocked");
        }
        user.setBlocked(blocked);
        log.warn("User {} blocked status changed to {}", user.getEmail(), blocked);
    }

    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        AppUser user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("auth.error.userNotFound"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("profile.error.oldPasswordInvalid");
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }
}
