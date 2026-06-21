package com.example.travelagency.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(public * com.example.travelagency.service..*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String method = joinPoint.getSignature().toShortString();
        log.debug("Start {}", method);
        try {
            Object result = joinPoint.proceed();
            log.debug("Finish {} in {} ms", method, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable ex) {
            log.error("Error in {}: {}", method, ex.getMessage());
            throw ex;
        }
    }
}
