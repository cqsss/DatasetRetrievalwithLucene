package com.datasetretrievalwithlucene.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SearchController {
    @RequestMapping("/search")
    public String search() {
        return "search";
    }
}
