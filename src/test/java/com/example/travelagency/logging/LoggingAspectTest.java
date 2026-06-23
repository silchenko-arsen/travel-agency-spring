package com.example.travelagency.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class LoggingAspectTest {

    private final LoggingAspect loggingAspect = new LoggingAspect();

    @Test
    void logServiceMethods_whenMethodReturnsResult_shouldReturnSameResult() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("UserService.getByEmail()");
        when(joinPoint.proceed()).thenReturn("result");

        Object result = loggingAspect.logServiceMethods(joinPoint);

        assertThat(result).isEqualTo("result");

        verify(joinPoint).proceed();
    }

    @Test
    void logServiceMethods_whenMethodThrowsException_shouldRethrowSameException() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        RuntimeException exception = new RuntimeException("boom");

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("UserService.getByEmail()");
        when(joinPoint.proceed()).thenThrow(exception);

        assertThatThrownBy(() -> loggingAspect.logServiceMethods(joinPoint))
                .isSameAs(exception);

        verify(joinPoint).proceed();
    }
}