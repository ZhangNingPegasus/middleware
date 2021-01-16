package org.wyyt.sharding.db2es.core.util.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 * the common functions of ZooKeeper
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
public final class ZooKeeperUtils {
    private static final String CHARSET_NAME = "gbk";

    public static CuratorFramework createCuratorFramework(final String zkServers) {
        return CuratorFrameworkFactory.builder().connectString(zkServers)
                .sessionTimeoutMs(60000)    // 连接超时时间
                .connectionTimeoutMs(15000)  // 会话超时时间
                .retryPolicy(new ExponentialBackoffRetry(1000, 5))   // 刚开始重试间隔为1秒，之后重试间隔逐渐增加，最多重试不超过五次
                .build();
    }

    public static void setData(final CuratorFramework curatorFramework,
                               final String path,
                               final String data) throws Exception {
        curatorFramework
                .create()
                .orSetData()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .forPath(path, data.getBytes(CHARSET_NAME));
    }

    public static String getData(final CuratorFramework curatorFramework,
                                 final String path,
                                 final Stat stat) throws Exception {
        return new String(curatorFramework.getData().storingStatIn(stat).forPath(path), CHARSET_NAME);
    }

    public static String getData(final CuratorFramework curatorFramework,
                                 final String path) throws Exception {
        return new String(curatorFramework.getData().forPath(path), CHARSET_NAME);
    }

    public static List<String> getChildren(final CuratorFramework curatorFramework,
                                           final String path) throws Exception {
        return curatorFramework.getChildren().forPath(path);
    }

    public static boolean exists(final CuratorFramework curatorFramework,
                                 final String path) throws Exception {
        return null != curatorFramework.checkExists().forPath(path);
    }

    public static void remove(final CuratorFramework curatorFramework,
                              final String path) throws Exception {
        if (ZooKeeperUtils.exists(curatorFramework, path)) {
            curatorFramework.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);
        }
    }
}