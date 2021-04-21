package com.datasetretrievalwithlucene.demo.Controller;

import com.datasetretrievalwithlucene.demo.Bean.User;
import com.datasetretrievalwithlucene.demo.Service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @RequestMapping("/register")
    public String register() {
        return "register";
    }

    @RequestMapping(value = "/doregister", method = RequestMethod.GET)
    public String doregister(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        @RequestParam("confirmPassword") String confirm_password,
                        Map<String, Object> map, HttpSession httpSession, Model model) {
        if(username.isEmpty()){
            map.put("msg","用户名不能为空");
            return "register";
        }
        else if(userService.searchUser(username)){
            map.put("msg","该用户已存在");
            return "register";
        }
        else if (!password.equals(confirm_password)){
            map.put("msg","两次密码输入不一致");
            return  "register";
        }
        else if (password.length()<6||password.length()>50){
            map.put("msg","密码长度至少6位,至多50位");
            return  "register";
        }
        else {
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            userService.insertUser(user);
            map.put("msg","注册成功！");
            return  "redirect:/search";
        }

    }
}
