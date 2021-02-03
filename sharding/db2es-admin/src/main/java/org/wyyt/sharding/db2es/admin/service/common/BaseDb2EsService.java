package org.wyyt.sharding.db2es.admin.service.common;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.wyyt.sharding.db2es.core.entity.domain.Common;
import org.wyyt.sharding.db2es.core.entity.domain.Names;
import org.wyyt.sharding.db2es.core.entity.view.NodeVo;
import org.wyyt.sharding.db2es.core.entity.view.TopicVo;
import org.wyyt.sharding.db2es.core.exception.Db2EsException;
import org.wyyt.tool.rpc.Result;
import org.wyyt.tool.rpc.RpcService;
import org.wyyt.tool.rpc.SignTool;

import javax.annotation.Nullable;
import java.util.*;

import static org.wyyt.sharding.db2es.core.util.CommonUtils.OBJECT_MAPPER;

/**
 * The service for RPC of db2es
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public abstract class BaseDb2EsService {
    protected static final String HTTP_URL_FORMAT = "http://%s:%s/%s";
    protected static final String TOPIC_LIST = "topic/list";
    protected static final String START_TOPIC = "topic/startTopic";
    protected static final String STOP_TOPIC = "topic/stopTopic";
    protected static final String RE_START_TOPIC = "topic/restartTopic";
    protected static final String INSTALL_TOPIC = "topic/installTopic";
    protected static final String UNINSTALL_TOPIC = "topic/uninstallTopic";
    protected static final String CALC_OFFSET_BY_TIMESTAMP = "topic/calcOffsetByTimestamp";
    protected static final String SETTING_VO = "set/get";
    protected static final String REFRESH_DB_CONFIG = "set/refreshDbConfig";
    @Autowired
    private ZooKeeperService zooKeeperService;
    @Autowired
    private RpcService rpcService;

    public List<NodeVo> listDd2Es() throws Exception {
        final Set<NodeVo> leaderVoSet = this.refreshLeaderVoSet();
        final List<NodeVo> result = new ArrayList<>();
        for (final NodeVo nodeVo : leaderVoSet) {
            if (null == nodeVo) {
                continue;
            }
            result.add(nodeVo);
        }
        result.sort(Comparator.comparing(NodeVo::getId));
        return result;
    }

    public NodeVo getNodeVoByDb2EsId(final Integer db2esId) throws Exception {
        final Set<NodeVo> leaderVoSet = this.refreshLeaderVoSet();
        for (final NodeVo nodeVo : leaderVoSet) {
            if (null == nodeVo) {
                continue;
            }
            if (nodeVo.getId().equals(db2esId)) {
                return nodeVo;
            }
        }
        return null;
    }

    public NodeVo getNodeVoByTopicName(final String topicName) throws Exception {
        final Map<String, NodeVo> topicsMap = this.refreshTopicsMap();
        if (!topicsMap.containsKey(topicName)) {
            throw new Db2EsException(String.format("主题[%s]不存在任何的DB2ES中, 请检查是该Topic是否已开启", topicName));
        }
        return topicsMap.get(topicName);
    }

    public NodeVo getNodeByTopicName(final String topicName) throws Exception {
        final Map<String, NodeVo> topicsMap = this.refreshTopicsMap();
        return topicsMap.get(topicName);
    }

    protected Map<NodeVo, List<TopicVo>> getAllTopics(@Nullable final String searchTopicName) throws Exception {
        final Map<String, Object> params = new HashMap<>();
        params.put("topicName", searchTopicName);
        return postToAllLeader(TOPIC_LIST, params, TopicVo.class);
    }

    protected <T> T postToTopicForOne(final String topicName,
                                      final String path,
                                      final Map<String, Object> params,
                                      final Class<T> tClass) throws Exception {
        final List<T> result = postToTopic(topicName, path, params, tClass);
        if (result != null) {
            if (1 == result.size()) {
                return result.get(0);
            } else if (result.size() > 1) {
                throw new Db2EsException("期待一个结果, 但是却拥有多个");
            }
        }
        return null;
    }

    protected <T> List<T> postToTopic(final String topicName,
                                      final String path,
                                      final Map<String, Object> params,
                                      final Class<T> tClass) throws Exception {
        this.refreshTopicsMap();
        final NodeVo leaderNodeVo = getNodeVoByTopicName(topicName);
        return new ArrayList<>(post(leaderNodeVo, path, params, tClass));
    }

    protected <T> Map<NodeVo, List<T>> postToAnyLeader(final String path,
                                                       final Map<String, Object> params,
                                                       final Class<T> tClass) throws Exception {
        final Map<NodeVo, List<T>> result = new HashMap<>();
        final Set<NodeVo> leaderVoSet = this.refreshLeaderVoSet();
        for (final NodeVo leaderVo : leaderVoSet) {
            final List<T> respond = post(leaderVo, path, params, tClass);
            result.put(leaderVo, respond);
            return result;
        }
        return null;
    }

    protected <T> Map<NodeVo, List<T>> postToAllLeader(final String path,
                                                       final Map<String, Object> params,
                                                       final Class<T> tClass) throws Exception {
        final Map<NodeVo, List<T>> result = new HashMap<>();
        final Set<NodeVo> leaderVoSet = this.refreshLeaderVoSet();
        for (final NodeVo leaderVo : leaderVoSet) {
            final List<T> respond = post(leaderVo, path, params, tClass);
            if (result.containsKey(leaderVo)) {
                result.get(leaderVo).addAll(respond);
            } else {
                result.put(leaderVo, respond);
            }
        }
        return result;
    }

    protected <T> List<T> post(final NodeVo leaderVo,
                               final String path,
                               Map<String, Object> params,
                               final Class<T> tClass) throws Exception {
        final List<T> result = new ArrayList<>();
        final String url = getUrl(leaderVo, path);
        final Map<String, String> headers = new HashMap<>();
        if (null == params) {
            params = new HashMap<>();
        }
        headers.put("sign", SignTool.sign(params, Names.API_KEY, Names.API_IV));
        final Result<?> respondResult = this.rpcService.post(url, params, headers, new com.alibaba.fastjson.TypeReference<Result<?>>() {
        });
        if (null == respondResult) {
            return result;
        }
        if (respondResult.getOk()) {
            final Object data = respondResult.getData();
            if (null != data) {
                if (data instanceof List) {
                    for (final T datum : (List<T>) data) {
                        result.add(OBJECT_MAPPER.convertValue(datum, tClass));
                    }
                } else if (data instanceof Map) {
                    result.add(OBJECT_MAPPER.convertValue(data, tClass));
                } else {
                    result.add((T) ConvertUtils.convert(data, tClass));
                }
            }
        } else {
            throw new Db2EsException(String.format("Access url[%s] meet error, %s", url, respondResult.getError()));
        }
        return result;
    }

    private String getUrl(final NodeVo leaderVo, final String path) {
        return String.format(HTTP_URL_FORMAT, leaderVo.getIp(), leaderVo.getPort(), path);
    }

    private Map<String, NodeVo> refreshTopicsMap() throws Exception {
        final Map<String, NodeVo> result = new HashMap<>();
        final Map<NodeVo, List<TopicVo>> allTopicsMap = getAllTopics(null);
        for (final Map.Entry<NodeVo, List<TopicVo>> pair : allTopicsMap.entrySet()) {
            final NodeVo leaderVo = pair.getKey();
            final List<TopicVo> topicVoList = pair.getValue();

            for (final TopicVo topicVo : topicVoList) {
                final String topicName = topicVo.getTopicName();
                if (result.containsKey(topicName)) {
                    throw new Db2EsException(String.format("主题[%s]在多台分布式节点(非高可用, %s, %s)上同时存在, 请修改配置后在启动",
                            topicName,
                            result.get(topicName).getIp(),
                            leaderVo.getIp()));
                }
                result.put(topicName, leaderVo);
            }
        }
        return result;
    }

    private Set<NodeVo> refreshLeaderVoSet() throws Exception {
        final Set<NodeVo> result = new HashSet<>();
        if (this.zooKeeperService.exists(Common.ZK_LEADER_PATH)) {
            final List<String> idPaths = this.zooKeeperService.getChildren(Common.ZK_LEADER_PATH);
            for (final String id : idPaths) {
                final String idPath = Common.ZK_LEADER_PATH.concat("/").concat(id);
                final List<String> nodes = this.zooKeeperService.getChildren(idPath);
                if (nodes == null || nodes.isEmpty()) {
                    continue;
                }
                final String leaderNode = getLeaderNode(nodes);
                Assert.notNull(leaderNode, "Leader Node is null");
                final String data = this.zooKeeperService.getData(idPath.concat("/").concat(leaderNode));
                final NodeVo leaderNodeVo = JSON.parseObject(data, NodeVo.class);
                leaderNodeVo.setId(getId(idPath));
                leaderNodeVo.setSlaveList(new ArrayList<>(nodes.size() - 1));

                for (final String node : nodes) {
                    if (leaderNode.equals(node)) {
                        continue;
                    }
                    final NodeVo slaveNodeVo = JSON.parseObject(this.zooKeeperService.getData(idPath.concat("/").concat(node)), NodeVo.class);
                    slaveNodeVo.setId(leaderNodeVo.getId());
                    leaderNodeVo.getSlaveList().add(slaveNodeVo);
                }
                result.add(leaderNodeVo);
            }
        }
        return result;
    }

    private String getLeaderNode(final List<String> nodes) {
        if (null == nodes || nodes.isEmpty()) {
            return null;
        }

        String result = null;
        Long suffix = null;

        for (final String node : nodes) {
            if (ObjectUtils.isEmpty(node)) {
                continue;
            }

            final int startIndex = node.lastIndexOf('-');

            final String suffixStr = node.substring(startIndex + 1);
            final long suffixLong = Long.parseLong(suffixStr);
            if (null == suffix || suffix > suffixLong) {
                suffix = suffixLong;
                result = node;
            }
        }
        return result;
    }

    private Integer getId(final String idPath) {
        if (ObjectUtils.isEmpty(idPath)) {
            return null;
        }

        final int startIndex = idPath.lastIndexOf('_');
        return Integer.parseInt(idPath.substring(startIndex + 1));
    }
}