package org.wyyt.springcloud.gateway.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.wyyt.springcloud.gateway.entity.entity.Api;
import org.wyyt.springcloud.gateway.service.ApiServiceImpl;
import org.wyyt.springcloud.gateway.service.AppServiceImpl;
import org.wyyt.springcloud.gateway.service.AuthServiceImpl;
import org.wyyt.tool.rpc.Result;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.wyyt.springcloud.gateway.controller.AuthController.PREFIX;


/**
 * The controller of Auth
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
public class AuthController {
    public static final String PREFIX = "auth";
    private final AuthServiceImpl authServiceImpl;
    private final AppServiceImpl appServiceImpl;
    private final ApiServiceImpl apiServiceImpl;

    public AuthController(final AuthServiceImpl authServiceImpl,
                          final AppServiceImpl appServiceImpl,
                          final ApiServiceImpl apiServiceImpl) {
        this.authServiceImpl = authServiceImpl;
        this.appServiceImpl = appServiceImpl;
        this.apiServiceImpl = apiServiceImpl;
    }

    @GetMapping("tolist")
    public String toList(final Model model) {
        model.addAttribute("apps", this.appServiceImpl.list());
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("toadd")
    public String toAdd(final Model model,
                        @RequestParam("appId") final Long appId) {
        model.addAttribute("appId", appId);
        model.addAttribute("serviceNames", this.apiServiceImpl.listServiceNames());
        return String.format("%s/%s", PREFIX, "add");
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<Api>> list(@RequestParam(value = "page") final Integer pageNum,
                                  @RequestParam(value = "limit") final Integer pageSize,
                                  @RequestParam(value = "appId", required = false) final Long appId) {
        final IPage<Api> page = this.authServiceImpl.page(pageNum, pageSize, appId);
        if (null == page) {
            return Result.ok();
        }
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @PostMapping("listApis")
    @ResponseBody
    public Result<List<Api>> listApis(@RequestParam(value = "page") final Integer pageNum,
                                      @RequestParam(value = "limit") final Integer pageSize,
                                      @RequestParam(value = "appId") final Long appId,
                                      @RequestParam(value = "serviceName", required = false) final String serviceName,
                                      @RequestParam(value = "name", required = false) final String name,
                                      @RequestParam(value = "path", required = false) final String path) {
        final IPage<Api> page = this.authServiceImpl.selectNoAuthApis(pageNum, pageSize, appId, serviceName, name, path);
        if (null == page) {
            return Result.ok();
        }
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "appId") final Long appId,
                         @RequestParam(value = "apiIds") final String apiIds) throws Exception {
        final String[] apiIdArray = apiIds.split(",");
        final List<Long> apiIdList = new ArrayList<>(apiIdArray.length);
        for (final String apiId : apiIdArray) {
            if (ObjectUtils.isEmpty(apiId)) {
                continue;
            }
            apiIdList.add(Long.parseLong(apiId));
        }
        this.authServiceImpl.add(appId, apiIdList);
        return Result.ok();
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "appId") final Long appId,
                         @RequestParam(value = "apiIds") final String apiIds) throws Exception {
        final String[] apiIdArray = apiIds.split(",");
        final Set<Long> apiIdSet = new HashSet<>(apiIdArray.length);
        for (final String apiId : apiIdArray) {
            if (ObjectUtils.isEmpty(apiId)) {
                continue;
            }
            apiIdSet.add(Long.parseLong(apiId));
        }
        this.authServiceImpl.del(appId, apiIdSet);
        return Result.ok();
    }
}