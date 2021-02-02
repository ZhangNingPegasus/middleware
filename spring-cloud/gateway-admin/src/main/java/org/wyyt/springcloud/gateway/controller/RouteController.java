package org.wyyt.springcloud.gateway.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.springcloud.gateway.entity.WorkingVo;
import org.wyyt.springcloud.gateway.entity.entity.Route;
import org.wyyt.springcloud.gateway.entity.service.RouteService;
import org.wyyt.springcloud.gateway.service.ConsulService;
import org.wyyt.springcloud.gateway.service.GatewayService;
import org.wyyt.tool.common.CommonTool;
import org.wyyt.tool.rpc.Result;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.wyyt.springcloud.gateway.controller.RouteController.PREFIX;

/**
 * The controller of table `t_route`
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
public class RouteController {
    public static final String PREFIX = "route";

    private final RouteService routeService;
    private final ConsulService consulService;
    private final GatewayService gatewayService;

    public RouteController(final RouteService routeService,
                           final ConsulService consulService,
                           final GatewayService gatewayService) {
        this.routeService = routeService;
        this.consulService = consulService;
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
        model.addAttribute("serviceNames", this.consulService.listServiceNames());
        return String.format("%s/%s", PREFIX, "add");
    }

    @GetMapping("toedit")
    public String toEdit(final Model model,
                         @RequestParam(name = "id") final Long id) {
        model.addAttribute("serviceNames", this.consulService.listServiceNames());
        model.addAttribute("route", this.routeService.getById(id));
        return String.format("%s/%s", PREFIX, "edit");
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<Route>> list(@RequestParam(value = "page") final Integer pageNum,
                                    @RequestParam(value = "limit") final Integer pageSize,
                                    @RequestParam(value = "description", required = false) final String description) {
        final IPage<Route> page = this.routeService.page(description, pageNum, pageSize);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @PostMapping("listWorking")
    @ResponseBody
    public Result<List<WorkingVo>> listWorking(@RequestParam(value = "page") final Integer pageNum,
                                               @RequestParam(value = "limit") final Integer pageSize,
                                               @RequestParam(value = "routeName", required = false) final String routeName) throws Exception {
        final List<String> workingRouteList = this.gatewayService.listWorkingRoutes();
        if (null == workingRouteList || workingRouteList.isEmpty()) {
            return Result.ok();
        }
        final List<WorkingVo> workingVoList = new ArrayList<>(workingRouteList.size());
        for (final String workingRoute : workingRouteList) {
            final WorkingVo workingVo = new WorkingVo();
            workingVo.setData(workingRoute
                    .replaceAll("RouteDefinition", "")
                    .replaceAll("PredicateDefinition", "")
                    .replaceAll("FilterDefinition", ""));
            workingVoList.add(workingVo);
        }
        return Result.ok(workingVoList);
    }

    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "description") final String description,
                         @RequestParam(value = "uri") final String uri,
                         @RequestParam(value = "predicates") final String predicates,
                         @RequestParam(value = "filters") final String filters,
                         @RequestParam(value = "orderNum") final Integer orderNum,
                         @RequestParam(value = "serviceName") final String serviceName,
                         @RequestParam(value = "enabled") final Boolean enabled) {
        this.routeService.add(description, uri, predicates, filters, orderNum, serviceName, enabled);
        return Result.ok();
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(value = "id") final Long id,
                          @RequestParam(value = "description") final String description,
                          @RequestParam(value = "uri") final String uri,
                          @RequestParam(value = "predicates") final String predicates,
                          @RequestParam(value = "filters") final String filters,
                          @RequestParam(value = "orderNum") final Integer orderNum,
                          @RequestParam(value = "serviceName") final String serviceName,
                          @RequestParam(value = "enabled") final Boolean enabled) {
        this.routeService.edit(id, description, uri, predicates, filters, orderNum, serviceName, enabled);
        return Result.ok();
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "ids") final String ids) {
        final List<Long> idList = CommonTool.parseList(ids, ",", Long.class);
        this.routeService.delete(new HashSet<>(idList));
        return Result.ok();
    }

    @PostMapping("enable")
    @ResponseBody
    public Result<?> enable(@RequestParam(value = "id") final Long id) {
        this.routeService.enable(id, true);
        return Result.ok();
    }

    @PostMapping("disable")
    @ResponseBody
    public Result<?> disable(@RequestParam(value = "id") final Long id) {
        this.routeService.enable(id, false);
        return Result.ok();
    }

    @PostMapping("publish")
    @ResponseBody
    public Result<?> publish() throws Exception {
        this.gatewayService.refresh();
        return Result.ok();
    }
}