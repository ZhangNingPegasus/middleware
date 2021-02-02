package org.wyyt.springcloud.gateway.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.springcloud.gateway.entity.entity.Api;
import org.wyyt.springcloud.gateway.service.ApiServiceImpl;
import org.wyyt.springcloud.gateway.service.ConsulService;
import org.wyyt.tool.common.CommonTool;
import org.wyyt.tool.rpc.Result;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.wyyt.springcloud.gateway.controller.ApiController.PREFIX;


/**
 * The controller of Api
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
public class ApiController {
    public static final String PREFIX = "api";
    private final ConsulService consulService;
    private final ApiServiceImpl apiServiceImpl;

    public ApiController(final ConsulService consulService,
                         final ApiServiceImpl apiServiceImpl) {
        this.consulService = consulService;
        this.apiServiceImpl = apiServiceImpl;
    }

    @GetMapping("tolist")
    public String toList(final Model model) {
        final Set<String> serviceNames = new HashSet<>();
        serviceNames.addAll(this.consulService.listServiceNames());
        serviceNames.addAll(this.apiServiceImpl.listServiceNames());
        model.addAttribute("serviceNames", serviceNames.stream().sorted(String::compareTo).collect(Collectors.toList()));
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("toadd")
    public String toAdd(final Model model) {
        model.addAttribute("serviceNames", this.consulService.listServiceNames());
        return String.format("%s/%s", PREFIX, "add");
    }

    @GetMapping("toedit")
    public String toEdit(final Model model,
                         @RequestParam(value = "id") final Long id) {
        model.addAttribute("api", this.apiServiceImpl.getById(id));
        model.addAttribute("serviceNames", this.consulService.listServiceNames());
        return String.format("%s/%s", PREFIX, "edit");
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<Api>> list(@RequestParam(value = "page") final Integer pageNum,
                                  @RequestParam(value = "limit") final Integer pageSize,
                                  @RequestParam(value = "name", required = false) final String name,
                                  @RequestParam(value = "serviceName", required = false) final String serviceName,
                                  @RequestParam(value = "path", required = false) final String path) {
        final IPage<Api> page = this.apiServiceImpl.page(pageNum, pageSize, name, serviceName, path);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "name") final String name,
                         @RequestParam(value = "method") final String method,
                         @RequestParam(value = "serviceName") final String serviceName,
                         @RequestParam(value = "path") final String path) {
        this.apiServiceImpl.add(name, method, serviceName, path);
        return Result.ok();
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(value = "id") final Long id,
                          @RequestParam(value = "name") final String name,
                          @RequestParam(value = "method") final String method,
                          @RequestParam(value = "serviceName") final String serviceName,
                          @RequestParam(value = "path") final String path) {
        this.apiServiceImpl.edit(id, name, method, serviceName, path);
        return Result.ok();
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "ids") final String ids) throws Exception {
        final List<Long> idList = CommonTool.parseList(ids, ",", Long.class);
        this.apiServiceImpl.del(new HashSet<>(idList));
        return Result.ok();
    }
}