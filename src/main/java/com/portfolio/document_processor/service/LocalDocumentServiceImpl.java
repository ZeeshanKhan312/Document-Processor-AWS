package com.portfolio.document_processor.service;

import com.portfolio.document_processor.model.DocumentStatusResponse;
import com.portfolio.document_processor.model.DocumentUploadResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Profile("local") // Tells Spring: Only load this bean if the "local" profile is active
public class LocalDocumentServiceImpl implements DocumentService{
    
    private final String UPLOAD_DIR = "uploads";

    @Override
    public DocumentUploadResponse uploadDocument(MultipartFile file) {
        String documentId= UUID.randomUUID().toString();
        String fileName=file.getOriginalFilename();

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }

            String fileExtension = "";
            if (fileName != null && fileName.contains(".")) {
                fileExtension = fileName.substring(fileName.lastIndexOf("."));
            }

            Path filePath = uploadPath.resolve(documentId + "_" + fileName); // Ensure consistent naming
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
    public DocumentStatusResponse getDocumentStatus(String documentId){
        File directory = new File(UPLOAD_DIR);

        //check if directory exists
        if(directory.exists() && directory.isDirectory()){
            //Search the folder for any file that starts with the documentId and an underscore
            File[] files= directory.listFiles((dir,name)-> name.startsWith(documentId + "_"));

            if(files!=null && files.length>0){
                File foundFile=files[0];

                //Strip the documentID and the underscore from the file name to get the original name
                String originalFileName=foundFile.getName().substring(documentId.length()+1);

                return new DocumentStatusResponse(
                        documentId,
                        originalFileName,
                        "COMPLETED (LOCAL)",
                        String.valueOf(foundFile.length())
                );

            }
        }
        // If the file wasn't found in the local folder
        return new DocumentStatusResponse(documentId,"N/A","NOT FOUND","0");
    }
}
