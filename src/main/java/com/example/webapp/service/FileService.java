package com.example.webapp.service;

import com.example.webapp.model.File;
import com.example.webapp.repositry.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private S3Service s3Service;

    @Value("${S3_BUCKET:}")
    private String s3Bucket;

    public File uploadFile(MultipartFile multipartFile) throws IOException {
        logger.info("Attempting to upload file: {}", multipartFile.getOriginalFilename());
        logger.info("S3 Bucket configured: {}", s3Bucket);

        String url = s3Service.uploadFile(multipartFile);

        // Create file entity
        File file = new File();
        file.setFileName(multipartFile.getOriginalFilename());
        file.setOriginalFileName(multipartFile.getOriginalFilename());
        file.setUrl(url);
        file.setUploadDate(LocalDate.now());

        // Save to database
        File savedFile = fileRepository.save(file);
        logger.info("File saved with ID: {}", savedFile.getId());
        return savedFile;
    }

    public Optional<File> getFile(String id) {
        return fileRepository.findById(id);
    }

    public void deleteFile(String id) throws Exception {
        Optional<File> fileOpt = fileRepository.findById(id);

        if (fileOpt.isPresent()) {
            File file = fileOpt.get();

            // Delete from S3
            s3Service.deleteFile(file.getUrl());

            // Delete from database
            fileRepository.delete(file);
        } else {
            throw new Exception("File not found");
        }
    }
}