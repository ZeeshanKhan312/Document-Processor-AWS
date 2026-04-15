package com.portfolio.document_processor.service;

import com.portfolio.document_processor.model.DocumentStatusResponse;
import com.portfolio.document_processor.model.DocumentUploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
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

    private final String TABLE_NAME = "DocumentMetadata";

    private final S3Client s3Client;
    private final DynamoDbClient dynamoDbClient;

    // Constructor injection (Best practice for Spring Boot dependencies)
    public S3DocumentServiceImpl(S3Client s3Client, DynamoDbClient dynamoDbClient) {
        this.s3Client = s3Client;
        this.dynamoDbClient = dynamoDbClient;
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
    public DocumentStatusResponse getDocumentStatus(String documentId) {
        try {
            // 1. Build the DynamoDB GetItem request
            software.amazon.awssdk.services.dynamodb.model.GetItemRequest request = software.amazon.awssdk.services.dynamodb.model.GetItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .key(java.util.Map.of("documentId", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().s(documentId).build()))
                    .build();
            // 2. Fetch the item
            software.amazon.awssdk.services.dynamodb.model.GetItemResponse response = dynamoDbClient.getItem(request);
            // 3. Check if the item exists in the database
            if (!response.hasItem()) {
                return new DocumentStatusResponse(documentId, "N/A", "PROCESSING (Or Not Found)", "0","N/A","0.00");
            }
            // 4. Map the DynamoDB attributes to our Java object
            java.util.Map<String, software.amazon.awssdk.services.dynamodb.model.AttributeValue> item = response.item();
            return new DocumentStatusResponse(
                    item.get("documentId").s(),
                    item.get("fileName").s(),
                    item.get("status").s(),
                    item.get("fileSizeBytes").n(),// .n() because it's stored as a number
                    item.get("fileType").s(),
                    item.get("processedAt").s()
            );

        } catch (Exception e) {
            e.printStackTrace();
            return new DocumentStatusResponse(documentId, "ERROR", "Failed to retrieve status", "0","N/A","0.00");
        }
    }
}
