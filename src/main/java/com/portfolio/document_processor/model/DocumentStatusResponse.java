package com.portfolio.document_processor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentStatusResponse {
    private String documentId;
    private String fileName;
    private String status;
    private String fileSizeBytes;
//    private String fileContent;
}
