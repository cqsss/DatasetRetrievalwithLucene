package com.datasetretrievalwithlucene.demo.Controller;

import com.datasetretrievalwithlucene.demo.Bean.QueryData;
import com.datasetretrievalwithlucene.demo.Bean.User;
import com.datasetretrievalwithlucene.demo.Service.QueryDataService;
import com.datasetretrievalwithlucene.demo.Service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
public class RatingController {
    private final QueryDataService queryDataService;
    private final UserService userService;

    public RatingController(UserService userService, QueryDataService queryDataService) {
        this.userService = userService;
        this.queryDataService = queryDataService;
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
            int qid = user.getLast_annotation_id();
            return "redirect:/dashboard?qid=" + qid + "&username=" + username;
        } else {
            map.put("msg","用户不存在或用户密码错误");
            return "signin";//为了防止表单重复提交，可以重定向
        }

    }
    @GetMapping("/dashboard")
    public String dashboard(@RequestParam("qid") int query_data_id,
                            @RequestParam("username") String username,
                            Model model) {
        QueryData queryData = queryDataService.getById(query_data_id);
        String query = queryData.getQuery();
        int dataset_id = queryData.getQuery_data_id();
        int max_id = queryDataService.getMaxId();
        if (query_data_id > max_id) {
            model.addAttribute("msg", "最后一个");
        }
        if (query_data_id < 1) {
            model.addAttribute("msg", "第一个");
        }
        int previous_id = Math.max(query_data_id-1,1);
        int next_id = Math.min(query_data_id+1,max_id);
        model.addAttribute("previous_id", previous_id);
        model.addAttribute("next_id", next_id);
        model.addAttribute("query", query);
        model.addAttribute("dataset_id", dataset_id);
        model.addAttribute("username", username);
        return "dashboard";
    }
    @RequestMapping(value = "/rating", method = RequestMethod.POST)
    public void rating(@RequestParam("rating") String rating) {
        System.out.println(rating);
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
    }*/

}
