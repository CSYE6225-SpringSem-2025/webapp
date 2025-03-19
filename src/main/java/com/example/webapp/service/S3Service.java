package com.example.webapp.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    @Autowired
    private AmazonS3 amazonS3Client;

    @Value("${S3_BUCKET:}")
    private String bucketName;

    public String uploadFile(MultipartFile file) throws IOException {
        if (bucketName == null || bucketName.isEmpty()) {
            logger.warn("No S3 bucket configured. Skipping S3 upload.");
            return generateLocalUrl(file.getOriginalFilename());
        }

        try {
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            PutObjectRequest request = new PutObjectRequest(
                    bucketName,
                    fileName,
                    file.getInputStream(),
                    metadata
            );

            amazonS3Client.putObject(request);
            return amazonS3Client.getUrl(bucketName, fileName).toString();
        } catch (Exception e) {
            logger.error("Error uploading to S3", e);
            return generateLocalUrl(file.getOriginalFilename());
        }
    }

    public void deleteFile(String fileUrl) {
        if (bucketName == null || bucketName.isEmpty()) {
            logger.warn("No S3 bucket configured. Skipping S3 delete.");
            return;
        }

        try {
            String key = extractKeyFromUrl(fileUrl);
            amazonS3Client.deleteObject(bucketName, key);
        } catch (Exception e) {
            logger.error("Error deleting from S3", e);
        }
    }

    private String generateUniqueFileName(String originalFilename) {
        return UUID.randomUUID() + "_" + originalFilename;
    }

    private String generateLocalUrl(String originalFilename) {
        return "local://" + UUID.randomUUID() + "_" + originalFilename;
    }

    private String extractKeyFromUrl(String fileUrl) {
        return fileUrl.substring(fileUrl.indexOf(bucketName) + bucketName.length() + 1);
    }
}