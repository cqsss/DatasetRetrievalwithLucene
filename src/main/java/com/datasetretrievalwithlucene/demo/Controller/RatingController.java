package com.datasetretrievalwithlucene.demo.Controller;

import com.datasetretrievalwithlucene.demo.Bean.*;
import com.datasetretrievalwithlucene.demo.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
public class RatingController {
    private final QueryDataService queryDataService;
    private final UserService userService;
    private final AnnotationService annotationService;
    private final DatasetService datasetService;
    private final QueryService queryService;

    public RatingController(UserService userService, QueryDataService queryDataService, AnnotationService annotationService, DatasetService datasetService, QueryService queryService) {
        this.userService = userService;
        this.queryDataService = queryDataService;
        this.annotationService = annotationService;
        this.datasetService = datasetService;
        this.queryService = queryService;
    }

    @RequestMapping("/login")
    public String login() {
        return "signin";
    }

    @RequestMapping(value = "/signin",method = RequestMethod.POST)
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        Map<String,Object> map, HttpSession httpSession, Model model){
        User user=new User();
        if (userService.searchUser(username)){
            user = userService.getByUsername(username);
        } else {
            map.put("msg","用户不存在或密码错误");
            return "signin";//为了防止表单重复提交，可以重定向
        }
        if (!StringUtils.isEmpty(username) && password.equals(user.getPassword())){
            httpSession.setAttribute("loginUser",username);
            int qdid = user.getLast_annotation_id();
            return "redirect:/dashboard?qdid=" + qdid + "&username=" + username;
        } else {
            map.put("msg","用户不存在或用户密码错误");
            return "signin";//为了防止表单重复提交，可以重定向
        }

    }
    @GetMapping("/dashboard")
    public String dashboard(@RequestParam("qdid") int query_data_id,
                            @RequestParam("username") String username,
                            Model model) {
        QueryData queryData = queryDataService.getQueryDataById(query_data_id);
        int query_id = queryData.getQuery_id();
        Query query = queryService.getQueryById(query_id);
        int dataset_id = queryData.getDataset_id();
        Dataset dataset = datasetService.getByDatasetId(dataset_id);
        int user_id = userService.getIdByUsername(username);
        int max_id = queryDataService.getMaxId();
        if (query_data_id > max_id) {
            model.addAttribute("msg", "最后一个");
        }
        if (query_data_id < 1) {
            model.addAttribute("msg", "第一个");
        }
        int previous_id = Math.max(query_data_id-1,1);
        int next_id = Math.min(query_data_id+1,max_id);
        int score = -1;
        Annotation annotation;
        if(annotationService.searchAnnotation(user_id, query_id, dataset_id)) {
            annotation = annotationService.getAnnotation(user_id, query_id, dataset_id);
            score = annotation.getRating();
        }
        userService.updateLastIdByUserId(user_id, query_data_id);

        model.addAttribute("qdid", query_data_id);
        model.addAttribute("previous_id", previous_id);
        model.addAttribute("next_id", next_id);
        model.addAttribute("max_id", max_id);
        model.addAttribute("query", query);
        model.addAttribute("dataset", dataset);
        model.addAttribute("username", username);
        model.addAttribute("score", score);
        return "dashboard";
    }
    @RequestMapping(value = "/rating", method = RequestMethod.POST)
    @ResponseBody
    public void rating(@RequestParam("qid")int query_id,
                       @RequestParam("dsid")int dataset_id,
                       @RequestParam("username")String username, @RequestBody String rating) {
        String scoreString = rating.substring(rating.length()-1);
        int score = -1;
        if(scoreString!="")
            score = Integer.parseInt(scoreString);
        int user_id = userService.getIdByUsername(username);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.now();
        String annotation_time = dateTime.format(formatter);
        Annotation annotation;
        if(annotationService.searchAnnotation(user_id, query_id, dataset_id)) {
            annotation = annotationService.getAnnotation(user_id, query_id, dataset_id);
            int annotation_id = annotation.getAnnotation_id();
            if(score > 0)
                annotationService.updateRatingById(annotation_id, score, annotation_time);
        } else {
            annotation = new Annotation();
            annotation.setUser_id(user_id);
            annotation.setQuery_id(query_id);
            annotation.setDataset_id(dataset_id);
            annotation.setRating(score);
            annotation.setAnnotation_time(annotation_time);
            if(score > 0)
                annotationService.insertAnnotation(annotation);
        }
    }
    @GetMapping(value = "/logout/{username}")
    public String logout(HttpSession httpSession, @PathVariable("username") String username, Model model){
        httpSession.removeAttribute(username);
        httpSession.invalidate();
        model.addAttribute("msg","注销成功");
        return  "signin";
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
