package com.datasetretrievalwithlucene.demo.Controller;

import com.datasetretrievalwithlucene.demo.util.DBIndexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class SearchController {
    private final DBIndexer dbIndexer;

    public SearchController(DBIndexer dbIndexer) {
        this.dbIndexer = dbIndexer;
    }

    @RequestMapping ("/index")
    public String DoIndex() {
        dbIndexer.main();
        return "success";
    }
}
