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
public class S3MetricsAspect {

    private static final Logger logger = LoggerFactory.getLogger(S3MetricsAspect.class);

    @Autowired
    private MeterRegistry meterRegistry;

    @Pointcut("execution(* com.example.webapp.service.S3Service.*(..))")
    public void s3Methods() {}

    @Around("s3Methods()")
    public Object measureS3Timing(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String metricName = "s3.operation." + methodName;

        logger.info("S3 operation: {}", methodName);

        // Time the S3 operation
        long startTime = System.currentTimeMillis();
        Object result;
        try {
            result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            // Record timing
            Timer.builder(metricName)
                    .tags("operation", methodName)
                    .register(meterRegistry)
                    .record(executionTime, TimeUnit.MILLISECONDS);

            logger.info("S3 operation: {} completed in {} ms", methodName, executionTime);
            return result;
        } catch (Exception e) {
            meterRegistry.counter("s3.errors", "operation", methodName).increment();
            logger.error("S3 operation: {} failed: {}", methodName, e.getMessage(), e);
            throw e;
        }
    }
}