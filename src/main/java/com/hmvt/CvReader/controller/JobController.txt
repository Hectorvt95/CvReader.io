/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hmvt.CvReader.controller;

import com.hmvt.CvReader.model.Job;
import com.hmvt.CvReader.service.JobSearchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; //this is for the controller REST for web app
import reactor.core.publisher.Mono;

import java.util.List;

/**
 *
 * @author marti
 */

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class JobController {
    
    @Autowired
    private JobSearchService jobSearchService;
    
    @GetMapping("/jobs/search")
    public Mono<ResponseEntity<List<Job>>> searchJobs(@RequestParam String query) {
        return jobSearchService.searchJobs(query)
            .map(jobs -> ResponseEntity.ok(jobs))
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Job Search API is running!");
    }
    
}
