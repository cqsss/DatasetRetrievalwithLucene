package com.datasetretrievalwithlucene.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RatingController {
    @RequestMapping("/login")
    public String login() {
        return "signin";
    }
    @RequestMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}
