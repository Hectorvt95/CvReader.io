/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hmvt.CvReader.controller;

/**
 *
 * @author marti
 */
import com.hmvt.CvReader.model.Job;
import com.hmvt.CvReader.model.ResumeInfo;
import com.hmvt.CvReader.service.JobSearchService;
import com.hmvt.CvReader.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.ui.Model;
import reactor.core.publisher.Mono;
//import org.springframework.ui.Model;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "*")
public class ResumeController {
    
    @Autowired
    private ResumeService resumeService;
    
    @Autowired
    private JobSearchService jobSearchService;
    
    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("title", "Home");
        model.addAttribute("message", "Welcome to CvReader.IO");
        model.addAttribute("submessage", "Search accross multiple job platforms in one place");
        return "home";
    }
    
    @PostMapping("/parse")
    public Mono<ResponseEntity<List<Job>>> parseResumeFile(@RequestParam("filePath") MultipartFile file) {
          try {
            // Validate file
            if (file.isEmpty()) {
                System.err.println("No file uploaded");
                //return ResponseEntity.badRequest().build();
                return null;  
            }
            String originalFilename = file.getOriginalFilename();
         
            if (originalFilename == null || !isValidFileType(originalFilename)) {
                System.err.println("Invalid file type, its not .pdf or .doc: " + originalFilename);
                //return ResponseEntity.badRequest().build();
                return null; 
            }
            
            // Create temporary file
            String tempDir = System.getProperty("java.io.tmpdir");
            Path tempFilePath = Paths.get(tempDir, "uploaded_" + System.currentTimeMillis() + "_" + originalFilename);
            
            // Save uploaded file to temporary location
            Files.copy(file.getInputStream(), tempFilePath, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("File uploaded to: " + tempFilePath.toString());
            
            // Parse the file - the whole resume is parsed but only the skills remain as a String, with parseResumeFile
            //                  this could also work with resumeParser.parseResumeSkillsOnly(filePath); but it just executes 
            //                  and doesnt has any comment or error handling as it doesnt work directly with the file
            String skills = resumeService.parseResumeFile(tempFilePath.toString());

            // Clean up temporary file
            Files.deleteIfExists(tempFilePath);
            
            //call the controler
            //return the map of jobs;
            
            return jobSearchService.searchJobs(skills)
                    .map(jobs -> ResponseEntity.ok(jobs))
                    .defaultIfEmpty(ResponseEntity.notFound().build());
             
        } catch (IOException e) {
            System.err.println("File processing error: " + e.getMessage());
            return null;   
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return null;
        }
    }
    
    @PostMapping("/skills")
    public ResponseEntity<String> parseResumeSkills(@RequestParam("filePath") MultipartFile file) {
          try {
            // Validate file
            if (file.isEmpty()) {
                System.err.println("No file uploaded");
                //return ResponseEntity.badRequest().build();
                return null;  
            }
            String originalFilename = file.getOriginalFilename();
         
            if (originalFilename == null || !isValidFileType(originalFilename)) {
                System.err.println("Invalid file type, its not .pdf or .doc: " + originalFilename);
                return null; 
            }
            
            // Create temporary file
            String tempDir = System.getProperty("java.io.tmpdir");
            Path tempFilePath = Paths.get(tempDir, "uploaded_" + System.currentTimeMillis() + "_" + originalFilename);
            
            // Save uploaded file to temporary location
            Files.copy(file.getInputStream(), tempFilePath, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("File uploaded to: " + tempFilePath.toString());
            
            // Parse the file
            String skills = resumeService.parseResumeFile(tempFilePath.toString());

            // Clean up temporary file
            Files.deleteIfExists(tempFilePath);
            
            return ResponseEntity.ok(skills);
             
        } catch (IOException e) {
            System.err.println("File processing error: " + e.getMessage());
            return null;   
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return null;
        }
    }

       
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Resume parser service is running");
    }
    
     private boolean isValidFileType(String filename) {
        String lowerFilename = filename.toLowerCase();
        return lowerFilename.endsWith(".pdf") || 
               lowerFilename.endsWith(".doc");
    }
    
   
}

