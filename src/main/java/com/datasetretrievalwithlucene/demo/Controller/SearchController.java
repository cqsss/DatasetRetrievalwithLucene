package com.datasetretrievalwithlucene.demo.Controller;

import com.datasetretrievalwithlucene.demo.Bean.Comment;
import com.datasetretrievalwithlucene.demo.Bean.Dataset;
import com.datasetretrievalwithlucene.demo.Bean.Score;
import com.datasetretrievalwithlucene.demo.Bean.User;
import com.datasetretrievalwithlucene.demo.Service.CommentService;
import com.datasetretrievalwithlucene.demo.Service.DatasetService;
import com.datasetretrievalwithlucene.demo.Service.ScoreService;
import com.datasetretrievalwithlucene.demo.Service.UserService;
import com.datasetretrievalwithlucene.demo.util.GlobalVariances;
import com.datasetretrievalwithlucene.demo.util.QualityRanking;
import com.datasetretrievalwithlucene.demo.util.RelevanceRanking;
import javafx.util.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController {
    private final UserService userService;
    private final DatasetService datasetService;
    private final CommentService commentService;
    private final ScoreService scoreService;

    public SearchController(UserService userService, DatasetService datasetService, CommentService commentService, ScoreService scoreService) {
        this.userService = userService;
        this.datasetService = datasetService;
        this.commentService = commentService;
        this.scoreService = scoreService;
    }

    private String current_query = "";
    private String current_method = "";


    @RequestMapping("/search")
    public String starter() {
        return "search_new";
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


        int totalHits = 0;

        switch (method) {
            case "BM25":
                List<Pair<Integer, Double>> BM25ScoreList = RelevanceRanking.BM25RankingList(query);
                totalHits = BM25ScoreList.size();
                /*for (Pair<Integer, Double> i : BM25ScoreList) {
                    datasetList.add(datasetService.getByDatasetId(i.getKey()));
                }*/

                for (int i = (page-1)* GlobalVariances.numOfDatasetsPerPage; i < Math.min(totalHits, page*GlobalVariances.numOfDatasetsPerPage); i++) {
                    tmpDataset = datasetService.getByDatasetId(BM25ScoreList.get(i).getKey());
                    datasetID = tmpDataset.getDataset_id();
                    if (datasetID > 311)
                        tmpDataset.setDataset_id(datasetID - GlobalVariances.datasetIDGap);
                    tmpDataset.setNotes(tmpDataset.getNotes().replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "").replaceAll("[(/>)<]", ""));
                    datasetList.add(tmpDataset);
                }
                break;
            case "TFIDF":
                List<Pair<Integer, Double>> TFIDFScoreList = RelevanceRanking.TFIDFRankingList(query);
                totalHits = TFIDFScoreList.size();
                /*for (Pair<Integer, Double> i : TFIDFScoreList) {
                    datasetList.add(datasetService.getByDatasetId(i.getKey()));
                }*/
                for (int i = (page-1)* GlobalVariances.numOfDatasetsPerPage; i < Math.min(totalHits, page*GlobalVariances.numOfDatasetsPerPage); i++) {
                    tmpDataset = datasetService.getByDatasetId(TFIDFScoreList.get(i).getKey());
                    datasetID = tmpDataset.getDataset_id();
                    if (datasetID > 311)
                        tmpDataset.setDataset_id(datasetID - GlobalVariances.datasetIDGap);
                    tmpDataset.setNotes(tmpDataset.getNotes().replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "").replaceAll("[(/>)<]", ""));
                    datasetList.add(tmpDataset);
                }
                break;
            case "FSDM":
                List<Pair<Integer, Double>> FSDMScoreList = RelevanceRanking.FSDMRankingList(query);
                totalHits = FSDMScoreList.size();
                /*for (Pair<Integer, Double> i : FSDMScoreList) {
                    datasetList.add(datasetService.getByDatasetId(i.getKey()));
                }*/
                for (int i = (page-1)* GlobalVariances.numOfDatasetsPerPage; i < Math.min(FSDMScoreList.size(), page*GlobalVariances.numOfDatasetsPerPage); i++) {
                    tmpDataset = datasetService.getByDatasetId(FSDMScoreList.get(i).getKey());
                    datasetID = tmpDataset.getDataset_id();
                    if (datasetID > 311)
                        tmpDataset.setDataset_id(datasetID - GlobalVariances.datasetIDGap);
                    tmpDataset.setNotes(tmpDataset.getNotes().replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "").replaceAll("[(/>)<]", ""));
                    datasetList.add(tmpDataset);
                }
                break;
            case "DPR":
                List<Pair<Integer, Double>> DPRScoreList = RelevanceRanking.DPRRankingList(query);
                totalHits = DPRScoreList.size();
                /*for (Pair<Integer, Double> i : DPRScoreList) {
                    datasetList.add(datasetService.getByDatasetId(i.getKey()));
                }*/
                for (int i = (page-1)* GlobalVariances.numOfDatasetsPerPage; i < Math.min(totalHits, page*GlobalVariances.numOfDatasetsPerPage); i++) {
                    tmpDataset = datasetService.getByDatasetId(DPRScoreList.get(i).getKey());
                    datasetID = tmpDataset.getDataset_id();
                    if (datasetID > 311)
                        tmpDataset.setDataset_id(datasetID - GlobalVariances.datasetIDGap);
                    tmpDataset.setNotes(tmpDataset.getNotes().replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "").replaceAll("[(/>)<]", ""));
                    datasetList.add(tmpDataset);
                }
                break;
            case "DRank":
                List<Pair<Integer, Double>> DRankScoreList = QualityRanking.DRankRankingList(query);
                totalHits = DRankScoreList.size();
                /*for (Pair<Integer, Double> i : DRankScoreList) {
                    datasetList.add(datasetService.getByDatasetId(i.getKey()));
                }*/
                for (int i = (page-1)* GlobalVariances.numOfDatasetsPerPage; i < Math.min(totalHits, page*GlobalVariances.numOfDatasetsPerPage); i++) {
                    tmpDataset = datasetService.getByDatasetId(DRankScoreList.get(i).getKey());
                    datasetID = tmpDataset.getDataset_id();
                    if (datasetID > 311)
                        tmpDataset.setDataset_id(datasetID - GlobalVariances.datasetIDGap);
                    tmpDataset.setNotes(tmpDataset.getNotes().replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "").replaceAll("[(/>)<]", ""));
                    datasetList.add(tmpDataset);
                }
                break;
            case "PageRank":
                List<Pair<Integer, Double>> PageRankScoreList = QualityRanking.PageRankRankingList(query);
                totalHits = PageRankScoreList.size();
                /*for (Pair<Integer, Double> i : PageRankScoreList) {
                    datasetList.add(datasetService.getByDatasetId(i.getKey()));
                }*/
                for (int i = (page-1)* GlobalVariances.numOfDatasetsPerPage; i < Math.min(totalHits, page*GlobalVariances.numOfDatasetsPerPage); i++) {
                    tmpDataset = datasetService.getByDatasetId(PageRankScoreList.get(i).getKey());
                    datasetID = tmpDataset.getDataset_id();
                    if (datasetID > 311)
                        tmpDataset.setDataset_id(datasetID - GlobalVariances.datasetIDGap);
                    tmpDataset.setNotes(tmpDataset.getNotes().replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "").replaceAll("[(/>)<]", ""));
                    datasetList.add(tmpDataset);
                }
                break;
            case "DING":
                List<Pair<Integer, Double>> DINGScoreList = QualityRanking.DINGRankingList(query);
                totalHits = DINGScoreList.size();
                /*for (Pair<Integer, Double> i : DINGScoreList) {
                    datasetList.add(datasetService.getByDatasetId(i.getKey()));
                }*/
                for (int i = (page-1)* GlobalVariances.numOfDatasetsPerPage; i < Math.min(totalHits, page*GlobalVariances.numOfDatasetsPerPage); i++) {
                    tmpDataset = datasetService.getByDatasetId(DINGScoreList.get(i).getKey());
                    datasetID = tmpDataset.getDataset_id();
                    if (datasetID > 311)
                        tmpDataset.setDataset_id(datasetID - GlobalVariances.datasetIDGap);
                    tmpDataset.setNotes(tmpDataset.getNotes().replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "").replaceAll("[(/>)<]", ""));
                    datasetList.add(tmpDataset);
                }
                break;
        }

        int totalPages = totalHits / GlobalVariances.numOfDatasetsPerPage;
        if (totalHits % GlobalVariances.numOfDatasetsPerPage != 0)
            totalPages ++;
        int previousPage = Math.max(1, page - 1);
        int nextPage = Math.min(totalPages, page + 1);
        Map<Integer, Integer> pages = new HashMap<>();
        for (int i = 0; i < 10 ; i ++) {
            if (page <= 5) {
                pages.put(i + 1, i + 1);
            } else if (page >= totalPages - 4) {
                pages.put(i + 1, totalPages + i - 9);
            } else {
                pages.put(i + 1, page + i - 5);
            }
        }

        model.addAttribute("datasets", datasetList);
        model.addAttribute("query", query);
        model.addAttribute("method", method);
        model.addAttribute("page", page);
        model.addAttribute("pages", pages);
        model.addAttribute("previousPage", previousPage);
        model.addAttribute("nextPage", nextPage);
        model.addAttribute("totalHits", totalHits);
        model.addAttribute("totalPages", totalPages);
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
    public String getDetail(@RequestParam("dsid") int dataset_id, Model model, HttpSession session) {
        Dataset dataset;
        if (dataset_id > 311) {
            dataset = datasetService.getByDatasetId(dataset_id + GlobalVariances.datasetIDGap);
            dataset.setDataset_id(dataset_id);
        } else {
            dataset = datasetService.getByDatasetId(dataset_id);
        }
        dataset.setNotes(dataset.getNotes().replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "").replaceAll("[(/>)<]", ""));
        double score = -1;
        List<Integer> scoreList = scoreService.getScoreListByDatasetId(dataset_id + GlobalVariances.datasetIDGap);
        int len = scoreList.size();
        double sum = 0;
        if (len != 0) {
            for (int i : scoreList)
                sum += i;
            score = sum / len;
        }
        int userScoreNum = -1;
        if (session.getAttribute("userID") != null) {
            Score userScore = scoreService.getScore(Integer.parseInt(session.getAttribute("userID").toString()), dataset_id + GlobalVariances.datasetIDGap);
            if (userScore != null)
                userScoreNum = userScore.getScore_num();
        }
        List<Comment> commentList = commentService.getCommentsByDatasetId(dataset_id + GlobalVariances.datasetIDGap);
        model.addAttribute("dataset", dataset);
        model.addAttribute("score", score);
        model.addAttribute("userScoreNum", userScoreNum);
        model.addAttribute("comments", commentList);
        model.addAttribute("detailURL", GlobalVariances.detailPageURL);
        return "detaildashboard";
    }

    @RequestMapping(value = "/commitcomment", method = RequestMethod.POST)
    public String commitReason(@RequestParam("dsid") int dataset_id,
                               @RequestParam("comment") String comment_text,
                               HttpSession session) {
        if (session.getAttribute("userID") != null) {
            int user_id = Integer.parseInt(session.getAttribute("userID").toString());
            int comment_dataset_id = dataset_id;
            if (comment_text == null)
                comment_text = "";
            if (dataset_id > 311) {
                comment_dataset_id = dataset_id + GlobalVariances.datasetIDGap;
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.now();
            String comment_time = dateTime.format(formatter);
            Comment comment = new Comment();
            comment.setDataset_id(comment_dataset_id);
            comment.setUser_id(user_id);
            comment.setUser_name(userService.getUsernameById(user_id));
            comment.setText(comment_text);
            comment.setComment_time(comment_time);
            commentService.insertComment(comment);
            return "redirect:/detail?dsid=" + dataset_id;
        } else {
            return "redirect:/login";
        }
    }

    @RequestMapping("/login")
    public String login() {
        return "signin";
    }

    @RequestMapping(value = "/signin", method = RequestMethod.POST)
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        Map<String, Object> map, HttpSession httpSession) {
        User user;
        if (userService.searchUser(username)) {
            user = userService.getByUsername(username);
        } else {
            map.put("msg", "用户不存在或密码错误");
            return "signin";//为了防止表单重复提交，可以重定向
        }
        if (!StringUtils.isEmpty(username) && password.equals(user.getPassword())) {
            int user_id = userService.getIdByUsername(username);
            httpSession.setAttribute("loginUser", username);
            httpSession.setAttribute("userID", user_id);
            return "redirect:/search";
        } else {
            map.put("msg", "用户不存在或用户密码错误");
            return "signin";//为了防止表单重复提交，可以重定向
        }

    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpSession httpSession) {
        httpSession.invalidate();
        return "redirect:/search";
    }

    @RequestMapping(value = "/score", method = RequestMethod.POST)
    public String rating(@RequestParam("dsid") int dataset_id,
                         @RequestBody String rating,
                         HttpSession session) {
        if (session.getAttribute("userID") != null) {
            int user_id = Integer.parseInt(session.getAttribute("userID").toString());
            String scoreString = rating.substring(rating.length() - 1);
            if (dataset_id > 311) {
                dataset_id = dataset_id + GlobalVariances.datasetIDGap;
            }
            int scoreNum = 0;
            if (!scoreString.equals(""))
                scoreNum = Integer.parseInt(scoreString);
            Score score;
            if (scoreService.searchScore(user_id, dataset_id)) {
                score = scoreService.getScore(user_id, dataset_id);
                int score_id = score.getScore_id();
                if (scoreNum > 0)
                    scoreService.updateScoreById(score_id, scoreNum);
            } else {
                score = new Score();
                score.setDataset_id(dataset_id);
                score.setUser_id(user_id);
                score.setScore_num(scoreNum);
                if (scoreNum > 0)
                    scoreService.insertScore(score);
            }
            return "redirect:/detail?dsid=" + dataset_id;
        } else {
            return "redirect:/login";
        }
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
