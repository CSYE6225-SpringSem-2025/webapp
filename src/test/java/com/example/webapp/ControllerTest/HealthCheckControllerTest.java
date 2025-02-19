package com.example.webapp.ControllerTest;

import com.example.webapp.model.HealthCheck;
import com.example.webapp.repositry.HealthCheckRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static io.restassured.RestAssured.*;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HealthCheckControllerTest {

    @LocalServerPort
    private int port; //brings me the localport in use

    @MockitoBean
    private HealthCheckRepository healthCheckRepository;

    @BeforeEach
    public void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    public void testSuccessfulHealthCheck() {
        given()
                .when()
                .get("/healthz")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void testHealthCheckWithBody() {
        given()
                .body("Unexpected body content")
                .when()
                .get("/healthz")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void testNotAllowedMethods() {
        // Test POST method
        given()
                .when()
                .post("/healthz")
                .then()
                .statusCode(HttpStatus.METHOD_NOT_ALLOWED.value());

        given()
                .when()
                .put("/healthz")
                .then()
                .statusCode(HttpStatus.METHOD_NOT_ALLOWED.value());

        given()
                .when()
                .delete("/healthz")
                .then()
                .statusCode(HttpStatus.METHOD_NOT_ALLOWED.value());

        given()
                .when()
                .patch("/healthz")
                .then()
                .statusCode(HttpStatus.METHOD_NOT_ALLOWED.value());

        given()
                .when()
                .head("/healthz")
                .then()
                .statusCode(HttpStatus.METHOD_NOT_ALLOWED.value());

        given()
                .when()
                .options("/healthz")
                .then()
                .statusCode(HttpStatus.METHOD_NOT_ALLOWED.value());
    }
    // request and params should be null
    @Test
    public void testtocheckrequestparameters() {
        given()
                .queryParam("saurabh", "saurabh")
                .when()
                .get("/healthz")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }
    @Test
    public void testDatabaseFailureScenario() {
        doThrow(new DataAccessException("Simulated database failure") {})
                .when(healthCheckRepository).save(Mockito.any(HealthCheck.class));

        given()
                .when()
                .get("/healthz")
                .then()
                .statusCode(HttpStatus.SERVICE_UNAVAILABLE.value()); // Assert 503 status code

    }

}
