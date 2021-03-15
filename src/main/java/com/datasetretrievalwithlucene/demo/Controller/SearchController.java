package com.datasetretrievalwithlucene.demo.Controller;

import com.datasetretrievalwithlucene.demo.Bean.Dataset;
import com.datasetretrievalwithlucene.demo.Service.DatasetService;
import com.datasetretrievalwithlucene.demo.Service.UserService;
import com.datasetretrievalwithlucene.demo.util.RelevanceRanking;
import javafx.util.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SearchController {
    private final UserService userService;
    private final DatasetService datasetService;

    public SearchController(UserService userService, DatasetService datasetService) {
        this.userService = userService;
        this.datasetService = datasetService;
    }


    @RequestMapping("/search")
    public String starter() {
        return "search";
    }

    @RequestMapping(value = "/result", method = RequestMethod.POST)
    public String BM25result(@RequestParam("query") String query, Model model) {
        List<Pair<Integer, Double>> BM25scoreList = RelevanceRanking.RankingList(query, 0);
        List<Dataset> datasetList = new ArrayList<>();
        for (Pair<Integer, Double> i : BM25scoreList) {
            datasetList.add(datasetService.getByDatasetId(i.getKey()));
        }
        model.addAttribute("datasets", datasetList);
        return "resultlist";
    }

    /*@RequestMapping(value = "/result", method = RequestMethod.POST)
    @ResponseBody
    public String search(@RequestParam("query") String query) {
        return RelevanceRanking.RankingList(query, 0).toString() +
                RelevanceRanking.RankingList(query, 1).toString() +
                RelevanceRanking.RankingList(query, 2).toString();
    }*/

    @RequestMapping(value = "/test")
    @ResponseBody
    public String test() throws IllegalAccessException {
        Dataset s = datasetService.getByDatasetId(1);
        for (Field i : s.getClass().getDeclaredFields()) {
            i.setAccessible(true);
            System.out.println(i.getName() + ": " + i.get(s));
        }
        return "test";
    }
}
