package org.wyyt.db2es.admin.service.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.wyyt.db2es.admin.entity.vo.TopicInfoVo;
import org.wyyt.db2es.core.entity.domain.TopicOffset;
import org.wyyt.db2es.core.entity.view.NodeVo;
import org.wyyt.db2es.core.entity.view.SettingVo;
import org.wyyt.db2es.core.entity.view.TopicVo;
import org.wyyt.tool.date.DateTool;

import java.util.*;

/**
 * The service for RPC of db2es
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Slf4j
@Service
public class Db2EsHttpService extends BaseDb2EsService {
    public List<TopicInfoVo> getTopicVoList(final String searchTopicName) throws Exception {
        final List<TopicInfoVo> result = new ArrayList<>();
        final Map<NodeVo, List<TopicVo>> leaderVoListMap = this.getAllTopics(searchTopicName);
        for (final Map.Entry<NodeVo, List<TopicVo>> leaderVoListEntry : leaderVoListMap.entrySet()) {
            final NodeVo leaderVo = leaderVoListEntry.getKey();
            final List<TopicVo> topicVoList = leaderVoListEntry.getValue();
            int index = 1;
            for (final TopicVo topicVo : topicVoList) {
                final TopicInfoVo topicInfoVo = new TopicInfoVo();
                topicInfoVo.setNum(index++);
                topicInfoVo.setDb2esId(leaderVo.getId());
                topicInfoVo.setHost(String.format("%s(%s)", leaderVo.getIp(), leaderVo.getPort()));
                topicInfoVo.setTopicName(topicVo.getTopicName());
                topicInfoVo.setSize(topicVo.getTopicOffset().getSize());
                topicInfoVo.setOffset(topicVo.getTopicOffset().getOffset());
                topicInfoVo.setVersion(topicVo.getVersion());
                if (null != topicVo.getTopicOffset().getOffset() && null != topicVo.getTopicOffset().getOffset()) {
                    topicInfoVo.setLag(Math.max(0, topicVo.getTopicOffset().getSize() - topicVo.getTopicOffset().getOffset()));
                }
                if (null != topicVo.getTopicOffset().getOffsetTimestamp()) {
                    topicInfoVo.setOffsetDateTime(DateTool.format(new Date(Long.parseLong(topicVo.getTopicOffset().getOffsetTimestamp()))));
                }
                topicInfoVo.setIsActive(topicVo.getIsActive());
                topicInfoVo.setErrorMsg(topicVo.getErrorMsg());
                topicInfoVo.setTps(topicVo.getTps());
                if (null == topicInfoVo.getOffset()) {
                    topicInfoVo.setOffset(0L);
                }
                if (null == topicInfoVo.getLag()) {
                    topicInfoVo.setLag(0L);
                }
                result.add(topicInfoVo);
            }
        }
        result.sort(Comparator.comparing(TopicInfoVo::getHost));
        return result;
    }

    public Boolean refreshDbConfig() throws Exception {
        final Map<NodeVo, List<Boolean>> nodeVoListMap = postToAllLeader(REFRESH_DB_CONFIG, null, Boolean.class);
        return true;
    }

    public List<SettingVo> getSetting(final String topicName) throws Exception {
        final Map<String, Object> params = new HashMap<>();
        params.put("topicName", topicName);
        return postToTopic(topicName, SETTING_VO, params, SettingVo.class);
    }

    public Boolean installTopic(final NodeVo nodeVo,
                                final Long topicId) throws Exception {
        final Map<String, Object> params = new HashMap<>();
        params.put("topicId", topicId);
        final List<Boolean> post = post(nodeVo, INSTALL_TOPIC, params, Boolean.class);
        return post.get(0);
    }

    public Boolean uninstallTopic(final String topicName) throws Exception {
        final Map<String, Object> params = new HashMap<>();
        params.put("topicName", topicName);
        return postToTopicForOne(topicName, UNINSTALL_TOPIC, params, Boolean.class);
    }

    public void start(final String topicName,
                      final String offset,
                      final String timestamp) throws Exception {
        final Map<String, Object> params = new HashMap<>();
        params.put("topicName", topicName);
        params.put("partition", 0);
        params.put("offset", "");
        params.put("timestamp", "");
        postToTopic(topicName, START_TOPIC, params, Void.class);
    }

    public void start(final String topicName) throws Exception {
        start(topicName, "", "");
    }

    public void stop(final String topicName) throws Exception {
        final Map<String, Object> params = new HashMap<>();
        params.put("topicName", topicName);
        params.put("partition", 0);
        postToTopic(topicName, STOP_TOPIC, params, Void.class);
    }

    public void restart(final String topicName,
                        final String offset,
                        final String timestamp) throws Exception {
        String offsetTimestamp = "";
        if (!StringUtils.isEmpty(timestamp)) {
            offsetTimestamp = String.valueOf(Objects.requireNonNull(DateTool.parse(timestamp)).getTime());
        }

        final Map<String, Object> params = new HashMap<>();
        params.put("topicName", topicName);
        params.put("partition", 0);
        params.put("offset", offset);
        params.put("timestamp", offsetTimestamp);
        postToTopic(topicName, RE_START_TOPIC, params, Void.class);
    }

    public TopicOffset calcOffsetByTimestamp(final String topicName,
                                             final Long timestamp) throws Exception {
        final Map<String, Object> params = new HashMap<>();
        params.put("topicName", topicName);
        params.put("partition", 0);
        params.put("timestamp", timestamp);
        return postToTopicForOne(topicName, CALC_OFFSET_BY_TIMESTAMP, params, TopicOffset.class);
    }
}