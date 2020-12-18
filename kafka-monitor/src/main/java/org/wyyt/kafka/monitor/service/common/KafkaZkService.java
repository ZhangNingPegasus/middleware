package org.wyyt.kafka.monitor.service.common;


import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wyyt.admin.ui.exception.BusinessException;
import org.wyyt.kafka.monitor.config.PropertyConfig;
import org.wyyt.kafka.monitor.entity.dto.SysKpi;
import org.wyyt.kafka.monitor.entity.po.ZkStatus;
import org.wyyt.kafka.monitor.entity.po.ZooKeeperKpi;
import org.wyyt.kafka.monitor.entity.vo.ZooKeeperVo;
import org.wyyt.kafka.monitor.util.ZooKeeperKpiUtil;
import org.wyyt.tool.common.CommonTool;
import org.wyyt.tool.resource.ResourceTool;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The service for kafka cluter's zookeeper.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Service
public class KafkaZkService implements InitializingBean, DisposableBean {
    private static final String CHARSET_NAME = "gbk";
    private final PropertyConfig propertyConfig;
    private CuratorFramework curatorFramework;

    public KafkaZkService(final PropertyConfig propertyConfig) {
        this.propertyConfig = propertyConfig;
    }

    public List<ZooKeeperVo> listZooKeeperCluster() {
        final String[] zks = this.propertyConfig.getZkServers().split(",");
        final List<ZooKeeperVo> result = new ArrayList<>(zks.length);
        for (final String zk : zks) {
            final ZooKeeperVo zooKeeperVo = new ZooKeeperVo();
            zooKeeperVo.setHost(zk.split(":")[0]);
            zooKeeperVo.setPort(zk.split(":")[1].split("/")[0]);
            final ZkStatus status = this.status(zooKeeperVo.getHost(), zooKeeperVo.getPort());
            zooKeeperVo.setMode(status.getMode());
            zooKeeperVo.setVersion(status.getVersion());
            result.add(zooKeeperVo);
        }
        return result;
    }

    public List<String> getChildren(final String path) throws Exception {
        return this.curatorFramework.getChildren().forPath(path);
    }

    public String getData(final String path) throws Exception {
        return getString(this.curatorFramework.getData().forPath(path));
    }

    public String getData(final String path, final Stat stat) throws Exception {
        return getString(this.curatorFramework.getData().storingStatIn(stat).forPath(path));
    }

    public boolean exists(final String path) throws Exception {
        final Stat stat = this.curatorFramework.checkExists().forPath(path);
        return null != stat;
    }

