package org.wyyt.springcloud.gateway.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.wyyt.springcloud.gateway.entity.GrayVo;
import org.wyyt.springcloud.gateway.entity.InspectVo;
import org.wyyt.springcloud.gateway.entity.ServiceVo;
import org.wyyt.springcloud.gateway.service.ConsulService;
import org.wyyt.springcloud.gateway.service.GrayPublishService;
import org.wyyt.tool.rpc.Result;

import javax.servlet.http.HttpSession;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.wyyt.springcloud.gateway.controller.GrayController.PREFIX;

/**
 * The controller of gray publish
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public class GrayController {
    private static final String SESSION_EDIT_DATA = "SESSION_EDIT_DATA";
    public static final String PREFIX = "gray";
    private final ConsulService consulService;
    private final GrayPublishService grayPublishService;

    public GrayController(final ConsulService consulService,
                          final GrayPublishService grayPublishService) {
        this.consulService = consulService;
        this.grayPublishService = grayPublishService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("toadd")
    public String toAdd() {
        return String.format("%s/%s", PREFIX, "add");
    }

    @GetMapping("toedit")
    public String toEdit(final HttpSession session,
                         final Model model) {
        model.addAttribute("gray", session.getAttribute(SESSION_EDIT_DATA));
        session.removeAttribute(SESSION_EDIT_DATA);
        return String.format("%s/%s", PREFIX, "edit");
    }

    @GetMapping("toinspect")
    public String toInspect(final Model model,
                            @RequestParam("data") String data) {
        model.addAttribute("data", data);
        return String.format("%s/%s", PREFIX, "inspect");
    }

    @GetMapping("toglobalinspect")
    public String toGlobalInspect() {
        return String.format("%s/%s", PREFIX, "globalinspect");
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<GrayVo>> list() throws Exception {
        return Result.ok(this.grayPublishService.listGrayVo());
    }

    @PostMapping("listServices")
    @ResponseBody
    public Result<List<ServiceVo>> listServices(@RequestParam(value = "value", required = false) String value) {
        final List<ServiceVo> result = this.consulService.listService();
        if (!ObjectUtils.isEmpty(value)) {
            final Map<String, String> map = JSON.parseObject(value, Map.class);
            for (final ServiceVo serviceVo : result) {
                serviceVo.setVersion(map.get(serviceVo.getName()));
            }
        }
        return Result.ok(result);
    }

    @PostMapping("inspect")
    @ResponseBody
    public Result<String> inspect(@RequestParam("data") String data) throws URISyntaxException {
        final List<InspectVo> inspectVos = JSON.parseArray(data, InspectVo.class);
        return Result.ok(this.grayPublishService.inspect(inspectVos));
    }

    @PostMapping("globalinspect")
    @ResponseBody
    public Result<String> globalInspect() throws Exception {
        return Result.ok(this.grayPublishService.globalInspect());
    }


    @PostMapping("seteditdata")
    @ResponseBody
    public Result<?> setEditData(final HttpSession session,
                                 @RequestParam("data") String data) {
        session.setAttribute(SESSION_EDIT_DATA, JSON.parseObject(data, GrayVo.class));
        return Result.ok();
    }

    @PostMapping("publish")
    @ResponseBody
    public Result<?> publish(@RequestParam("data") String data) throws Exception {
        final List<GrayVo> grayVoList = JSON.parseArray(data, GrayVo.class);
        this.grayPublishService.publish(grayVoList);
        return Result.ok();
    }
}