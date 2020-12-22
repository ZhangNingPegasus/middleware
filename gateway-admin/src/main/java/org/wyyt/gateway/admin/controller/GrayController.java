package org.wyyt.gateway.admin.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.gateway.admin.entity.GrayVo;
import org.wyyt.gateway.admin.entity.InspectVo;
import org.wyyt.gateway.admin.entity.ServiceVo;
import org.wyyt.gateway.admin.service.GatewayService;
import org.wyyt.gateway.admin.service.GrayPublishService;
import org.wyyt.tool.rpc.Result;

import java.util.List;

import static org.wyyt.gateway.admin.controller.GrayController.PREFIX;

/**
 * The controller of gray publish
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public class GrayController {
    public static final String PREFIX = "gray";
    private final GatewayService gatewayService;
    private final GrayPublishService grayPublishService;

    public GrayController(final GatewayService gatewayService,
                          final GrayPublishService grayPublishService) {
        this.gatewayService = gatewayService;
        this.grayPublishService = grayPublishService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("toadd")
    public String toAdd(final Model model) {
        return String.format("%s/%s", PREFIX, "add");
    }

    @GetMapping("toinspect")
    public String toInspect(final Model model,
                            @RequestParam("data") String data) {
        model.addAttribute("data", data);
        return String.format("%s/%s", PREFIX, "inspect");
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<GrayVo>> list() {
        return Result.ok(this.grayPublishService.listGrayVo());
    }

    @PostMapping("listServices")
    @ResponseBody
    public Result<List<ServiceVo>> listServices() throws Exception {
        return Result.ok(this.gatewayService.listService());
    }

    @PostMapping("inspect")
    @ResponseBody
    public Result<String> inspect(@RequestParam("data") String data) throws Exception {
        final List<InspectVo> inspectVos = JSON.parseArray(data, InspectVo.class);
        return Result.ok(this.grayPublishService.inspect(inspectVos));
    }

}