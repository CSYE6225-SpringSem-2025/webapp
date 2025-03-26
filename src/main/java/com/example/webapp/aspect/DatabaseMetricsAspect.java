package com.example.webapp.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class DatabaseMetricsAspect {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMetricsAspect.class);

    @Autowired
    private MeterRegistry meterRegistry;

    @Pointcut("execution(* com.example.webapp.repositry.*.*(..))")
    public void repositoryMethods() {}

    @Around("repositoryMethods()")
    public Object measureDatabaseTiming(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String metricName = "database.query." + className + "." + methodName;

        logger.info("Database operation: {} - {}", className, methodName);

        // Time the database operation
        long startTime = System.currentTimeMillis();
        Object result;
        try {
            result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            // Record timing
            Timer.builder(metricName)
                    .tags("class", className, "method", methodName)
                    .register(meterRegistry)
                    .record(executionTime, TimeUnit.MILLISECONDS);

            logger.info("Database operation: {} - {} completed in {} ms", className, methodName, executionTime);
            return result;
        } catch (Exception e) {
            meterRegistry.counter("database.errors", "class", className, "method", methodName).increment();
            logger.error("Database operation: {} - {} failed: {}", className, methodName, e.getMessage(), e);
            throw e;
        }
    }
}