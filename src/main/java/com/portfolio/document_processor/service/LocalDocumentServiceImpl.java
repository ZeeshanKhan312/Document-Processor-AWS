package com.portfolio.document_processor.service;

import com.portfolio.document_processor.model.DocumentUploadResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@Profile("local") // Tells Spring: Only load this bean if the "local" profile is active
public class LocalDocumentServiceImpl implements DocumentService{
    @Override
    public DocumentUploadResponse uploadDocument(MultipartFile file) {
        String documentId= UUID.randomUUID().toString();
        String fileName=file.getOriginalFilename();

        // 2. TODO: Write code here to save the file locally
        try {
            java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads");
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }

            String fileExtension = "";
            if (fileName != null && fileName.contains(".")) {
                fileExtension = fileName.substring(fileName.lastIndexOf("."));
            }

            java.nio.file.Path filePath = uploadPath.resolve(documentId + fileExtension);
            java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (java.io.IOException e) {
            return new DocumentUploadResponse(
                    documentId,
                    fileName,
                    "FAILED",
                    "Could not store the file locally: " + e.getMessage()
            );
        }


        return new DocumentUploadResponse(
                documentId,
                fileName,
                "PENDING",
                "File Validated and queued for local storage"
        );
    }

    @Override
    public DocumentUploadResponse fetchDocument(String documentId) {
        return null;
    }
}
