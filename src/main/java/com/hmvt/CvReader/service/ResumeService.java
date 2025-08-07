/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hmvt.CvReader.service;

/**
 *
 * @author marti
 */

import com.hmvt.CvReader.model.Job;
import com.hmvt.CvReader.model.ResumeInfo;
import com.hmvt.CvReader.parser.ResumeParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import com.hmvt.CvReader.service.JobSearchService;

import java.io.IOException;
import java.util.List;

@Service
public class ResumeService {
    
    @Autowired
    private ResumeParser resumeParser;  
    
    @Autowired
    private JobSearchService jobSearcher;
    
    public String parseResumeFile(String filePath) throws IOException {
       try {
           String resumeInfo = resumeParser.parseResumeSkillsOnly(filePath);

           // Log successful parsing
           System.out.println("Successfully parsed resume: " + filePath);
           System.out.println("Found skills");

           return resumeInfo; 

       } catch (IOException e) {
           System.err.println("Failed to parse resume file: " + filePath + " - " + e.getMessage());
           throw e;
       } catch (Exception e) {
           System.err.println("Unexpected error parsing resume: " + e.getMessage());
           throw new RuntimeException("Failed to parse resume: " + e.getMessage(), e);
       }
   }
    
    
    /**
     * Parse a resume from file path
     * @param filePath Path to the resume file
     * @return ResumeInfo containing extracted information
     * @throws IOException if file cannot be read
     * Check if Lightcast API is working by testing with sample text
     * @return true if API is responding correctly
     * 
     * This checks that
     * - The extraction of the resume context is successfull
     * - The extraction of only the skills is succesfull
     * - The union between this two classes to perform a job search based on the skills
     **/
    
    
    public boolean testLightcastConnection() {
        try {
            // Test with a simple text containing known skills
            //String testContent = "Software Engineer with experience in Java, Python, and React";
            //ResumeInfo testResult = resumeParser.parseResume("test_resume.txt"); // This would be a test file
            String fullPath = "C:\\Users\\marti\\OneDrive\\Documents\\NetBeansProjects\\DistSyst\\Project_CvService\\src\\main\\resources\\cv.pdf";
            ResumeInfo testResult = resumeParser.parseResume(fullPath); // This would be a test file
            
            String testResult2 = resumeParser.parseResumeSkillsOnly(fullPath);
            
            System.out.println("THIS IS THE STRING WITH ALL THE SKILLS");
            System.out.println(testResult2 + "\n");
            
            //now lets try to combine this testresult2 with the job searching
            System.out.println("*******-----> Lets work on the jobs extraction" + "\n");
            Mono<List<Job>> monoList = jobSearcher.searchJobs(testResult2);
            
            List<Job> jobList = monoList.block(); //this will blocks ultil the list is available
            if(jobList != null){
                jobList.forEach(job -> System.out.println(job.getTitle()));
            }else{
                System.out.println("The list of jobs is empty");
            }

            // If we get skills back, API is working
            return !testResult.getSkills().isEmpty();
           
            
        } catch (Exception e) {
            System.err.println("Lightcast API test failed: " + e.getMessage());
            return false;
        }
    }
}