package org.wyyt.sharding.db2es.client.zookeeper;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.wyyt.sharding.db2es.client.common.Constant;
import org.wyyt.sharding.db2es.client.common.Context;
import org.wyyt.sharding.db2es.client.common.Utils;
import org.wyyt.sharding.db2es.core.entity.domain.Common;
import org.wyyt.sharding.db2es.core.entity.view.NodeVo;
import org.wyyt.sharding.db2es.core.util.zookeeper.ZooKeeperUtils;
import org.wyyt.tool.resource.ResourceTool;

import java.io.Closeable;
import java.util.List;

import static org.wyyt.sharding.db2es.core.entity.domain.Names.DB2ES_ID;


/**
 * the wrapper class of ZooKeeper, which providing each of methods to manipulate ZooKeeper
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public class ZooKeeperWrapper implements Closeable {
    @Getter
    private final CuratorFramework curatorFramework;
    private final LeaderLatch leaderLatch;

    public ZooKeeperWrapper(final Context context) throws Exception {
        final String zookeeperServers = context.getConfig().getZkServers();
        if (ObjectUtils.isEmpty(zookeeperServers)) {
            throw new RuntimeException("kafka集群所使用的zookeeper集群地址不允许为空");
        }
        this.curatorFramework = ZooKeeperUtils.createCuratorFramework(zookeeperServers);
        this.curatorFramework.start();

        final String db2esId = context.getConfig().getDb2EsId().toString();
        Assert.isTrue(!ObjectUtils.isEmpty(db2esId), String.format("the parameter[%s] is required in file[%s]", DB2ES_ID, Constant.PROPERTIES_FILE_NAME));

        final Utils.IP local = Utils.getLocalIp(context);
        final NodeVo leaderVo = new NodeVo(local.getLocalName(), local.getLocalIp(), context.getConfig().getDb2EsPort());

        this.leaderLatch = new LeaderLatch(
                this.curatorFramework,
                String.format("%s%s", Common.ZK_ID_PATH, db2esId),
                String.format("%s", JSON.toJSONString(leaderVo)));

        this.leaderLatch.addListener(new LeaderLatchListener() {
            @Override
            public void isLeader() {
            }

            @Override
            public void notLeader() {
            }
        });
        this.leaderLatch.start();

        log.info(String.format("ZooKeeperWrapper: initialize the ZooKeeper with server [%s] with successfully", zookeeperServers));
    }

    public final boolean isLeader() {
        return this.leaderLatch.hasLeadership();
    }

    public final void electLeader() throws Exception {
        this.leaderLatch.await();
    }

    public final void setData(final String path,
                              final String data) throws Exception {
        ZooKeeperUtils.setData(this.curatorFramework, path, data);
    }

    public final String getData(final String path) throws Exception {
        return ZooKeeperUtils.getData(this.curatorFramework, path);
    }

    public final List<String> getChildren(final String path) throws Exception {
        return ZooKeeperUtils.getChildren(this.curatorFramework, path);
    }

    public final boolean exists(final String path) throws Exception {
        return ZooKeeperUtils.exists(this.curatorFramework, path);
    }

    @Override
    public final void close() {
        ResourceTool.closeQuietly(this.leaderLatch);
        ResourceTool.closeQuietly(this.curatorFramework);
    }
}