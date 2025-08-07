package com.hmvt.CvReader.config;

import com.hmvt.CvReader.service.CvTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * CvTemplateInitializer - The Auto-Upload Magic Component
 * 
 * This is the component that automatically uploads all the CV templates when the application starts
 * 
 * HOW THIS WORKS:
 * 1. Spring Boot finishes loading all components
 * 2. run() method is automatically called
 * 3. It looks for the CV templates archive
 * 4. Uploads all templates to the database
 * 5. Users can immediately download template
 */
@Component  //Tells Spring to load this component
public class CvTemplateInitializer implements ApplicationRunner {
    //This interface makes run() method execute after startup
    
    @Autowired 
    private CvTemplateService cvTemplateService;

    /** 
     * Is auto-upload enabled? From application.properties: cv.templates.auto-upload.enabled=true
     */
    @Value("${cv.templates.auto-upload.enabled:true}")
    private boolean autoUploadEnabled;
    
    /**
     * From application.properties: cv.templates.auto-upload.path=src/main/resources/cv-templates
     */
    @Value("${cv.templates.auto-upload.path:src/main/resources/cv-templates}")
    private String templatesPath;
    
    /**
     * From application.properties: cv.templates.auto-upload.archive-name=cv-templates.zip
     */
    @Value("${cv.templates.auto-upload.archive-name:cv-templates.zip}")
    private String archiveName;
    
    
    /**
     * This method runs automatically after Spring Boot finishes starting up!
     * This completelly runs once when the application starts
     * 
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("CV TEMPLATES AUTO-UPLOAD INITIALIZER");
        System.out.println("=".repeat(50));
        
        //if auto-upload is enabled, thi scomes from the project properties
        if (!autoUploadEnabled) {
            System.out.println("Auto-upload is disabled in configuration");
            System.out.println("To enable: set cv.templates.auto-upload.enabled=true");
            return;
        }
        
        //Check if templates exist
        long existingCount = cvTemplateService.getTemplateCount();
        if (existingCount > 0) {
            System.out.println("CV Templates already exist in database (" + existingCount + " templates)");
            return;
        }
        
        //Look for templates to upload
        System.out.println("Searching for CV templates...");
        System.out.println("Looking in: " + templatesPath);
        System.out.println("Archive name: " + archiveName);
        
        try {
            // Try to load from archive file
            loadFromArchive();
            System.out.println("Auto-upload completed successfully from archive!");
            return;
                  
        } catch (Exception e) {
            System.err.println("Auto-upload failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("=".repeat(50));
        }
    }
        
    /**
     * Try to find and load CV templates from a ZIP/RAR archive
     * @return true if archive found and processed
     */
    private boolean loadFromArchive() {
        System.out.println("Looking for CV templates archive...");
        
        // check if the zip file is in the folder path--- TEST! *
        String path = templatesPath + "/" + archiveName;
        Path archivePath = Paths.get(path);
        
        if (Files.exists(archivePath)) {
            System.out.println("Found CV templates archive: " + archivePath.toAbsolutePath());

            try {
                // Read the entire archive file into memory
                byte[] archiveData = Files.readAllBytes(archivePath);
                System.out.println("Archive size: " + (archiveData.length / 1024) + " KB");

                // Pass the archive data to the service for processing
                cvTemplateService.uploadTemplatesFromArchiveData(
                    archiveData, //the bytes
                    archivePath.getFileName().toString() //the name of the file
                );

                System.out.println("Successfully processed archive: " + path);
                return true; 

            } catch (Exception e) {
                System.out.println("Failed to process archive " +path+ ": " + e.getMessage());
            }
        }
     
        System.out.println("No CV templates archive found in the location given: " + path);
        return false; 
    }
  
}

/*
 * AUTO-UPLOAD FLOW SUMMARY:
 * 
 * 1. APPLICATION STARTS
 *    └── Spring Boot loads all components
 *    └── CvTemplateInitializer.run() is automatically called
 * 
 * 2. CONFIGURATION CHECK
 *    └── Check if auto-upload is enabled
 *    └── Check if templates already exist
 * 
 * 3. SEARCH FOR TEMPLATES
 *    ├── Try to find ZIP/RAR archive in multiple locations
 *    ├── If archive found: process it and upload all templates
 *    └── If no archive: scan directory for individual files
 * 
 * 4. UPLOAD PROCESS
 *    ├── Read file data into memory
 *    ├── Generate names
 *    ├── Create CvTemplate entities
 *    └── Save to database via CvTemplateService
 *
 * 
 * CONFIGURATION application.properties:
 * 
 * cv.templates.auto-upload.enabled=true          # Enable/disable auto-upload
 * cv.templates.auto-upload.path=src/main/resources/cv-templates  # Where to look
 * cv.templates.auto-upload.archive-name=cv-templates.zip        # Archive filename
 * 
 */