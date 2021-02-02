package org.wyyt.sharding.db2es.admin.service.common;

import lombok.Getter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wyyt.sharding.db2es.admin.config.PropertyConfig;
import org.wyyt.sharding.db2es.core.exception.Db2EsException;
import org.wyyt.sharding.db2es.core.util.zookeeper.ZooKeeperUtils;
import org.wyyt.tool.resource.ResourceTool;

import java.util.List;

/**
 * The service for kafka cluter's zookeeper.
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class ZooKeeperService implements DisposableBean {
    @Getter
    private final CuratorFramework curatorFramework;

    public ZooKeeperService(final PropertyConfig propertyConfig) {
        if (ObjectUtils.isEmpty(propertyConfig.getZkServers())) {
            throw new Db2EsException("zookeeper的地址配置为空，请在application.yml中通过db2es.zookeeper.servers配置zookeeper的地址，多个地址用逗号分隔，例:192.168.182.128:2181,192.168.182.129:2181,192.168.182.130:2181");
        }
        this.curatorFramework = ZooKeeperUtils.createCuratorFramework(propertyConfig.getZkServers());
        this.curatorFramework.start();
    }

    public List<String> getChildren(final String path) throws Exception {
        return ZooKeeperUtils.getChildren(this.curatorFramework, path);
    }

    public String getData(final String path) throws Exception {
        return ZooKeeperUtils.getData(this.curatorFramework, path);
    }

    public String getData(final String path,
                          final Stat stat) throws Exception {
        return ZooKeeperUtils.getData(this.curatorFramework, path, stat);
    }

    public void setData(final String path,
                        final String data) throws Exception {
        ZooKeeperUtils.setData(this.curatorFramework, path, data);
    }

    public boolean exists(final String path) throws Exception {
        return ZooKeeperUtils.exists(this.curatorFramework, path);
    }

    public void remove(final String path) throws Exception {
        ZooKeeperUtils.remove(this.curatorFramework, path);
    }

    @Override
    public void destroy() {
        if (null != this.curatorFramework) {
            ResourceTool.closeQuietly(this.curatorFramework);
        }
    }
}