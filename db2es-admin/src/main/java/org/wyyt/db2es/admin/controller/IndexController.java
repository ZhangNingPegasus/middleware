package org.wyyt.db2es.admin.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.db2es.admin.entity.vo.AdminVo;
import org.wyyt.db2es.core.util.CommonUtils;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.web.Result;

import java.util.Calendar;

/**
 * The controller of index page
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Controller
public class IndexController {
    @GetMapping(value = {"/", "l"})
    public String toLogin(final Model model) {
        model.addAttribute("version", CommonUtils.getVersion());
        model.addAttribute("year", Calendar.getInstance().get(Calendar.YEAR));
        return "login";
    }

    @RequestMapping(value = "index")
    public String toIndex(final Model model,
                          final AdminVo adminVo) {
        model.addAttribute("version", CommonUtils.getVersion());
        model.addAttribute("admin", adminVo);
        return "index";
    }

    @PostMapping("login")
    @ResponseBody
    public Result<?> login(@RequestParam(name = "username") final String username,
                           @RequestParam(name = "password") final String password,
                           @RequestParam(name = "remember", defaultValue = "false") final Boolean remember) {
        try {
            final Subject subject = SecurityUtils.getSubject();
            final UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(username, password);
            usernamePasswordToken.setRememberMe(remember);
            subject.login(usernamePasswordToken);
            return Result.success();
        } catch (final Exception e) {
            return Result.error(String.format("账号或密码错误. 原因:%s", ExceptionTool.getRootCauseMessage(e)));
        }
    }


    @PostMapping("quit")
    @ResponseBody
    public Result<?> quit() {
        final Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            subject.logout();
        }
        return Result.success();
    }
}