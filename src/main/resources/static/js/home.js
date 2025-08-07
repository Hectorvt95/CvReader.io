
// Global variables to store current job and skills data
let currentJobsData = null;
let currentSkillsData = null;
let currentFilename = null; // NEW: Store the uploaded filename


// File input handling W
function updateFileName() {
    const fileInput = document.getElementById('fileInput');
    const fileName = document.getElementById('fileName');
    
    document.getElementById('gdprConsentMain').style.display = 'block';
}

// Message display functions W
function showMessage(message, type) {
    const messageContainer = document.getElementById('messageContainer');
    const messageClass = type === 'error' ? 'error-message' : 'success-message';
    messageContainer.innerHTML = `
        <div class="${messageClass}">
            <i class="fa fa-${type === 'error' ? 'exclamation-triangle' : 'check-circle'}"></i>
            ${message}
        </div>
    `;
}

// W
function clearMessages() {
    document.getElementById('messageContainer').innerHTML = '';
}

// Loading the logo spinningg W
function showLoading() {
    document.getElementById('submitBtn').style.display = 'none';
    document.getElementById('loadingSpinner').style.display = 'block';
}

// w
function hideLoading() {
    document.getElementById('submitBtn').style.display = 'inline-block';
    document.getElementById('loadingSpinner').style.display = 'none';
}

// Reset form to initial upload state
function resetToUpload() {
    // Show upload section and welcome message
    document.getElementById('uploadSection').style.display = 'block';
    document.getElementById('welcomeSection').style.display = 'block';
    
    // Hide results sections
    document.getElementById('resultsHeader').style.display = 'none';
    document.getElementById('jobResults').style.display = 'none';
    
    // Clear form and messages
    document.getElementById('uploadForm').reset();
    document.getElementById('fileName').textContent = '';
    clearMessages();
    
    // Clear stored data
    currentJobsData = null;
    currentSkillsData = null;
    currentFilename = null;
    sessionStorage.removeItem('jobsData');
    
    // Reset loading state
    hideLoading();
}

// Skills formatting for display - modify as the skills come as a String
function formatSkillsDisplay(skills) {
    if (!skills || skills.length === 0) {
        return '<em>No skills detected</em>';
    }
    let skillsArray = [];
    
    skillsArray = skills.split(',').map(s => s.trim()).filter(s => s.length > 0);
    const skillBadges = skillsArray.map(skill => 
        `<span class="skill-badge">${escapeHtml(skill)}</span>`
    ).join(' ');
    
    return `
        <div class="skills-container">
            <div class="skills-count">${skillsArray.length} skill${skillsArray.length !== 1 ? 's' : ''} detected:</div>
            <div class="skills-badges">${skillBadges}</div>
        </div>
    `;
}

// HTML escape function to prevent XSS this to make it use the css, wastn reading it before
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Generate job cards HTML for preview 
function generateJobCardsPreview(jobs, maxCards = 100) {
    const jobsToShow = jobs.slice(0, maxCards);
    
    return jobsToShow.map(job => `
        <div class="job-card-expanded">
            <div class="job-source">${job.source || 'Unknown Source'}</div>
            <div class="job-title">${job.title || 'Job Title Not Available'}</div>
            <div class="job-company">
                <i class="fa fa-building" style="margin-right: 8px; color: #666;"></i>
                <span><strong>${job.company || 'Company Not Specified'}</strong></span>
            </div>
            ${job.location ? `
                <div class="job-location">
                    <i class="fa fa-map-marker" style="margin-right: 8px; color: #666;"></i>
                    <span>${job.location}</span>
                </div>
            ` : ''}
            ${job.salary ? `
                <div class="job-salary">
                    <i class="fa fa-money" style="margin-right: 8px; color: #28a745;"></i>
                    <span><strong>${job.salary}</strong></span>
                </div>
            ` : ''}
            ${job.postedDate ? `
                <div class="job-date">
                    <i class="fa fa-clock-o" style="margin-right: 8px; color: #666;"></i>
                    <span>Posted: ${job.postedDate}</span>
                </div>
            ` : ''}
            <div class="job-actions" style="margin-top: 15px;">
                ${job.url ? `
                    <a href="${job.url}" target="_blank" class="btn-apply">
                        <i class="fa fa-external-link"></i> Apply Now
                    </a>
                ` : '<span class="text-muted">No application link available</span>'}
            </div>
        </div>
    `).join('');
}

