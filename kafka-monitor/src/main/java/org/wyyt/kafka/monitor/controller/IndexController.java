package org.wyyt.kafka.monitor.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wyyt.kafka.monitor.entity.vo.AdminVo;
import org.wyyt.kafka.monitor.service.dto.SysAdminService;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.web.Result;

import java.util.Calendar;

/**
 * The controller for providing a home page.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Controller
public class IndexController {
    private final SysAdminService sysAdminService;

    public IndexController(final SysAdminService sysAdminService) {
        this.sysAdminService = sysAdminService;
    }

    @GetMapping(value = {"/", "l"})
    public String toLogin(final Model model) {
        model.addAttribute("version", System.getProperty("version", "BETA"));
        model.addAttribute("year", Calendar.getInstance().get(Calendar.YEAR));
        return "login";
    }

    @GetMapping("index")
    public String toIndex(final Model model,
                          final AdminVo adminVo) {
        model.addAttribute("version", System.getProperty("version", "BETA"));
        model.addAttribute("admin", adminVo);
        return "index";
    }


    @GetMapping("toinfo")
    public String toInfo(final Model model,
                         final AdminVo adminVo) {
        model.addAttribute("admin", this.sysAdminService.getBaseMapper().getById(adminVo.getId()));
        return "info";
    }

    @GetMapping("topassword")
    public String toPassword() {
        return "password";
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
        } catch (Exception e) {
            return Result.error(String.format("账号或密码错误. 原因:%s", ExceptionTool.getRootCauseMessage(e)));
        }
    }

    @PostMapping("repwd")
    @ResponseBody
    public Result<?> repwd(final AdminVo adminVo,
                           @RequestParam(name = "oldPassword") final String oldPassword,
                           @RequestParam(name = "password") final String password) {
        if (sysAdminService.changePassword(adminVo.getId(), oldPassword, password)) {
            return Result.success();
        }
        return Result.error("密码修改失败");
    }

    @PostMapping("reinfo")
    @ResponseBody
    public Result<?> reinfo(final AdminVo adminVo,
                            @RequestParam(name = "name") final String name,
                            @RequestParam(name = "gender") final Boolean gender,
                            @RequestParam(name = "phoneNumber") final String phoneNumber,
                            @RequestParam(name = "email") final String email,
                            @RequestParam(name = "remark") final String remark) {
        this.sysAdminService.updateInfo(adminVo.getId(), name, gender, phoneNumber, email, remark);
        return Result.success();
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