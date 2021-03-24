package com.datasetretrievalwithlucene.demo.Controller;

import com.datasetretrievalwithlucene.demo.util.DBIndexer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {
    private final DBIndexer dbIndexer;

    public IndexController(DBIndexer dbIndexer) {
        this.dbIndexer = dbIndexer;
    }

    @RequestMapping("/index")
    public String DoIndex() {
        dbIndexer.main();
        return "success";
    }

}