// Main function to display job results
function displayJobResults(data) {
    // Store the data globally for export functionality
    currentJobsData = data.jobs;
    currentSkillsData = data.skills;
    
    // Store in sessionStorage for the jobs page
    sessionStorage.setItem('jobsData', JSON.stringify(data));
    
    // Hide upload section and welcome message
    document.getElementById('uploadSection').style.display = 'none';
    document.getElementById('welcomeSection').style.display = 'none';
    
    // Show results header
    document.getElementById('resultsHeader').style.display = 'block';
    document.getElementById('jobResults').style.display = 'block';
    
    // Update subtitle with job count
    document.getElementById('resultsSubtitle').textContent = 
        `Found ${data.jobs ? data.jobs.length : 0} job opportunities`;
    
    // Display skills if available
    if (data.skills) {
        document.getElementById('skillsInfo').style.display = 'block';
        document.getElementById('detectedSkills').innerHTML = formatSkillsDisplay(data.skills);
    }
    
    // Generate and display job cards
    const jobCards = document.getElementById('jobCards');
    
    if (data.jobs && data.jobs.length > 0) {
        const previewCards = generateJobCardsPreview(data.jobs);
        
        jobCards.innerHTML = previewCards + `
            <div class="view-more-message" style="text-align: center; padding: 20px; color: #666;">
                <p>Click "View All Jobs" button above to export all the job results.</p>
            </div>
        `;

        jobCards.innerHTML = previewCards;
        
    } else {
        // Show no jobs found message
        const skillsText = data.skills ? 
            (Array.isArray(data.skills) ? data.skills.join(', ') : data.skills.toString()) : 
            'None detected';
        
        jobCards.innerHTML = `
            <div class="no-jobs-message">
                <i class="fa fa-search fa-3x" style="color: #ddd; margin-bottom: 20px;"></i>
                <h3>No Jobs Found</h3>
                <p>We couldn't find any jobs matching the skills from your resume.</p>
                <p><strong>Detected skills:</strong> ${skillsText}</p>
                <p>Try uploading a different resume or check back later.</p>
            </div>
        `;
    }
}

// Function to export jobs to the dedicated jobs page W
//function exportJobsToPage() {
//    if (!currentJobsData || currentJobsData.length === 0) {
//        showMessage('No jobs to export. Please upload a resume first.', 'error');
//        return;
//    } 
//    
//    // Data is already stored in sessionStorage by displayJobResults
//    // Open the jobs page in a new tab
//    window.open('/jobs', '_blank');
//    //window.location.replace('/jobs', '_blank');
//}



