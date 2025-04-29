package org.example.popitkan5.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DocumentationController {

    @GetMapping("/swagger-ui.html")
    public String redirectToCustomDocumentation() {
        return "redirect:/api-docs.html";
    }
    
    @GetMapping("/api-docs")
    public String redirectToApiDocs() {
        return "redirect:/api-docs.html";
    }
}
