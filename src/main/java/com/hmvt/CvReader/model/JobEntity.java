/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hmvt.CvReader.model;

/**
 *
 * @author marti
 */

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
public class JobEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "filename", nullable = false)
    private String filename;
    
    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;
    
    @Column(name = "job_title")
    private String jobTitle;
    
    @Column(name = "company")
    private String company;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "source")
    private String source;
    
    @Column(name = "salary")
    private String salary;
    
    @Column(name = "posted_date")
    private String postedDate;
    
    @Column(name = "job_url", columnDefinition = "TEXT")
    private String jobUrl;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Default constructor
    public JobEntity() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructor with parameters
    public JobEntity(String filename, String skills, String jobTitle, String company, 
                     String location, String source, String salary, String postedDate, String jobUrl) {
        this.filename = filename;
        this.skills = skills;
        this.jobTitle = jobTitle;
        this.company = company;
        this.location = location;
        this.source = source;
        this.salary = salary;
        this.postedDate = postedDate;
        this.jobUrl = jobUrl;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    
    public String getFilename() {return filename;}
    public void setFilename(String filename) {this.filename = filename;}
    
    public String getSkills() {return skills;}
    public void setSkills(String skills) {this.skills = skills;}
    
    public String getJobTitle() {return jobTitle;}
    public void setJobTitle(String jobTitle) {this.jobTitle = jobTitle;}
    
    public String getCompany() {return company;}
    public void setCompany(String company) {this.company = company;}
    
    public String getLocation() {return location;}
    public void setLocation(String location) {this.location = location;}
    
    public String getSource() {return source;}
    public void setSource(String source) {this.source = source;}
    
    public String getSalary() {return salary;}
    public void setSalary(String salary) {this.salary = salary;}
    
    public String getPostedDate() {return postedDate;}
    public void setPostedDate(String postedDate) {this.postedDate = postedDate;}
    
    public String getJobUrl() {return jobUrl;}
    public void setJobUrl(String jobUrl) {this.jobUrl = jobUrl;}
    
    public LocalDateTime getCreatedAt() {return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}
    
    @Override
    public String toString() {
        return "JobEntity{" +
                "id=" + id +
                ", filename='" + filename + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", company='" + company + '\'' +
                ", location='" + location + '\'' +
                ", source='" + source + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
