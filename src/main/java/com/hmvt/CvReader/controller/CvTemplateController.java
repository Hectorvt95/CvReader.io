package com.hmvt.CvReader.controller;

import com.hmvt.CvReader.model.CvTemplate;
import com.hmvt.CvReader.service.CvTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * CvTemplateController - REST API Layer
 * 
 * This controller exposes HTTP endpoints that the frontend can call.
 * 
 * - GET /api/templates/all → "Give me all templates"
 * - GET /api/templates/download-all → "Download all templates as ZIP"
 */
@RestController  // Tells Spring this returns JSON/data 
@RequestMapping("/api/templates")  // All endpoints start with /api/templates
@CrossOrigin(origins = "*")  // Allow frontend to call these APIs
public class CvTemplateController {
    
    @Autowired
    private CvTemplateService cvTemplateService;
    
    /**
     * GET /api/templates/download-all
     * 
     * Downloads all CV templates as a single ZIP file
     * 
     * Used by user when clicks "Download CV Templates" button
     * 
     */
    @GetMapping("/download-all")
    public ResponseEntity<byte[]> downloadAllTemplates() {
        try {
            System.out.println("API Request: Download all templates as ZIP");
            
            // Get all active templates from database
            List<CvTemplate> templates = cvTemplateService.getAllTemplates();
            
            if (templates.isEmpty()) {
                System.out.println("No templates found in database");
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("Found " + templates.size() + " templates to include in ZIP");
            
            // Create ZIP file containing all templates
            byte[] zipData = createZipFromTemplates(templates);
            
            // Set response headers for ZIP download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/zip"));
            headers.setContentDispositionFormData("attachment", "CvReader-Templates.zip");
            headers.setContentLength(zipData.length);
            
            System.out.println("Serving ZIP file (" + (zipData.length / 1024) + " KB) with " + 
                             templates.size() + " CV templates");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(zipData);
                    
        } catch (Exception e) {
            System.err.println("Error creating CV templates ZIP file: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    
    /**
     * This method takes all templates into a zip file and each template becomes a file in the ZIP with its original filename.
     */
    private byte[] createZipFromTemplates(List<CvTemplate> templates) throws IOException {
        // Create output stream to build ZIP 
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (ZipOutputStream zipOut = new ZipOutputStream(baos)) {
            
            // Add each template to the ZIP
            for (CvTemplate template : templates) {
                try {
                    // Create ZIP entry for each template
                    ZipEntry zipEntry = new ZipEntry(template.getOriginalFileName());
                    zipOut.putNextEntry(zipEntry);
                    
                    // Write template file data to ZIP
                    zipOut.write(template.getFileData());
                    zipOut.closeEntry();
                    
                    System.out.println("Added to ZIP: " + template.getOriginalFileName() + " (" + template.getFileSizeFormatted() + ")");
                    
                } catch (Exception e) {
                    System.out.println("Error adding template to ZIP: " + template.getOriginalFileName() + 
                                     " - " + e.getMessage());
                    // Continue with other templates even if one fails
                }
            }
            
            zipOut.finish();  // Finalize the ZIP file
        }
        
        return baos.toByteArray();  // Return ZIP as byte array
    }
 
}

/*
 * CONTROLLER LAYER RESPONSIBILITIES:
 * 
 * 1. HTTP REQUEST HANDLING
 *    - Map URL paths to Java methods (@GetMapping, @PostMapping)
 *    - Extract parameters from URLs (@PathVariable, @RequestParam)
 *    - Handle different HTTP methods (GET for retrieval, POST for uploads)
 * 
 * 2. INPUT VALIDATION
 *    - Validate request parameters
 *    - Handle missing or invalid data
 *    - Return appropriate HTTP status codes
 * 
 * 3. RESPONSE FORMATTING
 *    - Convert Java objects to JSON (for API responses)
 *    - Set proper HTTP headers (Content-Type, Content-Disposition)
 *    - Handle file downloads with correct MIME types
 * 
 * 4. ERROR HANDLING
 *    - Catch exceptions and return proper error responses
 *    - Log errors for debugging
 *    - Provide meaningful error messages to frontend
 * 
 * 5. DELEGATION
 *    - Don't contain business logic (that's in the service)
 *    - Call service methods and return results
 *    - Act as a "bridge" between HTTP and business logic
 * 
 * ENDPOINT SUMMARY:
 * 
 * GET /api/templates/all
 * ├── Returns: List of template metadata (JSON)
 * ├── Used by: Frontend to show template cards
 * └── Example: [{"id":1, "templateName":"Modern CV", "fileType":"PDF"}]
 * 
 * GET /api/templates/download/{id}
 * ├── Returns: Actual file bytes with download headers
 * ├── Used by: User clicking "Download" on a template
 * └── Example: Downloads "modern_cv.pdf" file
 * 
 * GET /api/templates/download-all
 * ├── Returns: ZIP file with all templates
 * ├── Used by: User clicking "Download CV Templates" button
 * └── Example: Downloads "CvReader-Templates.zip"
 * 
 * POST /api/templates/admin/upload
 * ├── Accepts: MultipartFile (ZIP/RAR archive)
 * ├── Used by: Admin uploading new templates
 * └── Example: Upload new-templates.zip
 * 
 * GET /api/templates/count
 * ├── Returns: Number of available templates
 * ├── Used by: Statistics or admin dashboards
 * └── Example: Returns 5
 * 
 * GET /api/templates/health
 * ├── Returns: Service status message
 * ├── Used by: Health checks and monitoring
 * └── Example: "CV Template service is running ✅ (5 templates available)"
 * 
 * The controller is the "front door" of your API - it handles all 
 * communication between the frontend and your backend services!
 */