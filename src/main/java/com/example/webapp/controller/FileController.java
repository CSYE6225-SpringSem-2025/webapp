package com.example.webapp.controller;

import com.example.webapp.model.File;
import com.example.webapp.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/v1")
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileService fileService;

    @PostMapping("/file")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("profilePic") MultipartFile file,
            HttpServletRequest request
    ) {
        logger.info("POST /v1/file - Received file upload request");
        try {
            if (file == null || file.isEmpty()) {
                logger.warn("POST /v1/file - File is empty or not provided");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "File is empty or not provided"));
            }

            File savedFile = fileService.uploadFile(file);
            logger.info("POST /v1/file - File uploaded successfully with ID: {}", savedFile.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("file_name", savedFile.getFileName());
            response.put("id", savedFile.getId());
            response.put("url", savedFile.getUrl());
            response.put("upload_date", savedFile.getUploadDate().toString());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("POST /v1/file - File upload failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "File upload failed", "details", e.getMessage()));
        }
    }

    @GetMapping("/file/{id}")
    public ResponseEntity<Map<String, Object>> getFile(@PathVariable String id) {
        logger.info("GET /v1/file/{} - Retrieving file information", id);
        Optional<File> fileOpt = fileService.getFile(id);

        if (fileOpt.isPresent()) {
            File file = fileOpt.get();
            logger.info("GET /v1/file/{} - File found and returning information", id);

            Map<String, Object> response = new HashMap<>();
            response.put("file_name", file.getFileName());
            response.put("id", file.getId());
            response.put("url", file.getUrl());
            response.put("upload_date", file.getUploadDate().toString());

            return ResponseEntity.ok(response);
        } else {
            logger.warn("GET /v1/file/{} - File not found", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/file")
    public ResponseEntity<Map<String, Object>> getFileWithoutId() {
        logger.warn("GET /v1/file - Request received without file ID");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "File ID is required"));
    }

    @DeleteMapping("/file/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable String id) {
        logger.info("DELETE /v1/file/{} - Attempting to delete file", id);
        try {
            fileService.deleteFile(id);
            logger.info("DELETE /v1/file/{} - File deleted successfully", id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            if (e.getMessage().equals("File not found")) {
                logger.warn("DELETE /v1/file/{} - File not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else {
                logger.error("DELETE /v1/file/{} - Internal server error: {}", id, e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

    @DeleteMapping("/file")
    public ResponseEntity<Map<String, Object>> deleteFileWithoutId() {
        logger.warn("DELETE /v1/file - Request received without file ID");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "File ID is required"));
    }

    @PutMapping("/file/{id}")
    public ResponseEntity<Map<String, Object>> putFileWithId(@PathVariable String id) {
        logger.warn("PUT /v1/file/{} - Method not allowed", id);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Map.of("error", "Method not allowed"));
    }

    @PutMapping("/file")
    public ResponseEntity<Map<String, Object>> putFileWithoutId() {
        logger.warn("PUT /v1/file - Method not allowed");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Map.of("error", "Method not allowed"));
    }

    @PatchMapping("/file/{id}")
    public ResponseEntity<Map<String, Object>> patchFileWithId(@PathVariable String id) {
        logger.warn("PATCH /v1/file/{} - Method not allowed", id);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Map.of("error", "Method not allowed"));
    }

    @PatchMapping("/file")
    public ResponseEntity<Map<String, Object>> patchFileWithoutId() {
        logger.warn("PATCH /v1/file - Method not allowed");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Map.of("error", "Method not allowed"));
    }

    @RequestMapping(value = "/file/{id}", method = RequestMethod.OPTIONS)
    public ResponseEntity<Map<String, Object>> optionsFileWithId(@PathVariable String id) {
        logger.warn("OPTIONS /v1/file/{} - Method not allowed", id);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Map.of("error", "Method not allowed"));
    }

    @RequestMapping(value = "/file", method = RequestMethod.OPTIONS)
    public ResponseEntity<Map<String, Object>> optionsFileWithoutId() {
        logger.warn("OPTIONS /v1/file - Method not allowed");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Map.of("error", "Method not allowed"));
    }

    @RequestMapping(value = "/file/{id}", method = RequestMethod.HEAD)
    public ResponseEntity<Map<String, Object>> headFileWithId(@PathVariable String id) {
        logger.warn("HEAD /v1/file/{} - Method not allowed", id);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Map.of("error", "Method not allowed"));
    }

    @RequestMapping(value = "/file", method = RequestMethod.HEAD)
    public ResponseEntity<Map<String, Object>> headFileWithoutId() {
        logger.warn("HEAD /v1/file - Method not allowed");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Map.of("error", "Method not allowed"));
    }

    @GetMapping("/file/test")
    public ResponseEntity<String> testFileEndpoint() {
        logger.info("GET /v1/file/test - Testing file endpoint");
        return ResponseEntity.ok("File endpoint is working!");
    }
}