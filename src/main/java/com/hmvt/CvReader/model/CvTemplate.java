/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hmvt.CvReader.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 *
 * @author marti
 */

/**
 * CvTemplate Entity - Represents a CV template stored in the database
 * 
 * This class is the "blueprint" for how CV templates are stored in the H2 database.
 * Each instance represents one CV template file (PDF, DOC, DOCX).
 */
@Entity  // ← Tells Spring JPA this is a database table
@Table(name = "cv_templates")  //  Table name in database
public class CvTemplate {
    
    // === PRIMARY KEY ===
    @Id  // ← This field is the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  //  Auto-increment ID
    private Long id;  // Unique identifier 
    
    // === TEMPLATE METADATA ===
    @Column(nullable = false)  //  Cannot be null in database
    private String templateName;  //  "Modern Professional CV"
    
    @Column(nullable = false)
    private String originalFileName;  //  "modern_cv.pdf"
    
    @Column(nullable = false)
    private String fileType;  // "PDF", "DOC", "DOCX"
    
    // === FILE STORAGE ===
    @Lob  // ← Large Object - stores binary data (the actual file)
    @Column(name = "file_data", nullable = false)
    private byte[] fileData;  // THE ACTUAL CV FILE AS BYTES******
    
    @Column(nullable = false)
    private Long fileSize;  // File size in bytes
    
    // === ADDITIONAL INFO ===
    @Column(length = 500)  //  Max characters
    private String description;  // "Professional CV Template"
    
    @Column(nullable = false)
    private LocalDateTime uploadedAt;  // When was it uploaded
    
    @Column(nullable = false)
    private boolean isActive = true;  // Soft delete flag (true = visible, false = hidden)
    
  
    public CvTemplate() {
        this.uploadedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor for creating new template
     * Used when uploading templates from ZIP file
     */
    public CvTemplate(String templateName, String originalFileName, String fileType, 
                     byte[] fileData, String description) {
        this();  // Call default constructor 
        this.templateName = templateName;
        this.originalFileName = originalFileName;
        this.fileType = fileType;
        this.fileData = fileData;
        this.fileSize = (long) fileData.length;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    
    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
    
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    
    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { 
        this.fileData = fileData;
        // Automatically update file size when file data changes
        this.fileSize = fileData != null ? (long) fileData.length : 0L;
    }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    
    /**
     * Helper method to get file size in human-readable format
     * to make it easier when measuring it but not in kb
     */
    public String getFileSizeFormatted() {
        if (fileSize == null || fileSize == 0) return "0 KB";
        return String.format("%.1f KB", fileSize / 1024.0);
    }
}

/*
 * HOW THIS WORKS:
 * 
 * 1. When Spring Boot starts, JPA sees @Entity annotation (same as in the DB)
 * 2. Creates table "cv_templates" with these columns:
 *    - id (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
 *    - template_name (VARCHAR) 
 *    - original_filename (VARCHAR)
 *    - file_type (VARCHAR)
 *    - file_data (BLOB) ← Stores your actual PDF/DOC files!
 *    - file_size (BIGINT)
 *    - description (VARCHAR(500))
 *    - uploaded_at (TIMESTAMP)
 *    - is_active (BOOLEAN)
 * 
 * 3. Each row in this table = one CV template file
 * 4. The file_data column contains the actual bytes
 * 5. When user downloads, these bytes are sent back as a file
 */