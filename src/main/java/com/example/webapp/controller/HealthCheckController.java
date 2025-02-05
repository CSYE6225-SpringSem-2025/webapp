package com.example.webapp.controller;

import com.example.webapp.model.HealthCheck;
import com.example.webapp.repositry.HealthCheckRepository;
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

    @Autowired
    private HealthCheckRepository repository;

    @GetMapping("/healthz")
    public ResponseEntity<Void> healthCheck(@RequestBody(required = false) String body , @RequestParam Map<String, String> params) {
        if (body != null && !body.isEmpty()) {
            return ResponseEntity.badRequest().build(); //400
        }
        if (!params.isEmpty()) {
            return ResponseEntity.badRequest().build(); //400
        }

        try {
            HealthCheck check = new HealthCheck();
            check.setDatetime(LocalDateTime.now(ZoneOffset.UTC));
            repository.save(check);

            HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");

            return ResponseEntity.ok().headers(headers).build(); //200
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
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
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    } //405
}
