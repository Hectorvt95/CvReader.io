/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hmvt.CvReader.controller;

/**
 *
 * @author marti
 */
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//@RestController
@Controller
@RequestMapping("/")
public class WebController {

     
    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("title", "Home");
        model.addAttribute("message", "Welcome to CvReader.IO");
        model.addAttribute("submessage", "Search accross multiple job platforms in one place");
        return "home";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About Us");
        model.addAttribute("message", "Learn more about our company and mission.");
        return "about";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("title", "Contact Us");
        model.addAttribute("message", "Get in touch with us!");
        return "contact";
    }
    
    //this mapping shows a list of jobs but just shows them.
    //the idea is to store the returned jobs into a database
    //as part of the metrics of what the course thought us
//    @GetMapping("/jobs")
//    public String jobs(Model model) {
//        model.addAttribute("title", "Job Results");
//        model.addAttribute("message", "Your Job Matches");
//        model.addAttribute("submessage", "Found job opportunities based on your skills");
//        return "jobs";
//    }
    
   
    
    

}
 