package com.datasetretrievalwithlucene.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RatingController {
    @RequestMapping("/login")
    public String login() {
        return "signin";
    }
    @RequestMapping("/dashboard?q={query}&dsid={dataset_id}")
    public String dashboard(@PathVariable("query") String query, @PathVariable("dataset_id") int dataset_id, Model model) {
        model.addAttribute(query);
        model.addAttribute(dataset_id);
        return "dashboard";
    }
}
