package com.example.travelagency.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StrongPasswordValidatorTest {

    private StrongPasswordValidator validator;

    @BeforeEach
    void setUp() {
        validator = new StrongPasswordValidator();
    }

    @Test
    void isValid_whenValueIsNull_shouldReturnTrue() {
        boolean result = validator.isValid(null, null);

        assertThat(result).isTrue();
    }

    @Test
    void isValid_whenValueIsBlank_shouldReturnTrue() {
        boolean result = validator.isValid("   ", null);

        assertThat(result).isTrue();
    }

    @Test
    void isValid_whenPasswordIsValid_shouldReturnTrue() {
        boolean result = validator.isValid("Qwerty123!", null);

        assertThat(result).isTrue();
    }

    @Test
    void isValid_whenPasswordHasNoLowercase_shouldReturnFalse() {
        boolean result = validator.isValid("QWERTY123!", null);

        assertThat(result).isFalse();
    }

    @Test
    void isValid_whenPasswordHasNoUppercase_shouldReturnFalse() {
        boolean result = validator.isValid("qwerty123!", null);

        assertThat(result).isFalse();
    }

    @Test
    void isValid_whenPasswordHasNoDigit_shouldReturnFalse() {
        boolean result = validator.isValid("Qwertyyy!", null);

        assertThat(result).isFalse();
    }

    @Test
    void isValid_whenPasswordHasNoSpecialCharacter_shouldReturnFalse() {
        boolean result = validator.isValid("Qwerty123", null);

        assertThat(result).isFalse();
    }

    @Test
    void isValid_whenPasswordIsTooShort_shouldReturnFalse() {
        boolean result = validator.isValid("Qwer1!", null);

        assertThat(result).isFalse();
    }
}