package com.example.webapp.controller;

import com.example.webapp.model.File;
import com.example.webapp.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
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

    @Autowired
    private FileService fileService;

    @PostMapping("/file")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("profilePic") MultipartFile file,
            HttpServletRequest request
    ) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "File is empty or not provided"));
            }

            File savedFile = fileService.uploadFile(file);

            Map<String, Object> response = new HashMap<>();
            response.put("file_name", savedFile.getFileName());
            response.put("id", savedFile.getId());
            response.put("url", savedFile.getUrl());
            response.put("upload_date", savedFile.getUploadDate().toString());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "File upload failed", "details", e.getMessage()));
        }
    }

    @GetMapping("/file/{id}")
    public ResponseEntity<Map<String, Object>> getFile(@PathVariable String id) {
        Optional<File> fileOpt = fileService.getFile(id);

        if (fileOpt.isPresent()) {
            File file = fileOpt.get();

            Map<String, Object> response = new HashMap<>();
            response.put("file_name", file.getFileName());
            response.put("id", file.getId());
            response.put("url", file.getUrl());
            response.put("upload_date", file.getUploadDate().toString());

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/file/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable String id) {
        try {
            fileService.deleteFile(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            if (e.getMessage().equals("File not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

    @GetMapping("/file/test")
    public ResponseEntity<String> testFileEndpoint() {
        return ResponseEntity.ok("File endpoint is working!");
    }
}