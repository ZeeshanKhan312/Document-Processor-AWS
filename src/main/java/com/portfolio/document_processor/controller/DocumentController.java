package com.portfolio.document_processor.controller;

import com.portfolio.document_processor.model.DocumentStatusResponse;
import com.portfolio.document_processor.model.DocumentUploadResponse;
import com.portfolio.document_processor.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    // Constructor injection (Best practice for Spring Boot dependencies)
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponse> uploadDocument(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new DocumentUploadResponse(null, null, "FAILED", "File is empty.")
            );
        }

        DocumentUploadResponse response = documentService.uploadDocument(file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{documentId}/status")
    public ResponseEntity<DocumentStatusResponse> getDocumentStatus(@PathVariable String documentId) {
        DocumentStatusResponse response = documentService.getDocumentStatus(documentId);
        return ResponseEntity.ok(response);
    }
}