package org.wyyt.admin.ui.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.admin.ui.common.Utils;
import org.wyyt.admin.ui.entity.vo.AdminVo;
import org.wyyt.admin.ui.service.SysAdminService;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.rpc.Result;

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
    private final SysAdminService sysAdminService;

    public IndexController(final SysAdminService sysAdminService) {
        this.sysAdminService = sysAdminService;
    }

    @GetMapping(value = {"/", "l"})
    public String toLogin(final Model model) {
        model.addAttribute("version", Utils.getVersion());
        model.addAttribute("year", Calendar.getInstance().get(Calendar.YEAR));
        return "login";
    }

    @RequestMapping(value = "index")
    public String toIndex(final Model model,
                          final AdminVo adminVo) {
        model.addAttribute("version", Utils.getVersion());
        model.addAttribute("admin", adminVo);
        return "index";
    }

    @GetMapping("toinfo")
    public String toInfo(final Model model,
                         final AdminVo adminVo) throws Exception {
        model.addAttribute("admin", this.sysAdminService.getById(adminVo.getId()));
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
            return Result.ok();
        } catch (final UnknownAccountException e) {
            return Result.error("无效的用户名或密码");
        } catch (final Exception e) {
            return Result.error(String.format("账号或密码错误. 原因:%s", ExceptionTool.getRootCauseMessage(e)));
        }
    }

    @PostMapping("repwd")
    @ResponseBody
    public Result<?> repwd(final AdminVo adminVo,
                           @RequestParam(name = "oldPassword") final String oldPassword,
                           @RequestParam(name = "password") final String password) throws Exception {
        if (sysAdminService.changePassword(adminVo.getId(), oldPassword, password)) {
            return Result.ok();
        }
        return Result.error("密码修改失败");
    }

    @PostMapping("reinfo")
    @ResponseBody
    public Result<?> reinfo(final AdminVo adminVo,
                            @RequestParam(name = "name") final String name,
                            @RequestParam(name = "phoneNumber") final String phoneNumber,
                            @RequestParam(name = "email") final String email,
                            @RequestParam(name = "remark") final String remark) throws Exception {
        this.sysAdminService.updateInfo(adminVo.getId(), name, phoneNumber, email, remark);
        return Result.ok();
    }


    @PostMapping("quit")
    @ResponseBody
    public Result<?> quit() {
        final Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            subject.logout();
        }
        return Result.ok();
    }
}