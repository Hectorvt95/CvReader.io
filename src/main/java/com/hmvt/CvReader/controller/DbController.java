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
import com.hmvt.CvReader.service.JobDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/db")
public class DbController {
    
    @Autowired
    private JobDatabaseService jobDatabaseService;
    
    /**
     * Request this class for saving jobs
     */
    public static class SaveJobsRequest {
        private List<Job> jobs;
        private String filename;
        private String skills;
        
        // Constructors
        public SaveJobsRequest() {}
        
        public SaveJobsRequest(List<Job> jobs, String filename, String skills) {
            this.jobs = jobs;
            this.filename = filename;
            this.skills = skills;
        }
        
        // Getters and Setters
        public List<Job> getJobs() { return jobs; }
        public void setJobs(List<Job> jobs) { this.jobs = jobs; }
        
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        
        public String getSkills() { return skills; }
        public void setSkills(String skills) { this.skills = skills; }
    }
    
    
    @GetMapping("/jobs")
    public String dbJobs(Model model) {
        model.addAttribute("title", "Database Jobs");
        model.addAttribute("message", "Jobs from Database");
        model.addAttribute("submessage", "All stored job opportunities");
        return "db_jobs";
    }

    
    /**
     * API endpoint to save jobs to database
     */
    @PostMapping("/api/jobs/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveJobs(@RequestBody SaveJobsRequest request) {
        try {
            int savedCount = jobDatabaseService.saveJobs(
                request.getJobs(), 
                request.getFilename(), 
                request.getSkills()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("savedCount", savedCount);
            response.put("message", "Successfully saved " + savedCount + " jobs to database");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error saving jobs: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API endpoint to get all jobs from database
     */
    @GetMapping("/api/jobs")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllJobs() {
        try {
            List<Job> jobs = jobDatabaseService.getAllJobs();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("jobs", jobs);
            response.put("totalJobs", jobs.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving jobs: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API endpoint to clear all jobs from database
     */
    @DeleteMapping("/api/jobs/clear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearAllJobs() {
        try {
            jobDatabaseService.deleteAllJobs();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully cleared all jobs from database");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error clearing database: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    
    
}
