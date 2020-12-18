package org.wyyt.kafka.monitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.kafka.monitor.entity.dto.SysAlertCluster;
import org.wyyt.kafka.monitor.entity.dto.SysDingDingConfig;
import org.wyyt.kafka.monitor.entity.vo.KafkaBrokerVo;
import org.wyyt.kafka.monitor.entity.vo.ZooKeeperVo;
import org.wyyt.kafka.monitor.service.common.KafkaService;
import org.wyyt.kafka.monitor.service.common.KafkaZkService;
import org.wyyt.kafka.monitor.service.dto.SysAlertClusterService;
import org.wyyt.kafka.monitor.service.dto.SysDingDingConfigService;
import org.wyyt.tool.rpc.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The controller for providing the ability of alert for cluster.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(AlertClusterController.PREFIX)
public class AlertClusterController {
    public static final String PREFIX = "alertcluster";
    private final KafkaService kafkaService;
    private final KafkaZkService kafkaZkService;
    private final SysAlertClusterService sysAlertClusterService;
    private final SysDingDingConfigService sysDingDingConfigService;

    public AlertClusterController(final KafkaService kafkaService,
                                  KafkaZkService kafkaZkService,
                                  final SysAlertClusterService sysAlertClusterService,
                                  final SysDingDingConfigService sysDingDingConfigService) {
        this.kafkaService = kafkaService;
        this.kafkaZkService = kafkaZkService;
        this.sysAlertClusterService = sysAlertClusterService;
        this.sysDingDingConfigService = sysDingDingConfigService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/list", PREFIX);
    }

    @GetMapping("toadd")
    public String toAdd(final Model model) {
        final SysDingDingConfig sysDingDingConfig = this.sysDingDingConfigService.get();

        model.addAttribute("type", SysAlertCluster.Type.values());
        if (null != sysDingDingConfig) {
            model.addAttribute("accessToken", sysDingDingConfig.getAccessToken());
            model.addAttribute("secret", sysDingDingConfig.getSecret());
        }
        return String.format("%s/add", PREFIX);
    }

    @GetMapping("toedit")
    public String toEdit(final Model model,
                         @RequestParam(value = "id") final String id) throws Exception {
        final SysAlertCluster sysAlertCluster = this.sysAlertClusterService.getById(id);
        final Result<List<String>> listResult = this.listServers(sysAlertCluster.getType(), "update");
        model.addAttribute("item", sysAlertCluster);
        model.addAttribute("type", SysAlertCluster.Type.values());
        model.addAttribute("servers", listResult.getData());
        return String.format("%s/edit", PREFIX);
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<SysAlertCluster>> list(@RequestParam(value = "page") final Integer pageNum,
                                              @RequestParam(value = "limit") final Integer pageSize) {
        final QueryWrapper<SysAlertCluster> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(SysAlertCluster::getRowCreateTime);
        Page<SysAlertCluster> page = this.sysAlertClusterService.page(new Page<>(pageNum, pageSize), queryWrapper);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "type") final Integer type,
                         @RequestParam(value = "server") final String server,
                         @RequestParam(value = "email") final String email,
                         @RequestParam(value = "accessToken") final String accessToken,
                         @RequestParam(value = "secret") final String secret
    ) {
        this.sysAlertClusterService.save(type, server, email, accessToken, secret);
        return Result.ok();
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(value = "id") final Long id,
                          @RequestParam(value = "type") final Integer type,
                          @RequestParam(value = "server") final String server,
                          @RequestParam(value = "email") final String email,
                          @RequestParam(value = "accessToken") final String accessToken,
                          @RequestParam(value = "secret") final String secret
    ) {
        this.sysAlertClusterService.update(id, type, server, email, accessToken, secret);
        return Result.ok();
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "id") Long id) {
        this.sysAlertClusterService.removeById(id);
        return Result.ok();
    }

    @PostMapping("listServers")
    @ResponseBody
    public Result<List<String>> listServers(@RequestParam(value = "type") final Integer type,
                                            @RequestParam(value = "opt") final String opt) throws Exception {
        final List<String> result = new ArrayList<>();
        final SysAlertCluster.Type clusterType = SysAlertCluster.Type.get(type);
        switch (clusterType) {
            case KAFKA:
                final List<KafkaBrokerVo> kafkaBrokerVos = this.kafkaService.listBrokerInfos();
                for (final KafkaBrokerVo kafkaBrokerVo : kafkaBrokerVos) {
                    result.add(kafkaBrokerVo.getHost());
                }
                break;
            case ZOOKEEPER:
                final List<ZooKeeperVo> zooKeeperVos = this.kafkaZkService.listZooKeeperCluster();
                for (final ZooKeeperVo zooKeeperVo : zooKeeperVos) {
                    result.add(zooKeeperVo.getHost());
                }
                break;
        }
        if ("insert".equalsIgnoreCase(opt)) {
            final List<SysAlertCluster> sysAlertClusterList = this.sysAlertClusterService.getByType(clusterType);
            result.removeAll(sysAlertClusterList.stream().map(SysAlertCluster::getServer).collect(Collectors.toList()));
        }
        result.sort(String::compareTo);
        return Result.ok(result);
    }
}