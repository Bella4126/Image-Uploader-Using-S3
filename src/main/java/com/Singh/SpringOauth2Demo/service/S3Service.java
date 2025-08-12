package com.Singh.SpringOauth2Demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3Service {

    @Autowired
    private S3Client s3Client;

    @Value("${app.s3.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile file) {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return "File uploaded successfully: " + fileName;
        } catch (S3Exception e) {
            throw new RuntimeException("S3 Error: " + e.awsErrorDetails().errorMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    public byte[] downloadFile(String fileName) {
        try {
            return s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build()).readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from S3", e);
        }
    }

    public String deleteFile(String fileName) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build());
            return "File deleted successfully: " + fileName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    }

    public List<String> listFiles() {
        try {
            return s3Client.listObjectsV2(ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build())
                    .contents()
                    .stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to list files from S3", e);
        }
    }

    public String getFileUrl(String fileName) {
        return "https://" + bucketName + ".s3." +
                s3Client.serviceClientConfiguration().region().id() +
                ".amazonaws.com/" + fileName;
    }
}
