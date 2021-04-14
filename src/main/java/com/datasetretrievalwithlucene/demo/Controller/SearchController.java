package com.datasetretrievalwithlucene.demo.Controller;

import com.datasetretrievalwithlucene.demo.Bean.Dataset;
import com.datasetretrievalwithlucene.demo.Service.DatasetService;
import com.datasetretrievalwithlucene.demo.Service.UserService;
import com.datasetretrievalwithlucene.demo.util.GlobalVariances;
import com.datasetretrievalwithlucene.demo.util.QualityRanking;
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

    private String current_query = "";
    private String current_method = "";


    @RequestMapping("/search")
    public String starter() {
        return "search";
    }

    @RequestMapping(value = "/dosearch", method = RequestMethod.POST)
    public String dosearch(@RequestParam("query") String query) {
        return "redirect:/result?q=" + query + "&method=BM25" + "&page=1";
    }

    @GetMapping(value = "/result")
    public String searchResult(@RequestParam("q") String query,
                             @RequestParam("method") String method,
                             @RequestParam("page") int page,
                             Model model) {

        List<Dataset> datasetList = new ArrayList<>();
        Dataset tmpDataset;
        long datasetID;
        if (current_query.isEmpty() || !current_query.equals(query)) {
            current_query = query;
        }

        if (current_method.isEmpty() || !current_method.equals(method)) {
            current_method = method;
        }

        int previousPage = Math.max(1, page - 1);
        int nextPage = Math.min(100, page + 1);

        switch (method) {
            case "BM25":
                List<Pair<Integer, Double>> BM25ScoreList = RelevanceRanking.BM25RankingList(query);
                /*for (Pair<Integer, Double> i : BM25ScoreList) {
                    datasetList.add(datasetService.getByDatasetId(i.getKey()));
                }*/
                for (int i = (page-1)* GlobalVariances.numOfDatasetsPerPage; i < Math.min(BM25ScoreList.size(), page*GlobalVariances.numOfDatasetsPerPage); i++) {
                    tmpDataset = datasetService.getByDatasetId(BM25ScoreList.get(i).getKey());
                    datasetID = tmpDataset.getDataset_id();
                    if (datasetID > 311)
                        tmpDataset.setDataset_id(datasetID - 221261);
                    tmpDataset.setNotes(tmpDataset.getNotes().replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "").replaceAll("[(/>)<]", ""));
                    datasetList.add(tmpDataset);
                }
                break;
            case "TFIDF":
                List<Pair<Integer, Double>> TFIDFScoreList = RelevanceRanking.TFIDFRankingList(query);
                /*for (Pair<Integer, Double> i : TFIDFScoreList) {
                    datasetList.add(datasetService.getByDatasetId(i.getKey()));
                }*/
                for (int i = (page-1)* GlobalVariances.numOfDatasetsPerPage; i < Math.min(TFIDFScoreList.size(), page*GlobalVariances.numOfDatasetsPerPage); i++) {
                    tmpDataset = datasetService.getByDatasetId(TFIDFScoreList.get(i).getKey());
                    datasetID = tmpDataset.getDataset_id();
                    if (datasetID > 311)
                        tmpDataset.setDataset_id(datasetID - 221261);
                    tmpDataset.setNotes(tmpDataset.getNotes().replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "").replaceAll("[(/>)<]", ""));
                    datasetList.add(tmpDataset);
                }
                break;
            case "FSDM":
                List<Pair<Integer, Double>> FSDMScoreList = RelevanceRanking.FSDMRankingList(query);
                /*for (Pair<Integer, Double> i : FSDMScoreList) {
                    datasetList.add(datasetService.getByDatasetId(i.getKey()));
                }*/
                for (int i = (page-1)* GlobalVariances.numOfDatasetsPerPage; i < Math.min(FSDMScoreList.size(), page*GlobalVariances.numOfDatasetsPerPage); i++) {
                    tmpDataset = datasetService.getByDatasetId(FSDMScoreList.get(i).getKey());
                    datasetID = tmpDataset.getDataset_id();
                    if (datasetID > 311)
                        tmpDataset.setDataset_id(datasetID - 221261);
                    tmpDataset.setNotes(tmpDataset.getNotes().replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "").replaceAll("[(/>)<]", ""));
                    datasetList.add(tmpDataset);
                }
                break;
            case "DPR":
                List<Pair<Integer, Double>> DPRScoreList = RelevanceRanking.DPRRankingList(query);
                /*for (Pair<Integer, Double> i : DPRScoreList) {
                    datasetList.add(datasetService.getByDatasetId(i.getKey()));
                }*/
                for (int i = (page-1)* GlobalVariances.numOfDatasetsPerPage; i < Math.min(DPRScoreList.size(), page*GlobalVariances.numOfDatasetsPerPage); i++) {
                    tmpDataset = datasetService.getByDatasetId(DPRScoreList.get(i).getKey());
                    datasetID = tmpDataset.getDataset_id();
                    if (datasetID > 311)
                        tmpDataset.setDataset_id(datasetID - 221261);
                    tmpDataset.setNotes(tmpDataset.getNotes().replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "").replaceAll("[(/>)<]", ""));
                    datasetList.add(tmpDataset);
                }
                break;
            case "DRank":
                List<Pair<Integer, Double>> DRankScoreList = QualityRanking.DRankRankingList(query);
                /*for (Pair<Integer, Double> i : DRankScoreList) {
                    datasetList.add(datasetService.getByDatasetId(i.getKey()));
                }*/
                for (int i = (page-1)* GlobalVariances.numOfDatasetsPerPage; i < Math.min(DRankScoreList.size(), page*GlobalVariances.numOfDatasetsPerPage); i++) {
                    tmpDataset = datasetService.getByDatasetId(DRankScoreList.get(i).getKey());
                    datasetID = tmpDataset.getDataset_id();
                    if (datasetID > 311)
                        tmpDataset.setDataset_id(datasetID - 221261);
                    tmpDataset.setNotes(tmpDataset.getNotes().replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "").replaceAll("[(/>)<]", ""));
                    datasetList.add(tmpDataset);
                }
                break;
            case "PageRank":
                List<Pair<Integer, Double>> PageRankScoreList = QualityRanking.PageRankRankingList(query);
                /*for (Pair<Integer, Double> i : PageRankScoreList) {
                    datasetList.add(datasetService.getByDatasetId(i.getKey()));
                }*/
                for (int i = (page-1)* GlobalVariances.numOfDatasetsPerPage; i < Math.min(PageRankScoreList.size(), page*GlobalVariances.numOfDatasetsPerPage); i++) {
                    tmpDataset = datasetService.getByDatasetId(PageRankScoreList.get(i).getKey());
                    datasetID = tmpDataset.getDataset_id();
                    if (datasetID > 311)
                        tmpDataset.setDataset_id(datasetID - 221261);
                    tmpDataset.setNotes(tmpDataset.getNotes().replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "").replaceAll("[(/>)<]", ""));
                    datasetList.add(tmpDataset);
                }
                break;
            case "DING":
                List<Pair<Integer, Double>> DINGScoreList = QualityRanking.DINGRankingList(query);
                /*for (Pair<Integer, Double> i : DINGScoreList) {
                    datasetList.add(datasetService.getByDatasetId(i.getKey()));
                }*/
                for (int i = (page-1)* GlobalVariances.numOfDatasetsPerPage; i < Math.min(DINGScoreList.size(), page*GlobalVariances.numOfDatasetsPerPage); i++) {
                    tmpDataset = datasetService.getByDatasetId(DINGScoreList.get(i).getKey());
                    datasetID = tmpDataset.getDataset_id();
                    if (datasetID > 311)
                        tmpDataset.setDataset_id(datasetID - 221261);
                    tmpDataset.setNotes(tmpDataset.getNotes().replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "").replaceAll("[(/>)<]", ""));
                    datasetList.add(tmpDataset);
                }
                break;
        }


        model.addAttribute("datasets", datasetList);
        model.addAttribute("query", query);
        model.addAttribute("method", method);
        model.addAttribute("page", page);
        model.addAttribute("previouspage", previousPage);
        model.addAttribute("nextpage", nextPage);
        model.addAttribute("detailURL", GlobalVariances.detailPageURL);
        return "resultlist";
    }

    /*@RequestMapping(value = "/result", method = RequestMethod.POST)
    @ResponseBody
    public String search(@RequestParam("query") String query) {
        return RelevanceRanking.RankingList(query, 0).toString() +
                RelevanceRanking.RankingList(query, 1).toString() +
                RelevanceRanking.RankingList(query, 2).toString();
    }*/

    @GetMapping(value = "/detail")
    public String getDetail(@RequestParam("dsid") int dataset_id, Model model) {
        Dataset dataset = datasetService.getByDatasetId(dataset_id);
        int score = 0;
        model.addAttribute("dataset", dataset);
        model.addAttribute("score", score);
        model.addAttribute("detailURL", GlobalVariances.detailPageURL);
        return "detaildashboard";
    }

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
