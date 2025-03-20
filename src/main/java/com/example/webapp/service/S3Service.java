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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

@Service
public class S3Service {
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    @Autowired
    private AmazonS3 amazonS3Client;

    @Value("${S3_BUCKET}")
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
            logger.info("About to delete S3 object with key: {}", key);
            amazonS3Client.deleteObject(bucketName, key);
            logger.info("S3 deletion API call completed for key: {}", key);

            // Verify deletion
            boolean exists = amazonS3Client.doesObjectExist(bucketName, key);
            logger.info("Object still exists after deletion? {}", exists);
        } catch (Exception e) {
            logger.error("Error in S3 deletion: {}", e.getMessage(), e);
        }
    }

    private String generateUniqueFileName(String originalFilename) {
        return UUID.randomUUID() + "_" + originalFilename;
    }

    private String generateLocalUrl(String originalFilename) {
        return "local://" + UUID.randomUUID() + "_" + originalFilename;
    }

    private String extractKeyFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();

            // Remove leading slash
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            logger.info("Extracted path from URL: " + path);
            return path;
        } catch (MalformedURLException e) {
            logger.error("Failed to parse URL: " + fileUrl, e);

            // Fallback extraction method if URL parsing fails
            int lastSlashIndex = fileUrl.lastIndexOf('/');
            if (lastSlashIndex != -1) {
                return fileUrl.substring(lastSlashIndex + 1);
            }

            return fileUrl;
        }
    }
}