package com.portfolio.document_processor.service;

import com.portfolio.document_processor.model.DocumentUploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

// Tells Spring to manage this as a singleton bean
@Service
// Tells Spring: Only load this bean if the "aws" profile is active
@Profile("aws")
public class S3DocumentServiceImpl implements DocumentService{

    // Injects the 'aws.s3.bucket.name' property value from the application configuration
    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    private final S3Client s3Client;

    // Constructor injection (Best practice for Spring Boot dependencies)
    public S3DocumentServiceImpl(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public DocumentUploadResponse uploadDocument(MultipartFile file) {
        String documentId= UUID.randomUUID().toString();
        String fileName=file.getOriginalFilename();
        String objectKey = documentId + "_" + fileName;


        try{
            // 1. Build the request telling AWS where to put the file
            PutObjectRequest putObjectRequest= PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build();
            // 2. Stream the file directly to S3
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(),file.getSize()));

            return new DocumentUploadResponse(
                    documentId,
                    fileName,
                    "SUCCESS",
                    "File successfully uploaded to S3 bucket: " + bucketName + " with key: " + objectKey
            );
        } catch (IOException e) {
            e.printStackTrace();
            return new DocumentUploadResponse(
                    documentId,
                    fileName,
                    "FAILED",
                    "Failed to read file for S3 upload: " + e.getMessage()
            );
        }catch (Exception e) {
            e.printStackTrace();
            return new DocumentUploadResponse(
                    documentId,
                    fileName,
                    "FAILED",
                    "AWS S3 Upload Error: " + e.getMessage()
            );
        }

    }

    @Override
    public DocumentUploadResponse fetchDocument(String documentId) {
        return null;
    }
}
