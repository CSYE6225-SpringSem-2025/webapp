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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class MetricsAspect {

    private static final Logger logger = LoggerFactory.getLogger(MetricsAspect.class);

    @Autowired
    private MeterRegistry meterRegistry;

    @Pointcut("execution(* com.example.webapp.controller.*.*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object measureApiTiming(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String methodName = joinPoint.getSignature().getName();
        String uri = request.getRequestURI();
        String httpMethod = request.getMethod();

        String metricName = "api.request." + httpMethod.toLowerCase() + "." + methodName;
        String logMsg = String.format("%s %s called", httpMethod, uri);
        logger.info(logMsg);

        // Increment counter for API call
        meterRegistry.counter("api.calls", "method", httpMethod, "uri", uri).increment();

        // Time the API call
        long startTime = System.currentTimeMillis();
        Object result;
        try {
            result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            // Record timing
            Timer.builder(metricName)
                    .tags("uri", uri, "method", httpMethod)
                    .register(meterRegistry)
                    .record(executionTime, TimeUnit.MILLISECONDS);

            logger.info("{} {} completed in {} ms", httpMethod, uri, executionTime);
            return result;
        } catch (Exception e) {
            meterRegistry.counter("api.errors", "method", httpMethod, "uri", uri).increment();
            logger.error("{} {} failed: {}", httpMethod, uri, e.getMessage(), e);
            throw e;
        }
    }
}