    public void remove(final String path) throws Exception {
        if (this.exists(path)) {
            this.curatorFramework.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);
        }
    }

    public String execute(final String command,
                          final String type) throws Exception {
        String result;
        final String[] len = command.replaceAll(" ", "").split(type);
        if (len.length == 0) {
            return command + " has error";
        } else {
            final String cmd = len[1];
            switch (type) {
                case "get":
                    result = getData(cmd);
                    break;
                case "ls":
                    result = getChildren(cmd).toString();
                    break;
                default:
                    result = "Invalid command or not supported";
                    break;
            }
        }
        return result;
    }

    public List<SysKpi> kpi(final Date now) {
        final List<SysKpi> result = new ArrayList<>(SysKpi.ZK_KPI.values().length);
        final List<ZooKeeperVo> zooKeeperVoList = this.listZooKeeperCluster();
        for (final SysKpi.ZK_KPI kpi : SysKpi.ZK_KPI.values()) {
            if (ObjectUtils.isEmpty(kpi.getName())) {
                continue;
            }
            final SysKpi sysKpi = new SysKpi();
            sysKpi.setKpi(kpi.getCode());
            sysKpi.setCollectTime(now);
            final StringBuilder host = new StringBuilder();
            for (final ZooKeeperVo zookeeper : zooKeeperVoList) {
                final String ip = zookeeper.getHost();
                final String port = zookeeper.getPort();
                host.append(String.format("%s,", ip));
                final ZooKeeperKpi zooKeeperKpi = ZooKeeperKpiUtil.listKpi(ip, Integer.parseInt(port));
                switch (kpi) {
                    case ZK_PACKETS_RECEIVED:
                        sysKpi.setValue(CommonTool.numberic((null == sysKpi.getValue() ? 0D : sysKpi.getValue()) + Double.parseDouble(null == zooKeeperKpi.getZkPacketsReceived() ? "0" : zooKeeperKpi.getZkPacketsReceived())));
                        break;
                    case ZK_PACKETS_SENT:
                        sysKpi.setValue(CommonTool.numberic((null == sysKpi.getValue() ? 0D : sysKpi.getValue()) + Double.parseDouble(null == zooKeeperKpi.getZkPacketsSent() ? "0" : zooKeeperKpi.getZkPacketsSent())));
                        break;
                    case ZK_NUM_ALIVE_CONNECTIONS:
                        sysKpi.setValue(CommonTool.numberic((null == sysKpi.getValue() ? 0D : sysKpi.getValue()) + Double.parseDouble(null == zooKeeperKpi.getZkNumAliveConnections() ? "0" : zooKeeperKpi.getZkNumAliveConnections())));
                        break;
                    case ZK_OUTSTANDING_REQUESTS:
                        sysKpi.setValue(CommonTool.numberic((null == sysKpi.getValue() ? 0D : sysKpi.getValue()) + Double.parseDouble(null == zooKeeperKpi.getZkOutstandingRequests() ? "0" : zooKeeperKpi.getZkOutstandingRequests())));
                        break;
                    default:
                        break;
                }
            }
            if (null == sysKpi.getValue()) {
                continue;
            }
            sysKpi.setHost(host.length() == 0 ? "unkowns" : host.substring(0, host.length() - 1));
            result.add(sysKpi);
        }
        return result;
    }

    @Override
    public void afterPropertiesSet() {
        if (ObjectUtils.isEmpty(propertyConfig.getZkServers())) {
            throw new BusinessException("zookeeper的地址配置为空");
        }
        this.curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(propertyConfig.getZkServers())
                .sessionTimeoutMs(60000)  // 连接超时时间
                .connectionTimeoutMs(15000)  // 会话超时时间
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))  // 刚开始重试间隔为1秒，之后重试间隔逐渐增加，最多重试不超过3次
                .build();
        this.curatorFramework.start();
    }

    @Override
    public void destroy() {
        ResourceTool.closeQuietly(this.curatorFramework);
    }

    private ZkStatus status(final String host,
                            final String port) {
        final ZkStatus zkStatus = new ZkStatus();
        Socket sock;
        try {
            String tmp;
            if (port.contains("/")) {
                tmp = port.split("/")[0];
            } else {
                tmp = port;
            }
            sock = new Socket(host, Integer.parseInt(tmp));
        } catch (final Exception e) {
            zkStatus.setMode("death");
            zkStatus.setVersion("death");
            return zkStatus;
        }
        BufferedReader reader = null;
        OutputStream outstream = null;
        try {
            outstream = sock.getOutputStream();
            outstream.write("stat".getBytes());
            outstream.flush();
            sock.shutdownOutput();

            reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains("mode: ")) {
                    zkStatus.setMode(line.replaceAll("Mode: ", "").trim());
                } else if (line.toLowerCase().contains("version")) {
                    zkStatus.setVersion(line.split(":")[1].split("-")[0].trim());
                }
            }
        } catch (final Exception ex) {
            zkStatus.setMode("death");
            zkStatus.setVersion("death");
            return zkStatus;
        } finally {
            ResourceTool.closeQuietly(sock);
            ResourceTool.closeQuietly(reader);
            ResourceTool.closeQuietly(outstream);
        }
        return zkStatus;
    }

    private static String getString(byte[] bytes) throws UnsupportedEncodingException {
        if (null == bytes) {
            return "";
        }
        return new String(bytes, CHARSET_NAME);
    }
}