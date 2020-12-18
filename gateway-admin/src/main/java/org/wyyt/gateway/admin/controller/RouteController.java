package org.wyyt.gateway.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.gateway.admin.business.entity.Route;
import org.wyyt.gateway.admin.business.service.RouteService;
import org.wyyt.gateway.admin.entity.WorkingVo;
import org.wyyt.gateway.admin.service.GatewayService;
import org.wyyt.tool.rpc.Result;

import java.util.ArrayList;
import java.util.List;

import static org.wyyt.gateway.admin.controller.RouteController.PREFIX;

/**
 * The controller of table `t_route`
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
public class RouteController {
    public static final String PREFIX = "route";

    private final RouteService routeService;
    private final GatewayService gatewayService;

    public RouteController(final RouteService routeService,
                           final GatewayService gatewayService) {
        this.routeService = routeService;
        this.gatewayService = gatewayService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/%s", PREFIX, "list");
    }


    @GetMapping("toworking")
    public String toWorking() {
        return String.format("%s/%s", PREFIX, "working");
    }

    @GetMapping("toadd")
    public String toAdd(final Model model) {
        model.addAttribute("serviceNames", this.gatewayService.listServiceNames());
        return String.format("%s/%s", PREFIX, "add");
    }

    @GetMapping("toedit")
    public String toEdit(final Model model,
                         @RequestParam(name = "id") final Long id) {
        model.addAttribute("serviceNames", this.gatewayService.listServiceNames());
        model.addAttribute("route", this.routeService.getById(id));
        return String.format("%s/%s", PREFIX, "edit");
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<Route>> list(@RequestParam(value = "page") final Integer pageNum,
                                    @RequestParam(value = "limit") final Integer pageSize,
                                    @RequestParam(value = "routeName", required = false) final String routeName) {
        final IPage<Route> page = this.routeService.page(routeName, pageNum, pageSize);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @PostMapping("listWorking")
    @ResponseBody
    public Result<List<WorkingVo>> listWorking(@RequestParam(value = "page") final Integer pageNum,
                                               @RequestParam(value = "limit") final Integer pageSize,
                                               @RequestParam(value = "routeName", required = false) final String routeName) throws Exception {
        final List<String> list = this.gatewayService.listWorkingRoutes();
        final List<WorkingVo> workingVoList = new ArrayList<>();
        for (final String s : list) {
            final WorkingVo workingVo = new WorkingVo();
            workingVo.setData(s
                    .replaceAll("RouteDefinition", "")
                    .replaceAll("PredicateDefinition", "")
                    .replaceAll("FilterDefinition", ""));
            workingVoList.add(workingVo);
        }
        return Result.ok(workingVoList);
    }

    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "routeId") final String routeId,
                         @RequestParam(value = "routeName") final String routeName,
                         @RequestParam(value = "uri") final String uri,
                         @RequestParam(value = "predicates") final String predicates,
                         @RequestParam(value = "filters") final String filters,
                         @RequestParam(value = "orderNum") final Integer orderNum,
                         @RequestParam(value = "enabled") final Boolean enabled) throws Exception {
        this.routeService.add(routeId, routeName, uri, predicates, filters, orderNum, enabled);
        this.gatewayService.refresh();
        return Result.ok();
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(value = "id") final Long id,
                          @RequestParam(value = "routeName") final String routeName,
                          @RequestParam(value = "uri") final String uri,
                          @RequestParam(value = "predicates") final String predicates,
                          @RequestParam(value = "filters") final String filters,
                          @RequestParam(value = "orderNum") final Integer orderNum,
                          @RequestParam(value = "enabled") final Boolean enabled) throws Exception {
        this.routeService.edit(id, routeName, uri, predicates, filters, orderNum, enabled);
        this.gatewayService.refresh();
        return Result.ok();
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "id") final Long id) throws Exception {
        this.routeService.delete(id);
        this.gatewayService.refresh();
        return Result.ok();
    }

    @PostMapping("enable")
    @ResponseBody
    public Result<?> enable(@RequestParam(value = "id") final Long id) throws Exception {
        this.routeService.enable(id, true);
        this.gatewayService.refresh();
        return Result.ok();
    }

    @PostMapping("disable")
    @ResponseBody
    public Result<?> disable(@RequestParam(value = "id") final Long id) throws Exception {
        this.routeService.enable(id, false);
        this.gatewayService.refresh();
        return Result.ok();
    }

}