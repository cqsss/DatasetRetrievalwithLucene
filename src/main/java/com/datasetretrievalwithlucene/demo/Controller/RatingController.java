package com.datasetretrievalwithlucene.demo.Controller;

import com.datasetretrievalwithlucene.demo.Bean.User;
import com.datasetretrievalwithlucene.demo.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Controller
public class RatingController {
    private final UserService userService;

    public RatingController(UserService userService) {
        this.userService = userService;
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
        if(userService.searchUser(username)){
            user = userService.getByUsername(username);
        }
        else {
            map.put("msg","用户不存在或密码错误");
            return "signin";//为了防止表单重复提交，可以重定向
        }
        if(!StringUtils.isEmpty(username)&&password.equals(user.getPassword())){
            httpSession.setAttribute("loginUser",username);
            return "redirect:/dashboard";
        }
        else{
            map.put("msg","用户不存在或用户密码错误");
            return "signin";//为了防止表单重复提交，可以重定向
        }

    }
    @GetMapping(value = "/logout/{username}")
    public String logout(HttpSession httpSession, @PathVariable("username") String username, Model model){
        httpSession.removeAttribute(username);
        httpSession.invalidate();
        model.addAttribute("msg","注销成功");
        return  "signin";
    }
    @RequestMapping("/dashboard")
    public String testdashboard() {
        return "dashboard";
    }
    @RequestMapping("/dashboard?q={query}&dsid={dataset_id}")
    public String dashboard(@PathVariable("query") String query, @PathVariable("dataset_id") int dataset_id, Model model) {
        model.addAttribute(query);
        model.addAttribute(dataset_id);
        return "dashboard";
    }
}
