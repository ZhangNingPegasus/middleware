package org.wyyt.kafka.monitor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.kafka.monitor.entity.dto.SysDingDingConfig;
import org.wyyt.kafka.monitor.service.dto.SysDingDingConfigService;
import org.wyyt.tool.date.DateTool;
import org.wyyt.tool.dingtalk.DingTalkTool;
import org.wyyt.tool.dingtalk.Message;
import org.wyyt.tool.rpc.Result;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * The controller for providing a UI for setting the dingding's configuration.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(DingDingConfigController.PREFIX)
public class DingDingConfigController {
    public static final String PREFIX = "dingdingconfig";
    private final SysDingDingConfigService sysDingDingConfigService;

    public DingDingConfigController(final SysDingDingConfigService sysDingDingConfigService) {
        this.sysDingDingConfigService = sysDingDingConfigService;
    }

    @GetMapping("tolist")
    public String toList(final Model model) {
        List<SysDingDingConfig> sysDingDingConfigList = this.sysDingDingConfigService.list();
        if (null != sysDingDingConfigList && !sysDingDingConfigList.isEmpty()) {
            final SysDingDingConfig sysDingDingConfig = sysDingDingConfigList.get(0);
            model.addAttribute("config", sysDingDingConfig);
        } else {
            model.addAttribute("config", new SysDingDingConfig());
        }
        return String.format("%s/list", PREFIX);
    }

    @GetMapping("totest")
    public String toTest() {
        return String.format("%s/test", PREFIX);
    }

    @PostMapping("save")
    @ResponseBody
    public Result<Integer> save(@RequestParam(name = "accesstoken") final String accesstoken,
                                @RequestParam(name = "secret") final String secret) {
        int result = this.sysDingDingConfigService.save(accesstoken, secret);
        return Result.ok(result);
    }

    @PostMapping("test")
    @ResponseBody
    public Result<?> test(@RequestParam(name = "content", defaultValue = "") final String content,
                          @RequestParam(name = "atMobiles", defaultValue = "") final String atMobiles,
                          @RequestParam(name = "isAtAll", defaultValue = "false") final Boolean isAtAll) throws Exception {
        final SysDingDingConfig sysDingDingConfig = this.sysDingDingConfigService.get();
        Message message = new Message();
        message.setMsgtype("text");
        message.setText(new Message.Text("告警主机：" + InetAddress.getLocalHost().getHostName() + "\n" +
                "主机地址：" + InetAddress.getLocalHost().getHostAddress() + "\n" +
                "告警等级：正常\n" +
                "当前状态：OK\n" +
                "问题详情：" + content + "\n" +
                "告警时间：" + DateTool.format(new Date()) + "\n"));
        message.setAt(new Message.At(Arrays.asList(atMobiles.split(",")), isAtAll));
        DingTalkTool.send(message, sysDingDingConfig.getAccessToken(), sysDingDingConfig.getSecret());
        return Result.ok();
    }
}