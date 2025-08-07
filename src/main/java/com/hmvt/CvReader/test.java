/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hmvt.CvReader;

import com.hmvt.CvReader.parser.ResumeParser;
import com.hmvt.CvReader.service.ResumeService;
import com.hmvt.CvReader.service.LightcastSkillsService;
import java.util.Set;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Scanner;

/**
 *
 * @author marti
 */
public class test {
    
    public static void main(String[] args) {
            // Start Spring Boot context to get proper dependency injection
            ConfigurableApplicationContext context = SpringApplication.run(SimpleRestServiceApplication.class, args);
                  
           
           try {
               // Get the ResumeParser bean from Spring context
               ResumeService resumeService = context.getBean(ResumeService.class);
               ResumeParser resumeParser = context.getBean(ResumeParser.class);         
               
               String fullPath = "C:\\Users\\marti\\OneDrive\\Documents\\NetBeansProjects\\DistSyst\\CvReader\\src\\main\\resources\\cv.pdf";
               System.out.println("=== TESTING RESUME PARSER ===");

//               System.out.println(resumeParser.readFile(fullPath));
               //System.out.println("Lets do the parseResumeSkillsOnly\n");
               //System.out.println(resumeParser.parseResumeSkillsOnly(fullPath));
              
               
               LightcastSkillsService skillService = context.getBean(LightcastSkillsService.class);
               
               skillService.authenticate();
               
               Set<String> apiSkills = skillService.getAllSkills();
               
               Scanner sc = new Scanner(System.in);
               String option = "";
              
               do{
                
                 System.out.print("Enter the skill you want to check in the API Lightcast Skill or enter 0 to exit ------> : ");
                 option = sc.nextLine();
                 
                 if(!option.equals("0")){
                    System.out.println(apiSkills.contains(option));
                 }
                
               }while(!option.equals("0"));
               sc.close();
               
               
//               JOptionPane.showMessageDialog(null,apiSkills.contains(option));
//
//               System.out.println(apiSkills.contains("Adaptability"));
//               System.out.println(apiSkills.contains("Cooking"));
//               System.out.println(apiSkills.contains("Data Structures"));
//               System.out.println(apiSkills.contains("Spring Boot"));
//               System.out.println(apiSkills.contains("Mechatronics"));
//               
//               System.out.println(apiSkills.contains("Java"));
//               System.out.println(apiSkills.contains("Cybersecurity"));
//               System.out.println(apiSkills.contains("VBA"));
//               System.out.println(apiSkills.contains("HTML"));
//               System.out.println(apiSkills.contains("REST"));
               
         
           }  catch(Exception e) {
               System.out.println("Spring context error: " + e.getMessage());
           } finally {
               // Close Spring context
               context.close();
           }
       }
    
}
