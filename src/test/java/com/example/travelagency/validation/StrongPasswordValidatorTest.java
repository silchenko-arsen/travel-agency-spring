package com.example.travelagency.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StrongPasswordValidatorTest {

    private final StrongPasswordValidator validator = new StrongPasswordValidator();

    @Test
    void isValid_whenPasswordIsStrong_shouldReturnTrue() {
        assertThat(validator.isValid("Qwerty1!", null)).isTrue();
    }

    @Test
    void isValid_whenPasswordHasNoUppercase_shouldReturnFalse() {
        assertThat(validator.isValid("qwerty1!", null)).isFalse();
    }

    @Test
    void isValid_whenPasswordHasNoLowercase_shouldReturnFalse() {
        assertThat(validator.isValid("QWERTY1!", null)).isFalse();
    }

    @Test
    void isValid_whenPasswordHasNoDigit_shouldReturnFalse() {
        assertThat(validator.isValid("Qwertyyy!", null)).isFalse();
    }

    @Test
    void isValid_whenPasswordHasNoSpecialCharacter_shouldReturnFalse() {
        assertThat(validator.isValid("Qwerty123", null)).isFalse();
    }

    @Test
    void isValid_whenPasswordIsTooShort_shouldReturnFalse() {
        assertThat(validator.isValid("Qw1!", null)).isFalse();
    }
}