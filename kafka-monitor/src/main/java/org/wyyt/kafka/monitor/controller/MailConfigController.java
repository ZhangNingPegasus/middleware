package org.wyyt.kafka.monitor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.kafka.monitor.entity.dto.SysMailConfig;
import org.wyyt.kafka.monitor.service.common.MailService;
import org.wyyt.kafka.monitor.service.dto.SysMailConfigService;
import org.wyyt.tool.web.Result;

import java.util.List;

/**
 * The controller for providing the UI used for setting the emails' configuration.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(MailConfigController.PREFIX)
public class MailConfigController {
    public static final String PREFIX = "mailconfig";
    private final SysMailConfigService sysMailConfigService;
    private final MailService mailService;

    public MailConfigController(final SysMailConfigService sysMailConfigService,
                                final MailService mailService) {
        this.sysMailConfigService = sysMailConfigService;
        this.mailService = mailService;
    }

    @GetMapping("tolist")
    public String toList(final Model model) {
        final List<SysMailConfig> sysMailConfigList = sysMailConfigService.list();
        if (null != sysMailConfigList && !sysMailConfigList.isEmpty()) {
            final SysMailConfig sysMailConfig = sysMailConfigList.get(0);
            model.addAttribute("config", sysMailConfig);
        } else {
            model.addAttribute("config", new SysMailConfig());
        }
        return String.format("%s/list", PREFIX);
    }

    @GetMapping("totest")
    public String toTest() {
        return String.format("%s/test", PREFIX);
    }

    @PostMapping("save")
    @ResponseBody
    public Result<Integer> save(@RequestParam(name = "host") final String host,
                                @RequestParam(name = "port") final String port,
                                @RequestParam(name = "username") final String username,
                                @RequestParam(name = "password") final String password) {
        final int result = this.sysMailConfigService.save(host, port, username, password);
        return Result.success(result);
    }

    @PostMapping("test")
    @ResponseBody
    public Result<?> test(@RequestParam(name = "to") final String to,
                          @RequestParam(name = "subject") final String subject,
                          @RequestParam(name = "html") final String html) throws Exception {
        this.mailService.send(to, subject, html);
        return Result.success();
    }

}
