package org.wyyt.sharding.db2es.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.wyyt.sharding.db2es.core.entity.persistent.Topic;
import org.wyyt.sharding.db2es.core.entity.view.NodeVo;
import org.wyyt.sharding.db2es.core.exception.Db2EsException;
import org.wyyt.sharding.anno.TranRead;
import org.wyyt.sharding.anno.TranSave;
import org.wyyt.sharding.db2es.admin.entity.dto.TopicDb2Es;
import org.wyyt.sharding.db2es.admin.entity.vo.TopicDb2EsVo;
import org.wyyt.sharding.db2es.admin.entity.vo.TopicInfoVo;
import org.wyyt.sharding.db2es.admin.mapper.TopicDb2EsMapper;
import org.wyyt.sharding.db2es.admin.service.common.Db2EsHttpService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The service for table 't_topic_db2es'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Service
public class TopicDb2EsService extends ServiceImpl<TopicDb2EsMapper, TopicDb2Es> {
    private final Db2EsHttpService db2EsHttpService;
    private final TopicService topicService;

    public TopicDb2EsService(final Db2EsHttpService db2EsHttpService,
                             final TopicService topicService) {
        this.db2EsHttpService = db2EsHttpService;
        this.topicService = topicService;
    }

    @TranSave
    public void deleteTopic(final Long topicId) {
        final Topic topic = this.topicService.getById(topicId);

        if (null == topic) {
            throw new Db2EsException(String.format("不存在[id=%s]的主题", topicId));
        }

        final TopicDb2Es topicDb2Es = this.getByTopicId(topicId);
        if (null != topicDb2Es) {
            throw new Db2EsException(String.format("主题[%s]正在被[db2es.id=%s]的服务使用, 请先卸载再删除",
                    topic.getName(),
                    topicDb2Es.getDb2esId()));
        }

        this.topicService.removeById(topicId);
    }

    @TranSave
    public void remove(final Integer db2esId,
                       final Long topicId) throws Exception {
        final Topic topic = this.topicService.getById(topicId);
        if (null == topic) {
            throw new Db2EsException(String.format("不存在[topid id = %s]的主题", topicId));
        }

        final NodeVo nodeVo = this.db2EsHttpService.getNodeVoByDb2EsId(db2esId);
        if (null != nodeVo) {
            throw new Db2EsException(String.format("[DB2ES ID = %s]的服务器正在运行该主题[%s],请使用卸载功能完成删除", db2esId, topic.getName()));
        }

        this.delete(db2esId, topicId);
    }

    @TranSave
    public void installTopic(final Integer db2esId,
                             final Long topicId) throws Exception {
        final NodeVo nodeVo = db2EsHttpService.getNodeVoByDb2EsId(db2esId);
        if (null == nodeVo) {
            throw new Db2EsException(String.format("不存在[DB2ES ID = %s]的服务", db2esId));
        }

        final Topic topic = topicService.getById(topicId);
        if (null == topic) {
            throw new Db2EsException(String.format("不存在[topid id = %s]的主题", topicId));
        }

        TopicDb2Es topicDb2Es = getByTopicId(topicId);
        if (null != topicDb2Es) {
            throw new Db2EsException(String.format("[Topic Id = %s]的主题已经分配给DB2ES ID = %s",
                    topicDb2Es.getTopicId(),
                    topicDb2Es.getDb2esId()));
        }
        topicDb2Es = new TopicDb2Es();
        topicDb2Es.setDb2esId(db2esId);
        topicDb2Es.setTopicId(topicId);
        this.save(topicDb2Es);
        if (!this.db2EsHttpService.installTopic(nodeVo, topicId)) {
            throw new Db2EsException(String.format("主题[%s]安装失败", topic.getName()));
        }
    }

    @TranSave
    public void uninstallTopic(final String topicName) throws Exception {
        final List<TopicInfoVo> all = this.db2EsHttpService.getTopicVoList(topicName);
        final List<TopicInfoVo> topicInfoVoList = all.stream().filter(p -> p.getTopicName().equals(topicName)).collect(Collectors.toList());

        if (topicInfoVoList.isEmpty()) {
            throw new Db2EsException(String.format("主题[%s]尚未分派给任何的db2es_server", topicName));
        }
        if (topicInfoVoList.size() > 1) {
            throw new Db2EsException(String.format("主题[%s]发现异常, 存在于多个主机中:[%s]",
                    topicName,
                    topicInfoVoList.stream().map(TopicInfoVo::getHost).collect(Collectors.toList())));
        }

        final TopicInfoVo topicInfoVo = topicInfoVoList.get(0);
        if (topicInfoVo.getIsActive()) {
            throw new Db2EsException(String.format("主题[%s]正在运行中, 请先暂停再卸载", topicName));
        }

        final Topic topic = this.topicService.getByName(topicName);
        if (null == topic) {
            throw new Db2EsException(String.format("主题[%s]不存在", topicName));
        }

        final NodeVo nodeVo = this.db2EsHttpService.getNodeVoByTopicName(topicName);
        final Integer db2esId = nodeVo.getId();
        final Long topicId = topic.getId();
        this.delete(db2esId, topicId);
        if (!this.db2EsHttpService.uninstallTopic(topicName)) {
            throw new Db2EsException(String.format("主题[%s]卸载失败", topicName));
        }
    }

    @TranSave
    public void delete(final int db2esId, final long topicId) {
        final QueryWrapper<TopicDb2Es> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(TopicDb2Es::getDb2esId, db2esId)
                .eq(TopicDb2Es::getTopicId, topicId);
        this.remove(queryWrapper);
    }

    @TranRead
    public List<Topic> listUnused() {
        return this.baseMapper.listUnused();
    }

    @TranRead
    public TopicDb2Es getByTopicId(final Long topicId) {
        final QueryWrapper<TopicDb2Es> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TopicDb2Es::getTopicId, topicId);
        return this.getOne(queryWrapper);
    }

    @TranRead
    public Map<String, TopicDb2EsVo> listAll() {
        return this.baseMapper.listAll();
    }
}