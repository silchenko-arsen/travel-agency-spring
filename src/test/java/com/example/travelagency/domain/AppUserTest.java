package com.example.travelagency.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AppUserTest {

    @Test
    void prePersist_shouldSetCreatedAtUpdatedAtDefaultRoleAndDefaultBalance() {
        AppUser user = new AppUser();

        user.prePersist();

        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
        assertThat(user.getRole()).isEqualTo(Role.ROLE_USER);
        assertThat(user.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void prePersist_whenRoleAndBalanceAlreadySet_shouldNotOverrideThem() {
        AppUser user = new AppUser();
        user.setRole(Role.ROLE_ADMIN);
        user.setBalance(new BigDecimal("500"));

        user.prePersist();

        assertThat(user.getRole()).isEqualTo(Role.ROLE_ADMIN);
        assertThat(user.getBalance()).isEqualByComparingTo("500");
    }

    @Test
    void preUpdate_shouldChangeUpdatedAt() {
        AppUser user = new AppUser();
        user.prePersist();

        LocalDateTime oldUpdatedAt = user.getUpdatedAt();

        user.preUpdate();

        assertThat(user.getUpdatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isAfterOrEqualTo(oldUpdatedAt);
    }
}