package com.portfolio.document_processor.service;

import com.portfolio.document_processor.model.DocumentUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {
    DocumentUploadResponse uploadDocument(MultipartFile file);
    DocumentUploadResponse fetchDocument(String documentId);
}
