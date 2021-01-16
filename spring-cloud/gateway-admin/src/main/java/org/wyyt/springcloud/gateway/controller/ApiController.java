package org.wyyt.springcloud.gateway.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.springcloud.gateway.entity.entity.Api;
import org.wyyt.springcloud.gateway.service.ApiServiceImpl;
import org.wyyt.springcloud.gateway.service.GatewayService;
import org.wyyt.tool.rpc.Result;

import java.util.List;

import static org.wyyt.springcloud.gateway.controller.ApiController.PREFIX;


/**
 * The controller of Api
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public class ApiController {
    public static final String PREFIX = "api";
    private final GatewayService gatewayService;
    private final ApiServiceImpl apiServiceImpl;

    public ApiController(final GatewayService gatewayService,
                         final ApiServiceImpl apiServiceImpl) {
        this.gatewayService = gatewayService;
        this.apiServiceImpl = apiServiceImpl;
    }

    @GetMapping("tolist")
    public String toList(final Model model) {
        model.addAttribute("serviceIds", this.gatewayService.listServiceIds());
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("toadd")
    public String toAdd(final Model model) {
        model.addAttribute("serviceIds", this.gatewayService.listServiceIds());
        return String.format("%s/%s", PREFIX, "add");
    }

    @GetMapping("toedit")
    public String toEdit(final Model model,
                         @RequestParam(value = "id") final Long id) {
        model.addAttribute("api", this.apiServiceImpl.getById(id));
        model.addAttribute("serviceIds", this.gatewayService.listServiceIds());
        return String.format("%s/%s", PREFIX, "edit");
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<Api>> list(@RequestParam(value = "page") final Integer pageNum,
                                  @RequestParam(value = "limit") final Integer pageSize,
                                  @RequestParam(value = "name", required = false) final String name,
                                  @RequestParam(value = "serviceId", required = false) final String serviceId,
                                  @RequestParam(value = "path", required = false) final String path) {
        final IPage<Api> page = this.apiServiceImpl.page(pageNum, pageSize, name, serviceId, path);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "name") final String name,
                         @RequestParam(value = "method") final String method,
                         @RequestParam(value = "serviceId") final String serviceId,
                         @RequestParam(value = "path") final String path) {
        this.apiServiceImpl.add(name, method, serviceId, path);
        return Result.ok();
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(value = "id") final Long id,
                          @RequestParam(value = "name") final String name,
                          @RequestParam(value = "method") final String method,
                          @RequestParam(value = "serviceId") final String serviceId,
                          @RequestParam(value = "path") final String path) {
        this.apiServiceImpl.edit(id, name, method, serviceId, path);
        return Result.ok();
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "id") final Long id) throws Exception {
        this.apiServiceImpl.del(id);
        return Result.ok();
    }
}