


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
    
    // Upload file to S3
    public String uploadFile(MultipartFile file) {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        
        try {
            // Check if bucket exists and is accessible
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3Client.headBucket(headBucketRequest);
            
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
    
    // Download file from S3
    public byte[] downloadFile(String fileName) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
            
            return s3Client.getObject(getObjectRequest).readAllBytes();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from S3", e);
        }
    }
    
    // Delete file from S3
    public String deleteFile(String fileName) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
            
            s3Client.deleteObject(deleteObjectRequest);
            return "File deleted successfully: " + fileName;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    }
    
    // List all files in the bucket
    public List<String> listFiles() {
        try {
            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();
            
            ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);
            
            return listObjectsResponse.contents().stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            throw new RuntimeException("Failed to list files from S3", e);
        }
    }
    
    // Generate presigned URL for file access
    public String getFileUrl(String fileName) {
        return "https://" + bucketName + ".s3." + 
               s3Client.serviceClientConfiguration().region().id() + 
               ".amazonaws.com/" + fileName;
    }
}