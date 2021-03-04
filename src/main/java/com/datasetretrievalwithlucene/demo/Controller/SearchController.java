package com.datasetretrievalwithlucene.demo.Controller;

import com.datasetretrievalwithlucene.demo.Service.UserService;
import com.datasetretrievalwithlucene.demo.util.RelevanceRanking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class SearchController {
    private final UserService userService;

    public SearchController(UserService userService) {
        this.userService = userService;
    }
    @RequestMapping("/search")
    public String starter() {
        return "search";
    }
    @RequestMapping(value = "/result", method = RequestMethod.POST)
    @ResponseBody
    public String search(@RequestParam("query")String query) {
        return RelevanceRanking.RankingList(query, 0).toString() +
                RelevanceRanking.RankingList(query, 1).toString() +
                RelevanceRanking.RankingList(query, 2).toString();
    }
}
