package com.datasetretrievalwithlucene.demo.Controller;

import com.datasetretrievalwithlucene.demo.Bean.*;
import com.datasetretrievalwithlucene.demo.Service.*;
import com.datasetretrievalwithlucene.demo.util.GlobalVariances;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
public class RatingController {
    private final QueryDataService queryDataService;
    private final UserService userService;
    private final AnnotationService annotationService;
    private final DatasetService datasetService;
    private final QueryService queryService;
    private int userNumber;
    private int queryNumber;
    private int queryDataNumber;

    public RatingController(UserService userService, QueryDataService queryDataService, AnnotationService annotationService, DatasetService datasetService, QueryService queryService) {
        this.userService = userService;
        this.queryDataService = queryDataService;
        this.annotationService = annotationService;
        this.datasetService = datasetService;
        this.queryService = queryService;
        userNumber = userService.getUserCount();
        queryNumber = queryService.getQueryCount();
        queryDataNumber = queryDataService.getQueryDataCount();
    }

    @RequestMapping("/login")
    public String login() {
        return "signin";
    }

    @RequestMapping(value = "/signin", method = RequestMethod.POST)
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        Map<String, Object> map, HttpSession httpSession, Model model) {
        User user;
        if (userService.searchUser(username)) {
            user = userService.getByUsername(username);
        } else {
            map.put("msg", "用户不存在或密码错误");
            return "signin";//为了防止表单重复提交，可以重定向
        }
        if (!StringUtils.isEmpty(username) && password.equals(user.getPassword())) {
            httpSession.setAttribute("loginUser", username);
            int qdid;
            if (user.getLast_annotation_id() == 0) {
                int first_query_id = (user.getUser_id() - 1) * (queryNumber / userNumber) + 1;
                qdid = queryDataService.getMinQueryDataIdByQueryId(first_query_id);
            }
            else
                qdid = user.getLast_annotation_id();
            return "redirect:/dashboard?qdid=" + qdid + "&userid=" + user.getUser_id();
        } else {
            map.put("msg", "用户不存在或用户密码错误");
            return "signin";//为了防止表单重复提交，可以重定向
        }

    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam("qdid") int query_data_id,
                            @RequestParam("userid") int user_id,
                            Model model) {

//        System.out.println(queryDataNumber);
//        System.out.println(queryNumber);
//        System.out.println(userNumber);
        QueryData queryData = queryDataService.getQueryDataById(query_data_id);
        int query_id = queryData.getQuery_id();
        Query query = queryService.getQueryById(query_id);
        int dataset_id = queryData.getDataset_id();
        Dataset dataset = datasetService.getByDatasetId(dataset_id);
        int first_query_id = (user_id - 1) * (queryNumber / userNumber) + 1;
        int last_query_id = (user_id - 1 + GlobalVariances.annotatorPerPair) * (queryNumber / userNumber);
        if (last_query_id > queryNumber) {
            last_query_id -= queryNumber;
        }
//        System.out.println("first_query_id: " + first_query_id);
//        System.out.println("last_query_id: " + last_query_id);

        int first_query_data_id = queryDataService.getMinQueryDataIdByQueryId(first_query_id);
        int last_query_data_id = queryDataService.getMaxQueryDataIdByQueryId(last_query_id);
//        System.out.println("first_query_data_id: " + first_query_data_id);
//        System.out.println("last_query_data_id: " + last_query_data_id);


        int current_number = (query_data_id + queryDataNumber - first_query_data_id) % queryDataNumber + 1;
        int total_number = 0;
        for (int i = 0; i < GlobalVariances.annotatorPerPair * (queryNumber / userNumber) ; i++) {
            int tmp = i + first_query_id;
            if (tmp > queryNumber) tmp -= queryNumber;
//            System.out.println(tmp);
            List<Integer> tmpList = queryDataService.getQueryDataIdByQueryId(tmp);
            total_number += tmpList.size();
        }

        int previous_id;
        if (query_data_id == first_query_data_id) {
            previous_id = query_data_id;
        } else if (query_data_id == 1) {
            previous_id = queryDataNumber;
        } else {
            previous_id = query_data_id - 1;
        }
        int next_id;
        if (query_data_id == last_query_data_id) {
            next_id = query_data_id;
        } else if (query_data_id == queryDataNumber){
            next_id = 1;
        } else {
            next_id = query_data_id + 1;
        }
        int score = -1;
        String reason = "";
        Annotation annotation;
        if (annotationService.searchAnnotation(user_id, query_id, dataset_id)) {
            annotation = annotationService.getAnnotation(user_id, query_id, dataset_id);
            score = annotation.getRating();
            reason = annotation.getReason();
        }
        userService.updateLastIdByUserId(user_id, query_data_id);

        model.addAttribute("qdid", query_data_id);
        model.addAttribute("previous_id", previous_id);
        model.addAttribute("next_id", next_id);
        model.addAttribute("total", total_number);
        model.addAttribute("current", current_number);
        model.addAttribute("query", query);
        model.addAttribute("dataset", dataset);
        model.addAttribute("userid", user_id);
        model.addAttribute("score", score);
        model.addAttribute("reason", reason);
        model.addAttribute("detailURL", GlobalVariances.detailPageURL);
        return "ratingdashboard";
    }

    @RequestMapping(value = "/rating", method = RequestMethod.POST)
    @ResponseBody
    public void rating(@RequestParam("qid") int query_id,
                       @RequestParam("dsid") int dataset_id,
                       @RequestParam("userid") int user_id, @RequestBody String rating) {
        String scoreString = rating.substring(rating.length() - 1);
        int score = -1;
        if (scoreString != "")
            score = Integer.parseInt(scoreString);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.now();
        String annotation_time = dateTime.format(formatter);
        Annotation annotation;
        if (annotationService.searchAnnotation(user_id, query_id, dataset_id)) {
            annotation = annotationService.getAnnotation(user_id, query_id, dataset_id);
            int annotation_id = annotation.getAnnotation_id();
            if (score > 0)
                annotationService.updateRatingById(annotation_id, score, annotation_time);
        } else {
            annotation = new Annotation();
            annotation.setUser_id(user_id);
            annotation.setQuery_id(query_id);
            annotation.setDataset_id(dataset_id);
            annotation.setRating(score);
            annotation.setReason("");
            annotation.setAnnotation_time(annotation_time);
            if (score > 0)
                annotationService.insertAnnotation(annotation);
        }
    }

    @RequestMapping(value = "/commitreason", method = RequestMethod.POST)
    public String commitReason(@RequestParam("qid") int query_id,
                       @RequestParam("dsid") int dataset_id,
                       @RequestParam("userid") int user_id, @RequestParam("reason") String reason) {
        if (reason == null)
            reason = "";
        Annotation annotation;
        if (annotationService.searchAnnotation(user_id, query_id, dataset_id)) {
            annotation = annotationService.getAnnotation(user_id, query_id, dataset_id);
            int annotation_id = annotation.getAnnotation_id();
            annotationService.updateReasonById(annotation_id, reason);
        } else {
            annotation = new Annotation();
            annotation.setUser_id(user_id);
            annotation.setQuery_id(query_id);
            annotation.setDataset_id(dataset_id);
            annotation.setReason(reason);
            annotationService.insertAnnotation(annotation);
        }
        System.out.println(reason);
        int qdid = queryDataService.getQueryDataIdByQueryIdAndDatasetId(query_id, dataset_id);
        return "redirect:/dashboard?qdid=" + qdid + "&userid=" + user_id;
    }

    @GetMapping(value = "/logout/{username}")
    public String logout(HttpSession httpSession, @PathVariable("username") String username, Model model) {
        httpSession.removeAttribute(username);
        httpSession.invalidate();
        model.addAttribute("msg", "注销成功");
        return "signin";
    }
    /*@RequestMapping("/dashboard")
    public String testdashboard(Model model) {
        List<QueryData> queryDataList = queryDataService.getAll();
        model.addAttribute("queryDataList", queryDataList);
        return "dashboard";
    }
    @RequestMapping("/testPython")
    @ResponseBody
    public String testPython() {
        Process proc = null;
        try {
            String a = "111";
            String b = "222";
            String c = "333";
            String d = "444";
            String[] argv = new String[] { "python", "G:\\DatasetRetrievalwithLucene\\src\\main\\resources\\py\\test.py", a, b, c, d };
            proc = Runtime.getRuntime().exec(argv);
            System.err.println("proc:"+proc);
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(),"GBK"));
            System.err.println("in:"+in);
            String line;
            System.err.println(in.lines().count());
            while ((line = in.readLine()) != null) {
                System.out.println("line:"+line);
            }
            in.close();
            proc.waitFor();
            System.out.println("end");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "success";
    }*/

}
