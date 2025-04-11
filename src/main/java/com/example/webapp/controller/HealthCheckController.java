package com.example.webapp.controller;

import com.example.webapp.model.HealthCheck;
import com.example.webapp.repositry.HealthCheckRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@RestController
public class HealthCheckController {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);

    @Autowired
    private HealthCheckRepository repository;

    @GetMapping("/healthz")
    public ResponseEntity<Void> healthCheck(@RequestBody(required = false) String body, @RequestParam Map<String, String> params) {
        logger.info("Health check endpoint called");

        if (body != null && !body.isEmpty()) {
            logger.warn("Health check called with request body, returning 400");
            return ResponseEntity.badRequest().build(); // 400
        }

        if (!params.isEmpty()) {
            logger.warn("Health check called with query parameters: {}, returning 400", params);
            return ResponseEntity.badRequest().build(); // 400
        }

        try {
            logger.debug("Creating health check record");
            HealthCheck check = new HealthCheck();
            check.setDatetime(LocalDateTime.now(ZoneOffset.UTC));
            repository.save(check);
            logger.info("Health check record created successfully");

            HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");

            logger.info("Health check successful, returning 200 OK");
            return ResponseEntity.ok().headers(headers).build(); // 200
        } catch (DataAccessException e) {
            logger.error("Database access error during health check: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build(); // 503
        } catch (Exception e) {
            logger.error("Unexpected error during health check: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build(); // 503
        }
    }

    @GetMapping("/cicd")
    public ResponseEntity<Void> cicdcheck(@RequestBody(required = false) String body, @RequestParam Map<String, String> params) {
        logger.info("CICD endpoint called");

        if (body != null && !body.isEmpty()) {
            logger.warn("CICD called with request body, returning 400");
            return ResponseEntity.badRequest().build(); // 400
        }

        if (!params.isEmpty()) {
            logger.warn("Health check called with query parameters: {}, returning 400", params);
            return ResponseEntity.badRequest().build(); // 400
        }

        try {
            logger.debug("Creating health check record");
            HealthCheck check = new HealthCheck();
            check.setDatetime(LocalDateTime.now(ZoneOffset.UTC));
            repository.save(check);
            logger.info("Health check record created successfully");

            HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");

            logger.info("Health check successful, returning 200 OK");
            return ResponseEntity.ok().headers(headers).build(); // 200
        } catch (DataAccessException e) {
            logger.error("Database access error during health check: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build(); // 503
        } catch (Exception e) {
            logger.error("Unexpected error during health check: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build(); // 503
        }
    }

    @RequestMapping(
            method = {
                    RequestMethod.HEAD,
                    RequestMethod.OPTIONS,
                    RequestMethod.PUT,
                    RequestMethod.PATCH,
                    RequestMethod.DELETE,
                    RequestMethod.POST
            },
            path = "/healthz"
    )
    public ResponseEntity<Void> notAllowed() {
        logger.warn("Health check called with unsupported HTTP method, returning 405");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build(); // 405
    }
}