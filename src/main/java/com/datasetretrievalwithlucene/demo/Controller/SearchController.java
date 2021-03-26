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

    @RequestMapping(value = "/dosearch", method = RequestMethod.POST)
    public String dosearch(@RequestParam("query") String query) {
        return "redirect:/result?q=" + query + "&method=BM25";
    }

    @GetMapping(value = "/result")
    public String BM25result(@RequestParam("q") String query,
                             @RequestParam("method") String method,
                             Model model) {

        List<Dataset> datasetList = new ArrayList<>();
        switch (method) {
            case "BM25":
                List<Pair<Integer, Double>> BM25ScoreList = RelevanceRanking.BM25RankingList(query);
                for (Pair<Integer, Double> i : BM25ScoreList) {
                    datasetList.add(datasetService.getByDatasetId(i.getKey()));
                }
                break;
            case "TFIDF":
                List<Pair<Integer, Double>> TFIDFScoreList = RelevanceRanking.TFIDFRankingList(query);
                for (Pair<Integer, Double> i : TFIDFScoreList) {
                    datasetList.add(datasetService.getByDatasetId(i.getKey()));
                }
                break;
            case "FSDM":
                List<Pair<Integer, Double>> FSDMScoreList = RelevanceRanking.FSDMRankingList(query);
                for (Pair<Integer, Double> i : FSDMScoreList) {
                    datasetList.add(datasetService.getByDatasetId(i.getKey()));
                }
                break;
            case "DPR":
                List<Pair<Integer, Double>> DPRScoreList = RelevanceRanking.DPRRankingList(query);
                for (Pair<Integer, Double> i : DPRScoreList) {
                    datasetList.add(datasetService.getByDatasetId(i.getKey()));
                }
        }
        model.addAttribute("datasets", datasetList);
        model.addAttribute("query", query);
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
