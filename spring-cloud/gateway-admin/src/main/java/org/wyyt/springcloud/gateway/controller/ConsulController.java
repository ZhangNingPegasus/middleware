package org.wyyt.springcloud.gateway.controller;

import kong.unirest.Unirest;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.wyyt.springcloud.gateway.config.PropertyConfig;
import org.wyyt.springcloud.gateway.entity.EndpointVo;
import org.wyyt.springcloud.gateway.entity.ServiceVo;
import org.wyyt.springcloud.gateway.service.GatewayService;
import org.wyyt.tool.common.CommonTool;
import org.wyyt.tool.rpc.Result;

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
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public class ConsulController {
    public static final String PREFIX = "consul";
    private final GatewayService gatewayService;
    private final PropertyConfig propertyConfig;

    public ConsulController(final GatewayService gatewayService,
                            final PropertyConfig propertyConfig) {
        this.gatewayService = gatewayService;
        this.propertyConfig = propertyConfig;
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
                .filter(p -> ObjectUtils.isEmpty(instanceId) || p.getId().contains(instanceId)).collect(Collectors.toList());

        final List<EndpointVo> result = filterEndpointVoList.stream().skip((pageNum - 1L) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());

        return Result.ok(result, filterEndpointVoList.size());
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> remove(@RequestParam(value = "ids") final String ids) {
        final List<String> idList = CommonTool.parseList(ids, ",", String.class);
        final String url = String.format("http://%s:%s", this.propertyConfig.getConsulHost(), this.propertyConfig.getConsulPort());
        for (final String id : idList) {
            Unirest.put(String.format("%s/v1/agent/service/deregister/%s", url, id)).asString().getBody();
        }
        return Result.ok();
    }
}