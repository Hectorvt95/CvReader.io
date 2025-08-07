package com.hmvt.CvReader.service;

import com.hmvt.CvReader.model.JobEntity;
import com.hmvt.CvReader.model.Job;
import com.hmvt.CvReader.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobDatabaseService {
    
    @Autowired
    private JobRepository jobRepository;
    
    /**
     * Save jobs to database
     * @param jobs List of Job objects
     * @param filename Original filename of the uploaded CV
     * @param skills Detected skills from CV
     * @return Number of jobs saved
     */
    @Transactional
    public int saveJobs(List<Job> jobs, String filename, String skills) {
        System.out.println("Saving " + jobs.size() + " jobs to database for file: " + filename);
        
        // First, delete existing jobs for this filename to avoid duplicates
        deleteJobsByFilename(filename);
        
        // Convert Job objects to JobEntity and save
        List<JobEntity> jobEntities = jobs.stream()
                .map(job -> new JobEntity(
                    filename,
                    skills,
                    job.getTitle(),
                    job.getCompany(),
                    job.getLocation(),
                    job.getSource(),
                    job.getSalary(),
                    job.getPostedDate(),
                    job.getUrl()
                ))
                .collect(Collectors.toList());
        
        List<JobEntity> savedJobs = jobRepository.saveAll(jobEntities);
        
        System.out.println("Successfully saved " + savedJobs.size() + " jobs to database");
        return savedJobs.size();
    }
    
    /**
     * Get all jobs from database
     * @return List of Job objects
     */
    public List<Job> getAllJobs() {
        List<JobEntity> jobEntities = jobRepository.findAllOrderByCreatedAtDesc();
        return convertToJobList(jobEntities);
    }
    
    /**
     * Get jobs by filename
     * @param filename Original filename
     * @return List of Job objects
     */
    public List<Job> getJobsByFilename(String filename) {
        List<JobEntity> jobEntities = jobRepository.findByFilenameOrderByCreatedAtDesc(filename);
        return convertToJobList(jobEntities);
    }
    
    /**
     * Get jobs by source
     * @param source Job source (e.g., "Jooble", "ArbeitNow")
     * @return List of Job objects
     */
    public List<Job> getJobsBySource(String source) {
        List<JobEntity> jobEntities = jobRepository.findBySource(source);
        return convertToJobList(jobEntities);
    }
    
    /**
     * Search jobs by keyword
     * @param keyword Search term
     * @return List of Job objects matching the keyword
     */
    public List<Job> searchJobs(String keyword) {
        List<JobEntity> jobEntities = jobRepository.searchByKeyword(keyword);
        return convertToJobList(jobEntities);
    }
    
    /**
     * Get all unique filenames
     * @return List of filenames
     */
    public List<String> getAllFilenames() {
        return jobRepository.findDistinctFilenames();
    }
    
    /**
     * Count jobs by filename
     * @param filename Original filename
     * @return Number of jobs for this filename
     */
    public Long countJobsByFilename(String filename) {
        return jobRepository.countByFilename(filename);
    }
    
    /**
     * Delete jobs by filename
     * @param filename Original filename
     */
    @Transactional
    public void deleteJobsByFilename(String filename) {
        jobRepository.deleteByFilename(filename);
        System.out.println("Deleted jobs for filename: " + filename);
    }
    
    /**
     * Delete all jobs
     */
    @Transactional
    public void deleteAllJobs() {
        jobRepository.deleteAll();
        System.out.println("Deleted all jobs from database");
    }
    
    /**
     * Get database statistics
     * @return Statistics about the database
     */
    public DatabaseStats getDatabaseStats() {
        long totalJobs = jobRepository.count();
        List<String> filenames = getAllFilenames();
        
        return new DatabaseStats(totalJobs, filenames.size(), filenames);
    }
    
    /**
     * Convert JobEntity list to Job list
     * @param jobEntities List of JobEntity objects
     * @return List of Job objects
     */
    private List<Job> convertToJobList(List<JobEntity> jobEntities) {
        return jobEntities.stream()
                .map(entity -> {
                    Job job = new Job();
                    job.setTitle(entity.getJobTitle());
                    job.setCompany(entity.getCompany());
                    job.setLocation(entity.getLocation());
                    job.setSource(entity.getSource());
                    job.setSalary(entity.getSalary());
                    job.setPostedDate(entity.getPostedDate());
                    job.setUrl(entity.getJobUrl());
                    // FIXED: Include filename and skills from entity
                    job.setFilename(entity.getFilename());
                    job.setSkills(entity.getSkills());
                    return job;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Inner class for database statistics
     */
    public static class DatabaseStats {
        private final long totalJobs;
        private final int totalFilenames;
        private final List<String> filenames;
        
        public DatabaseStats(long totalJobs, int totalFilenames, List<String> filenames) {
            this.totalJobs = totalJobs;
            this.totalFilenames = totalFilenames;
            this.filenames = filenames;
        }
        
        public long getTotalJobs() { return totalJobs; }
        public int getTotalFilenames() { return totalFilenames; }
        public List<String> getFilenames() { return filenames; }
    }
}