package org.wyyt.springcloud.gateway.controller;

import kong.unirest.Unirest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.wyyt.admin.ui.entity.dto.SysPage;
import org.wyyt.admin.ui.service.SysPageService;
import org.wyyt.springcloud.gateway.entity.EndpointVo;
import org.wyyt.springcloud.gateway.entity.ServiceVo;
import org.wyyt.springcloud.gateway.service.GatewayService;
import org.wyyt.tool.rpc.Result;
import org.wyyt.tool.rpc.RpcTool;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.wyyt.springcloud.gateway.controller.ConsulController.PREFIX;


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
public class ConsulController {
    public static final String PREFIX = "consul";
    private final GatewayService gatewayService;
    private final SysPageService sysPageService;

    public ConsulController(final GatewayService gatewayService,
                            final RpcTool rpcTool,
                            final SysPageService sysPageService) {
        this.gatewayService = gatewayService;
        this.sysPageService = sysPageService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/%s", PREFIX, "list");
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<EndpointVo>> list(@RequestParam(value = "instanceId", required = false) final String instanceId,
                                         @RequestParam(value = "page") final Integer pageNum,
                                         @RequestParam(value = "limit") final Integer pageSize) throws Exception {
        final List<EndpointVo> endpointVoList = new ArrayList<>();
        final List<ServiceVo> serviceVos = this.gatewayService.listService(false);
        for (final ServiceVo serviceVo : serviceVos) {
            endpointVoList.addAll(serviceVo.getEndpointVoList());
        }

        final List<EndpointVo> filterEndpointVoList = endpointVoList.stream()
                .filter(p -> StringUtils.isEmpty(instanceId) || p.getId().contains(instanceId)).collect(Collectors.toList());

        final List<EndpointVo> result = filterEndpointVoList.stream().skip((pageNum - 1L) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());

        return Result.ok(result, filterEndpointVoList.size());
    }

    @PostMapping("remove")
    @ResponseBody
    public Result<String> remove(@RequestParam(value = "id") final String id) throws Exception {
        final SysPage sysPage = sysPageService.getByName("注册中心");

        if (null == sysPage) {
            throw new Exception("请在[页面配置]界面配置注册中心的地址");
        }
        String url = sysPage.getUrl();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        final String result = Unirest.put(String.format("%s/v1/agent/service/deregister/%s", url, id)).asString().getBody();
        return Result.ok(result);
    }
}