/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hmvt.CvReader.repository;

import com.hmvt.CvReader.model.CvTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author marti
 */


/**
 * CvTemplateRepository - Data Access Layer
 * 
 * This interface defines how we interact with the database.
 * Spring JPA automatically creates implementations of these methods
 * 
 * This is like a "database assistant" and this is what thies does
 * - Save templates to database
 * - Find templates by different criteria
 * - Update existing templates
 * - Delete templates
 */
@Repository  // ← Tells Spring this is a data access component
public interface CvTemplateRepository extends JpaRepository<CvTemplate, Long> { // the long one is for the primary key type, so the type of the entity aswell
  
    // INHERITED METHODS FROM JpaRepository
    // These methods are automatically available without writing code:
    // 
    // Saves template to database = save(CvTemplate template)
    // Finds template by ID =  findById(Long id)
    // Gets all templates = findAll()
    // Deletes template by ID = deleteById(Long id)
    // Counts total templates = count()
    
    //return the list of cvs but we are going to just update one ??
    List<CvTemplate> findByIsActiveTrue();
    
    /**
     * Find all active templates ordered by upload date (newest first)
     * 
     * SQL: SELECT * FROM cv_templates 
     *      WHERE is_active = true 
     *      ORDER BY uploaded_at DESC
     * 
     */
    List<CvTemplate> findByIsActiveTrueOrderByUploadedAtDesc();
    
    
    
    /**
     * Find specific template by name, to check if it already exists 
     * 
     * SQL: SELECT * FROM cv_templates 
     *      WHERE template_name = ? AND is_active = true
     * )
     */
    Optional<CvTemplate> findByTemplateNameAndIsActiveTrue(String templateName);
}

/*
 * HOW SPRING JPA @WORKS:
 * 
 * 1. Spring sees method names like "findByIsActiveTrue"
 * 2. Parses the method name:
 *     "findBy" → SELECT query
 *     "IsActive" → WHERE is_active = True" → true
 * 3. Iy automatically generates SQL and creates implementation
 * 
 * METHOD NAMING PATTERNS:
 * - findBy[FieldName] → WHERE field = value
 * - findBy[FieldName]OrderBy[OtherField] → WHERE field = value ORDER BY other_field
 * - findBy[FieldName]And[OtherField] → WHERE field = value AND other_field = value
 * - findBy[FieldName]True → WHERE field = true
 * 
 */