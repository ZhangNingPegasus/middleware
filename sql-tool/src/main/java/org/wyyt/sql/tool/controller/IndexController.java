package org.wyyt.sql.tool.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.sql.tool.entity.vo.AdminVo;
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
public final class IndexController {

    @GetMapping(value = {"/", "l"})
    public final String toLogin(final Model model) {
        model.addAttribute("version", System.getProperty("version", "BETA"));
        model.addAttribute("year", Calendar.getInstance().get(Calendar.YEAR));
        return "login";
    }

    @RequestMapping(value = "index")
    public final String toIndex(final Model model,
                                final AdminVo adminVo) {
        model.addAttribute("version", System.getProperty("version", "BETA"));
        model.addAttribute("admin", adminVo);
        return "index";
    }

    @PostMapping("login")
    @ResponseBody
    public final Result<?> login(@RequestParam(name = "username") final String username,
                                 @RequestParam(name = "password") final String password,
                                 @RequestParam(name = "remember", defaultValue = "false") final Boolean remember) {
        try {
            final Subject subject = SecurityUtils.getSubject();
            UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(username, password);
            usernamePasswordToken.setRememberMe(remember);
            subject.login(usernamePasswordToken);
            return Result.success();
        } catch (Exception e) {
            return Result.error(String.format("账号或密码错误, %s", e.getMessage()));
        }
    }

    @PostMapping("quit")
    @ResponseBody
    public final Result<?> quit() {
        final Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            subject.logout();
        }
        return Result.success();
    }
}