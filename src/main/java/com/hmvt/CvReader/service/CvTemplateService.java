/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hmvt.CvReader.service;

import com.hmvt.CvReader.model.CvTemplate;
import com.hmvt.CvReader.repository.CvTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author marti
 */

/**
 * CvTemplateService - Business Logic Layer

 // AUTO-UPLOAD
 */

@Service
public class CvTemplateService {
    
    @Autowired 
    private CvTemplateRepository cvTemplateRepository;
    
    /**
    * Upload CV templates from archive data 
    * This is called by CvTemplateInitializer when the app starts.
    * Takes my ZIP file data and processes each CV template inside.
    * 
    * This is called to upload a Zip file directly
    * Converts MultipartFile to byte array and delegates to uploadTemplatesFromArchiveData.
    */
    public void uploadTemplatesFromArchive(MultipartFile archiveFile) throws IOException {
        if (archiveFile.isEmpty()) {
            throw new IllegalArgumentException("Archive file is empty");
        }
        
        String originalFilename = archiveFile.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException("Only ZIP files are supported");  //just to make it more robust
        }
        
        System.out.println("File upload: " + originalFilename);
        
        // Convert MultipartFile to byte array and process
        byte[] archiveData = archiveFile.getBytes();
        uploadTemplatesFromArchiveData(archiveData, originalFilename);
    }
    

    
    /**
     * 
     * @param archiveData - The bytes of your ZIP/RAR file
     * @param archiveFileName - Name of the archive fileh
     * @throws IOException 
     */
    public void uploadTemplatesFromArchiveData(byte[] archiveData, String archiveFileName) throws IOException {
        System.out.println("Processing CV templates from: " + archiveFileName);
        
        // Create a ZIP input stream from the byte array
        try (ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(archiveData))) {
            //this will create a streamer to parse que local file into a zip object
            ZipEntry entry;
            int templateCount = 0;
            
            // Loop through each file in the ZIP
            while ((entry = zipStream.getNextEntry()) != null) {
                
                if (isValidCvFile(entry.getName())) {
                    try {
                        // Read the actual file content from ZIP
                        byte[] fileData = zipStream.readAllBytes();
                        
                        // Extract file information
                        String fileName = entry.getName(); 
                        String fileType = getFileExtension(fileName).toUpperCase();
                        String templateName = generateTemplateName(fileName);
                        
                        // Create new template object
                        CvTemplate template = new CvTemplate(
                            templateName,     
                            fileName,         
                            fileType,         
                            fileData,         
                            "Professional CV Template"  
                        );
                        
                        // Save to database via repository
                        cvTemplateRepository.save(template);
                        templateCount++;
                        
                        System.out.println("Auto-uploaded: " + templateName + " (" + fileType + ", " + fileData.length + " bytes)");
                        
                    } catch (Exception e) {
                        System.err.println("Error processing " + entry.getName() + ": " + e.getMessage());

                    }
                }
                zipStream.closeEntry();
            }
            
            System.out.println("Successfully auto-uploaded " + templateCount + " CV templates");
            
        } catch (IOException e) {
            throw new IOException("Failed to process CV templates archive: " + e.getMessage(), e);
        }
    }
    

    
    /**
     * Get all available CV templates
     * 
     * Returns only active templates, ordered by upload date (newest first).
     * Used by the frontend to show template cards to users.
     * ---controller
     */
    public List<CvTemplate> getAllTemplates() {
        return cvTemplateRepository.findByIsActiveTrueOrderByUploadedAtDesc();
    }
    
    /**
     * Get template by ID
     * 
     * Used when user clicks "Download".
     * 
     */
    public Optional<CvTemplate> getTemplateById(Long id) {
        return cvTemplateRepository.findById(id);
    }
    
    /**
     * Get total number of templates (for checking if any exist)
     * 
     * Used by CvTemplateInitializer to check if auto-upload should run.
     * If count > 0, skips auto-upload to avoid duplicates.
     */
    public long getTemplateCount() {
        return cvTemplateRepository.findByIsActiveTrue().size();
    }
    
    // === MANAGEMENT METHODS ===
    
    /**
     * Delete template
     * 
     * Doesn't actually delete from database, just marks as inactive.
     */
    public void deleteTemplate(Long id) {
        Optional<CvTemplate> template = cvTemplateRepository.findById(id);
        if (template.isPresent()) {
            CvTemplate cvTemplate = template.get();
            cvTemplate.setActive(false);  // Soft delete
            cvTemplateRepository.save(cvTemplate);
            System.out.println("🗑️ Soft deleted template: " + cvTemplate.getTemplateName());
        }
    }
   
    
    /**
     * Only allows PDF, DOC, and DOCX files.
     * Prevents uploading of invalid file types.
     */
    private boolean isValidCvFile(String filename) {
        String lowerFilename = filename.toLowerCase();
        return lowerFilename.endsWith(".pdf") || 
               lowerFilename.endsWith(".doc") || 
               lowerFilename.endsWith(".docx");
    }
    
    /**
     * Extract file extension from filename
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }
    
    /**
     * Generate the actual name of the file
     */
    private String generateTemplateName(String filename) {
        // Remove file extension and path
        String name = filename;
        if (filename.contains("/")) {
            name = filename.substring(filename.lastIndexOf('/') + 1);
        }
        if (filename.contains("\\")) {
            name = filename.substring(filename.lastIndexOf('\\') + 1);
        }
        
        // Remove extention
        if (name.contains(".")) {
            name = name.substring(0, name.lastIndexOf('.'));
        }
        
        // Replace underscores and dashes with spaces
        name = name.replaceAll("[_-]", " ");
        
        // Capitalize each word
        String[] words = name.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1).toLowerCase());
                }
                result.append(" ");
            }
        }
        
        return result.toString().trim();
    }
}

