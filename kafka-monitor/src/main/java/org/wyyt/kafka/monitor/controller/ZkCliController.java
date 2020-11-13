package org.wyyt.kafka.monitor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.wyyt.kafka.monitor.entity.po.ZooKeeperKpi;
import org.wyyt.kafka.monitor.entity.vo.ZooKeeperVo;
import org.wyyt.kafka.monitor.service.common.KafkaZkService;
import org.wyyt.kafka.monitor.util.ZooKeeperKpiUtil;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.web.Result;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The controller for providing the zookeeper's client.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(ZkCliController.PREFIX)
public class ZkCliController {
    public static final String PREFIX = "zkCli";
    private final KafkaZkService kafkaZkService;

    public ZkCliController(final KafkaZkService kafkaZkService) {
        this.kafkaZkService = kafkaZkService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/list", PREFIX);
    }

    @PostMapping("zkInfo")
    @ResponseBody
    public Result<String> zkInfo() {
        try {
            final List<ZooKeeperVo> zooKeeperVoList = this.kafkaZkService.listZooKeeperCluster();
            if (!zooKeeperVoList.isEmpty()) {
                final ZooKeeperVo zooKeeperInfo = zooKeeperVoList.get(0);
                final ZooKeeperKpi zooKeeperKpi = ZooKeeperKpiUtil.listKpi(zooKeeperInfo.getHost(), Integer.parseInt(zooKeeperInfo.getPort()));
                if (!ObjectUtils.isEmpty(zooKeeperKpi.getZkNumAliveConnections())) {
                    final List<String> result = zooKeeperVoList.stream().map(p -> String.format("%s:%s", p.getHost(), p.getPort())).collect(Collectors.toList());
                    return Result.success(result.toString());
                }
            }
        } catch (Exception e) {
            return Result.error(ExceptionTool.getRootCauseMessage(e));
        }
        return Result.error();
    }

    @PostMapping("execute")
    @ResponseBody
    public Result<String> execute(@RequestParam(name = "command") final String command,
                                  @RequestParam(name = "type") final String type) {
        try {
            return Result.success(this.kafkaZkService.execute(command, type));
        } catch (Exception e) {
            return Result.error(ExceptionTool.getRootCauseMessage(e));
        }
    }
}