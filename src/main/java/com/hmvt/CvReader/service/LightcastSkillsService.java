package com.hmvt.CvReader.service;

import com.hmvt.CvReader.config.LightcastProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

@Service
public class LightcastSkillsService {
    // Using the Skills API endpoints instead of Open Skills
    private static final String AUTH_URL = "https://auth.emsicloud.com/connect/token";
    private static final String SKILLS_API_URL = "https://emsiservices.com/skills/versions/latest";
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    @Autowired
    private LightcastProperties lightcastProperties;
    
    // Token management
    private String accessToken;
    private long tokenExpiryTime;
    
    class SkillScore{
        String skillName;
        int score;
        
        SkillScore(String skillName, int score){
            this.skillName = skillName;
            this.score = score;
        }
        
        @Override
        public String toString(){
            return "Skill Name: " + skillName + ", Score: " + score + "\n";
        }
    }
    
    public LightcastSkillsService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    // Check if token is expired
    private boolean isTokenExpired() {
        return System.currentTimeMillis() >= tokenExpiryTime;
    }
    
    // Authenticate and get access token
    public void authenticate() throws IOException, InterruptedException {
        if (accessToken != null && !isTokenExpired()) {
            return; // Token is still valid
        }
        
        System.out.println("=== AUTHENTICATION ===");
        System.out.println("Client ID: " + lightcastProperties.getClientId());
        System.out.println("Scope: " + lightcastProperties.getScope());
        
        String credentials = lightcastProperties.getClientId() + ":" + lightcastProperties.getClientSecret();
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        
        String requestBody = "grant_type=client_credentials&scope=" + lightcastProperties.getScope();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AUTH_URL))
                .header("Authorization", "Basic " + encodedCredentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Auth Response Status: " + response.statusCode());
        
        if (response.statusCode() == 200) {
            JsonNode tokenResponse = objectMapper.readTree(response.body());
            accessToken = tokenResponse.get("access_token").asText();
            int expiresIn = tokenResponse.get("expires_in").asInt();
            
            tokenExpiryTime = System.currentTimeMillis() + (expiresIn * 1000L) - 60000; // 1 minute buffer
            
            System.out.println("Successfully authenticated with Lightcast API");
        } else {
            throw new RuntimeException("Failed to authenticate: HTTP " + response.statusCode() + " - " + response.body());
        }
    }
    
    
    // Extract skills using Skills API search 
    public Set<String> extractSkillsFromText(String text) throws IOException, InterruptedException {
        System.out.println("--> SKILL EXTRACTION  <--");
        authenticate();
        
        // Get all skills from the API and then match against text
        Set<String> allSkills = getAllSkills();
        Set<String> foundSkills = matchSkillsInText(text, allSkills);
        
        System.out.println("Found " + foundSkills.size() + " skills in text");
       
        //This printing line is just to show what skills were found in the CV, to test the matching.
        System.out.println(foundSkills);
        return foundSkills;
    }
    
    
    // Get all skills from Lightcast Skills API
       public Set<String> getAllSkills() throws IOException, InterruptedException {
        System.out.println("Fetching all skills from API (this may take a moment)...");
        
        Set<String> allSkills = new HashSet<>();
        
        // ---> The easiest way is, for each step into the process of Skills extraction, printing the step so 
        //      if something were to be wrong or working off, we would know in which part of the code this error might be
        System.out.println("Testing API response structure...");
        String skillsUrl = SKILLS_API_URL + "/skills";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(skillsUrl))
                .header("Authorization", "Bearer " + accessToken)  //this access token comes from the authentication, it is the final key lets say
                .header("Accept", "application/json")
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Skills API Response Status: " + response.statusCode());
        
        if (response.statusCode() == 200) {  //this is the status code for the response that enables us to extract the API skills
            JsonNode skillsResponse = objectMapper.readTree(response.body());
            
            // Print response structure for debugging
            System.out.println("Response structure keys: ");
            skillsResponse.fieldNames().forEachRemaining(field -> System.out.println("  - " + field));
            
            // get all skills
            JsonNode skillsArray = skillsResponse.get("data");
            if (skillsArray == null) {
                skillsArray = skillsResponse.get("skills");
            }

            if (skillsArray != null && skillsArray.isArray()) {
                System.out.println("Found " + skillsArray.size() + " skills in single response");
                for (JsonNode skill : skillsArray) {
                    String skillName = skill.get("name").asText().trim(); //for every JSON found in the skillsArray Json Node object, we take it by name
                    if (skillName != null) {
                        allSkills.add(skillName); // <- then the values are stored in this set, to avoid duplicated values
                    }                             //    until the end of the JSON containing a null value.
                }
            } else {
                System.out.println("No skills array found. Available fields:");
                skillsResponse.fieldNames().forEachRemaining(System.out::println);
            }
            
        } else {
            System.err.println("Failed to fetch skills: HTTP " + response.statusCode()); //if the status is not 200, we print it to see the actual status code
            System.err.println("Response body: " + response.body());
        }
        
        System.out.println("Successfully loaded " + allSkills.size() + " total skills from Lightcast API!");
        return allSkills;
    }
    
  
     
    // Match skills in text against the skills "database" extracted from the API
    private Set<String> matchSkillsInText(String text, Set<String> allSkills) {
        List<SkillScore> skillScores = new ArrayList<>();
        String lowerText = text.toLowerCase();
        
        //this is a new aproach for the extraction of the skills in the text, 
        //we are going to evaluate them and select the skills by their value and not only the first 10 skills discovered
        //as in the Version 1 of the matchSkillsInText.
  
        // Match skills against the text
        for (String skill : allSkills) {
            
            String pattern = "\\b" + Pattern.quote(skill.toLowerCase()) + "\\b";
            
            Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            
            Matcher matcher = compiledPattern.matcher(lowerText);
            
            //if (lowerText.contains(skill.toLowerCase())) {
            if (matcher.find()) {    
                int score = calculateScore(lowerText,skill);
                
                if(score > 0 ){
                    skillScores.add(new SkillScore(skill,score));
                    //System.out.println("Found skill: " + skill + " (score: " + score + ")");
                }
                 
            }
        }
        //*******TESTTTT
        //System.out.println(skillScores.toString());
        
        //This is a close aproach to the compare To and the sorting method for lists of objects
        //in this sorting way we will compare the skill scores
        skillScores.sort(new Comparator<SkillScore>() {
            @Override
            public int compare(SkillScore a, SkillScore b) {
                return b.score - a.score;
            }
        });
        
        Set<String> foundSkills = new LinkedHashSet<>();
        for (int i = 0; i < Math.min(10, skillScores.size()); i++) {
            foundSkills.add(skillScores.get(i).skillName);
            System.out.println("Selected: " + skillScores.get(i).skillName + " (score: " + skillScores.get(i).score + ")");
        }
    
        return foundSkills; //the found skillsl is a set (like a hash map) with unique values of skills found in the text (cv)
 
        // WHY LinkedHashSet? 
        // - It keep skills in the order we add them (maintains like the ranking)
        // - It prevents duplicates (like a regular Set)
        // - And it remembers the order
  
    }
    
    
    private int calculateScore(String text, String skill){
        int score = 0;
        //text is already in lower case
        String lowerSkill = skill.toLowerCase();
        
        if(isInSkillsSection(text, lowerSkill)){
            score += 10;
            //System.out.println(skill + " gets +5 points (in Skill section)");
        }
        
        if(isInExperienceSection(text, lowerSkill)){
            score += 7;
            //System.out.println( skill + " gets +5 points (in Experience section)");
        }
        
        if(isInEducationSection(text, lowerSkill)){
            score += 5;
            //System.out.println(skill + " gets +5 points (in Education section)");
        }
        
        int count = countOcurrences(text, lowerSkill); //+1 for each time this skill is mentioned in the text
        score += count;
        //System.out.println(skill + " appears " + count + " times in the text(+" + count + " points)");
        
       
        return score;
    }
    
    
    private boolean isInSkillsSection(String text, String skill){
        String lowerText = text.toLowerCase();
        
        int techSectionStart = lowerText.indexOf("s k i l l s");
        if(techSectionStart == -1){ 
           techSectionStart = lowerText.indexOf("skills");
        }
        if(techSectionStart == -1) return false; //this would ment that this section is not in the text
        
        //will look after education from the starting point of the index of techSectionStart and not from the begining

        int techSectionEnds = lowerText.indexOf("e d u c a t i o n",techSectionStart);
        if (techSectionEnds == -1){
            techSectionEnds = lowerText.indexOf("education",techSectionStart);  
        }
        if (techSectionEnds == -1){
           techSectionEnds = techSectionStart + 150; //lets give 150 words from the skill section if it doesnt find the next section 
           if(techSectionEnds > lowerText.length()){
               techSectionEnds = lowerText.length();
           }
        }
               
        //In theory this section will contains the section of skills, this based on my cv but it 
        //also reduces the chances of reading the whole text and then evaluating skills
        //What about if the structure of the skill is not well and it will return the whole text???
        //++++ Clean up the text by removing line breaks and extra spaces
        String techSection = lowerText.substring(techSectionStart,techSectionEnds);
        
        //This section is to remove the extra spaces or the jump from one like of the pdf to another one
        String cleanedSection = techSection.replaceAll("\\s+", " ");
        String cleanedSkill = skill.toLowerCase().replaceAll("\\s+", " ");
        
        //****tst
        //System.out.println(techSection);
        
        return cleanedSection.contains(cleanedSkill);
        
    }
    
    
    private boolean isInEducationSection(String text,String skill){
        String lowerText = text.toLowerCase();
        
        int eduSectionStart = lowerText.indexOf("e d u c a t i o n");
        if(eduSectionStart == -1){ 
           eduSectionStart = lowerText.indexOf("education");
        }
        if(eduSectionStart == -1){
            return false;
        }
        
        int eduSectionEnds = lowerText.indexOf("w o r k  e x p e r i e n c e",eduSectionStart);
        if (eduSectionEnds == -1){
            eduSectionEnds = lowerText.indexOf("work experience", eduSectionStart);
        }
        if (eduSectionEnds == -1){
            eduSectionEnds = eduSectionStart + 150; // lets give 150 words to fill that education section
            if (eduSectionEnds > lowerText.length()) {
                eduSectionEnds = lowerText.length();
            }
        }
        
        String eduSection = lowerText.substring(eduSectionStart,eduSectionEnds);
        
        String cleanedSection = eduSection.replaceAll("\\s+", " ");
        String cleanedSkill = skill.toLowerCase().replaceAll("\\s+", " ");
        
        //***TEST
        //System.out.println(eduSection);
        
        return cleanedSection.contains(cleanedSkill);
    }
    
    
    private boolean isInExperienceSection(String text, String skill){
        String lowerText = text.toLowerCase();

        int expSectionStart = lowerText.indexOf("w o r k  e x p e r i e n c e");
        if (expSectionStart == -1){
            expSectionStart = lowerText.indexOf("work experience");
        }
        if(expSectionStart == -1){
            return false;
        }
        
        int expSectionEnds = lowerText.indexOf("i n t e r e s t s", expSectionStart);
        if (expSectionEnds == -1){
            expSectionEnds = lowerText.indexOf("interests", expSectionStart);
        }
        if(expSectionEnds == -1){
           expSectionEnds = expSectionStart + 350;
           if (expSectionEnds > lowerText.length()) {
                expSectionEnds = lowerText.length();
            }
        }
        
        String expSection = lowerText.substring(expSectionStart,expSectionEnds);
        
        String cleanedSection = expSection.replaceAll("\\s+", " ");
        String cleanedSkill = skill.toLowerCase().replaceAll("\\s+", " ");
        
        return cleanedSection.contains(cleanedSkill);
    }
    
    
    private int countOcurrences(String text, String skill){
        int count = 0;
        int index = 0;
        
        while((index = text.indexOf(skill,index)) != -1){
            count ++;
            index += skill.length();
        }
        
        return count;
    }
    
    
  
}