async function saveJobsToDatabase() {
    if (!currentJobsData || currentJobsData.length === 0) {
        console.error('No jobs data to save');
        return false;
    }
    
    if (!currentFilename) {
        console.error('No filename available');
        return false;
    }
    
    try {
        console.log('Saving jobs to database...');
        
        const requestData = {
            jobs: currentJobsData,
            filename: currentFilename,
            skills: currentSkillsData || ''
        };
        
        const response = await fetch('/db/api/jobs/save', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const result = await response.json();
        
        if (result.success) {
            console.log('Successfully saved jobs to database:', result.message);
            showMessage(`Saved ${result.savedCount} jobs to database`, 'success');
            return true;
        } else {
            console.error('Failed to save jobs:', result.message);
            showMessage('Failed to save jobs to database', 'error');
            return false;
        }
        
    } catch (error) {
        console.error('Error saving jobs to database:', error);
        showMessage('Error saving jobs to database', 'error');
        return false;
    }
}

// Function to export jobs to the dedicated jobs page
async function exportJobsToPage() {
    if (!currentJobsData || currentJobsData.length === 0) {
        showMessage('No jobs to export. Please upload a resume first.', 'error');
        return;
    }
    
    // Show loading state
    const originalText = document.querySelector('.new-search-btn[onclick="exportJobsToPage()"]').innerHTML;
    document.querySelector('.new-search-btn[onclick="exportJobsToPage()"]').innerHTML = 
        '<i class="fa fa-spinner fa-spin"></i> Saving to Database...';
    
    try {
        // Save jobs to database first
        const saveSuccess = await saveJobsToDatabase();
        
        if (saveSuccess) {
            // Open the database jobs page
            window.open('/db/jobs', '_blank');
        } else {
            showMessage('Failed to save jobs to database', 'error');
        }
    } catch (error) {
        console.error('Error in exportJobsToPage:', error);
        showMessage('Error processing request', 'error');
    } finally {
        // Reset button text
        document.querySelector('.new-search-btn[onclick="exportJobsToPage()"]').innerHTML = originalText;
    }
}




// File validation
function validateFile(file) {
    const allowedTypes = ['application/pdf', 'application/msword'];
    const maxSize = 10 * 1024 * 1024; // 10MB
    
    if (!allowedTypes.includes(file.type) && !file.name.toLowerCase().endsWith('.pdf') && !file.name.toLowerCase().endsWith('.doc')) {
        return { valid: false, message: 'Please upload a PDF or DOC file.' };
    }
    
    if (file.size > maxSize) {
        return { valid: false, message: 'File size must be less than 10MB.' };
    }
    
    return { valid: true };
}

// API call functions
async function extractSkills(file) {
    const formData = new FormData();
    formData.append('filePath', file);
    
    const response = await fetch('/api/resume/skills', {
        method: 'POST',
        body: formData
    });
    
    if (!response.ok) {
        throw new Error(`Skills extraction failed: ${response.status}`);
    }
    
    return await response.text();
}

async function searchJobs(file) {
    const formData = new FormData();
    formData.append('filePath', file);
    
    const response = await fetch('/api/resume/parse', {
        method: 'POST',
        body: formData
    });
    
    if (!response.ok) {
        throw new Error(`Job search failed: ${response.status}`);
    }
    
    return await response.json();
}

// Main form submission handler
async function handleFormSubmission(event) {
    event.preventDefault();
    
    const fileInput = document.getElementById('fileInput');
    if (!fileInput.files.length) {
        showMessage('Please select a file to upload', 'error');
        return;
    }
    
    const file = fileInput.files[0];
    
    currentFilename = file.name;
    console.log('Form submission - filename set to:', currentFilename);
    
    // Validate file
    const validation = validateFile(file);
    if (!validation.valid) {
        showMessage(validation.message, 'error');
        return;
    }
    
    showLoading();
    clearMessages();
    
    try {
        console.log('Starting resume processing...');
        
        // Extract skills from resume
        console.log('Extracting skills...');
        const skillsData = await extractSkills(file);
        console.log('Skills extracted:', skillsData);
        
        // Search for jobs
        console.log('Searching for jobs...');
        const jobs = await searchJobs(file);
        console.log('Jobs found:', jobs.length);
        
        hideLoading();
        
        // Display results
        displayJobResults({
            jobs: jobs,
            skills: skillsData
        });
        
        showMessage(`Successfully processed your resume and found ${jobs.length} job opportunities!`, 'success');
        
    } catch (error) {
        hideLoading();
        console.error('Error processing resume:', error);
        showMessage('Error processing your resume. Please try again.', 'error');
    }
}

// Utility function to check if jobs page is available
function checkJobsPageAvailability() {
    return fetch('/jobs', { method: 'HEAD' })
        .then(response => response.ok)
        .catch(() => false);
}

// Initialize page functionality
function initializePage() {
    // Add form submission event listener
    const uploadForm = document.getElementById('uploadForm');
    if (uploadForm) {
        uploadForm.addEventListener('submit', handleFormSubmission);
    }
    
    // Add file input change listener for immediate validation feedback
    const fileInput = document.getElementById('fileInput');
    if (fileInput) {
        fileInput.addEventListener('change', function() {
            //updateFileName();
            clearMessages();
            
            if (this.files.length > 0) {
                const validation = validateFile(this.files[0]);
                if (!validation.valid) {
                    showMessage(validation.message, 'error');
                    this.value = ''; // Clear invalid file
                    document.getElementById('fileName').textContent = '';
                }
            }
        });
    }
    
    // Clear any existing session data on page load
    // sessionStorage.removeItem('jobsData');
    
    console.log('Home page initialized');
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', initializePage);

// Handle page visibility changes (optional - for future enhancement)
document.addEventListener('visibilitychange', function() {
    if (document.visibilityState === 'visible') {
        // Page became visible - could refresh data or check status
        console.log('Page became visible');
    }
});

// Export functions for potential external use
window.CvReaderHome = {
    resetToUpload,
    exportJobsToPage,
    displayJobResults,
    getCurrentJobsData: () => currentJobsData,
    getCurrentSkillsData: () => currentSkillsData
};

async function downloadAllTemplates() {
    const button = document.querySelector('.btn-cv-templates');
    const originalContent = button.innerHTML;
    
    try {
        console.log("Starting CV templates download...");
        
        // Show loading state
        button.classList.add('loading');
        button.innerHTML = '<i class="fa fa-spinner fa-spin"></i> Preparing Download...';
        
        // Show info message
        showMessage('Preparing your CV templates download...', 'info');
        
        // Call the backend API to get the ZIP file
        const response = await fetch('/api/templates/download-all');
        
        if (response.ok) {
            // Get the ZIP file as blob
            const blob = await response.blob();
            
            // Get filename from response headers (or use default)
            const contentDisposition = response.headers.get('Content-Disposition');
            let filename = 'cv-templates.zip';
            if (contentDisposition) {
                const matches = contentDisposition.match(/filename="(.+)"/);
                if (matches) {
                    filename = matches[1];
                }
            }
            
            // Create download link and trigger download
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = filename;
            document.body.appendChild(a);
            a.click(); // This triggers the browser download
            
            // Cleanup
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
            
            // Show success message
            showMessage('CV templates downloaded successfully!', 'success');
            console.log("Download completed: " + filename);
            
        } else if (response.status === 404) {
            showMessage('No CV templates are currently available. Please check back later.', 'error');
        } else {
            throw new Error(`Download failed: ${response.status}`);
        }
        
    } catch (error) {
        console.error('CV templates download error:', error);
        showMessage('Download failed. Please try again or contact support.', 'error');
    } finally {
        // Reset button state
        button.classList.remove('loading');
        button.innerHTML = originalContent;
    }
}

/**
 * Helper function to show messages (enhanced version)
 */
function showMessage(message, type) {
    const messageContainer = document.getElementById('messageContainer');
    if (!messageContainer) return;
    
    // Clear any existing messages
    messageContainer.innerHTML = '';
    
    const messageClass = type === 'error' ? 'error-message' : 
                       type === 'success' ? 'success-message' : 
                       'alert alert-info';
    
    const iconClass = type === 'error' ? 'fa-exclamation-triangle' : 
                     type === 'success' ? 'fa-check-circle' : 
                     'fa-info-circle';
    
    messageContainer.innerHTML = `
        <div class="${messageClass}" style="animation: slideDown 0.3s ease-out;">
            <i class="fa ${iconClass}"></i>
            ${message}
        </div>
    `;
    
    // Auto-hide success/info messages after 4 seconds
    if (type !== 'error') {
        setTimeout(() => {
            if (messageContainer.innerHTML.includes(message)) {
                messageContainer.innerHTML = '';
            }
        }, 4000);
    }
}

// CSS animation for message slide down effect
const style = document.createElement('style');
style.textContent = `
    @keyframes slideDown {
        0% {
            opacity: 0;
            transform: translateY(-10px);
        }
        100% {
            opacity: 1;
            transform: translateY(0);
        }
    }
    
    .alert-info {
        background-color: #d1ecf1;
        color: #0c5460;
        padding: 15px;
        border-radius: 8px;
        margin-bottom: 20px;
        border: 1px solid #bee5eb;
    }
`;
document.head.appendChild(style);