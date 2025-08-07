/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hmvt.CvReader.repository;

/**
 *
 * @author marti
 */

import com.hmvt.CvReader.model.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, Long> {
    
    // Find jobs by filename
    List<JobEntity> findByFilename(String filename);
    
    // Find jobs by source
    List<JobEntity> findBySource(String source);
    
    // Find jobs by company
    List<JobEntity> findByCompany(String company);
    
    // Find the latest jobs (most recent uploads)
    @Query("SELECT j FROM JobEntity j ORDER BY j.createdAt DESC")
    List<JobEntity> findAllOrderByCreatedAtDesc();
    
    // Find jobs by filename ordered by creation date
    @Query("SELECT j FROM JobEntity j WHERE j.filename = :filename ORDER BY j.createdAt DESC")
    List<JobEntity> findByFilenameOrderByCreatedAtDesc(@Param("filename") String filename);
    
    // Get unique filenames
    @Query("SELECT DISTINCT j.filename FROM JobEntity j ORDER BY j.filename")
    List<String> findDistinctFilenames();
    
    // Count jobs by filename
    @Query("SELECT COUNT(j) FROM JobEntity j WHERE j.filename = :filename")
    Long countByFilename(@Param("filename") String filename);
    
    // Delete jobs by filename
    void deleteByFilename(String filename);
    
    // Search jobs by title or company containing keyword
    @Query("SELECT j FROM JobEntity j WHERE " +
           "LOWER(j.jobTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.company) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.skills) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<JobEntity> searchByKeyword(@Param("keyword") String keyword);
}