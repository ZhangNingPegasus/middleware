package org.wyyt.sharding.db2es.client.http.handler;

import org.springframework.util.ObjectUtils;
import org.wyyt.sharding.db2es.client.http.BaseHandler;
import org.wyyt.sharding.db2es.client.http.Param;
import org.wyyt.sharding.db2es.client.http.anno.PostMapping;
import org.wyyt.sharding.db2es.client.http.anno.RestController;
import org.wyyt.sharding.db2es.core.entity.view.SettingVo;
import org.wyyt.tool.rpc.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.wyyt.sharding.db2es.core.entity.domain.Names.*;

/**
 * rest controller of setting
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@RestController("set")
public final class SettingHandler extends BaseHandler {
    @PostMapping("get")
    public final Result<List<SettingVo>> get(final Param param) {
        final List<SettingVo> result = new ArrayList<>();

        result.add(new SettingVo(DB2ES_ID, this.context.getConfig().getDb2EsId().toString(), "分布式id, 具有同样的id为主备模式, 不同的id为分布式"));
        result.add(new SettingVo(DB2ES_PORT, String.valueOf(this.context.getConfig().getDb2EsPort()), "db2es的端口"));
        result.add(new SettingVo(CONTINUE_ON_ERROR,
                this.context.getConfig().getContinueOnError().toString(),
                "当导入Elastic-Search失败时的后续动作, true表示将错误信息记录到数据库日志中并继续后续消费;false表示将持续消费当前失败的消息,直至成功为止, 默认: false"));
        result.add(new SettingVo(KAFKA_BOOTSTRAP_SERVERS, this.context.getKafkaBootstrapServers(), "Kafka集群地址"));
        result.add(new SettingVo(APOLLO_APP_ID, this.context.getConfig().getApolloAppId(), "Apollo配置"));

        if (!ObjectUtils.isEmpty(this.context.getConfig().getInitialCheckpoint())) {
            result.add(new SettingVo(INITIAL_CHECKPOINT,
                    this.context.getConfig().getInitialCheckpoint(),
                    "指定所有Topic的消费位点, 优先级低于db2es.{topic_name}-{partition}.checkpoint。如果不指定,则会自动接着上次未消费的地方接着消费"));
        }

        for (Map.Entry<String, String> pair : this.context.getConfig().getTopicCheckpointMap().entrySet()) {
            result.add(new SettingVo(pair.getKey(),
                    pair.getValue(),
                    "指定主题的消费位点. 格式: [偏移量] 或 [偏移量@消费位点的时间戳],如:1183或1183@1591752301558,当是后者时,会忽略[偏移量]"));
        }
        return Result.ok(result);
    }

    @PostMapping("refreshDbConfig")
    public synchronized final Result<Boolean> refreshDbConfig(final Param param) throws Exception {
        this.context.refreshExtraConfig();
        return Result.ok(true);
    }
}