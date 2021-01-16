package org.wyyt.sharding.db2es.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wyyt.tool.anno.TranRead;
import org.wyyt.tool.anno.TranSave;
import org.wyyt.sharding.db2es.admin.mapper.TopicMapper;
import org.wyyt.sharding.db2es.core.entity.persistent.Topic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The service for table 't_topic'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class TopicService extends ServiceImpl<TopicMapper, Topic> {

    @TranRead
    public Map<String, Topic> listTopicMap(final String searchName) {
        final Map<String, Topic> result = new HashMap<>();
        final List<Topic> topicList = listTopic(searchName);
        for (final Topic topic : topicList) {
            result.put(topic.getName(), topic);
        }
        return result;
    }

    @TranRead
    public List<Topic> listTopic(final String searchName) {
        final QueryWrapper<Topic> queryWrapper = new QueryWrapper<>();
        final LambdaQueryWrapper<Topic> lambda = queryWrapper.lambda();
        if (!ObjectUtils.isEmpty(searchName)) {
            lambda.like(Topic::getName, searchName);
        }
        lambda.select(Topic::getId,
                Topic::getName,
                Topic::getNumberOfShards,
                Topic::getNumberOfReplicas,
                Topic::getAliasOfYears,
                Topic::getRefreshInterval,
                Topic::getDescription,
                Topic::getRowCreateTime,
                Topic::getRowUpdateTime)
                .orderByAsc(Topic::getName);
        return this.list(queryWrapper);
    }

    @TranRead
    public Topic getByName(final String topicName) {
        final QueryWrapper<Topic> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Topic::getName, topicName);
        return this.getOne(queryWrapper);
    }

    @TranSave
    public void insertOrUpdate(final Topic newTopic) {
        final Topic dbTopic = getByName(newTopic.getName());
        if (null == dbTopic) {
            this.save(newTopic);
        } else {
            final UpdateWrapper<Topic> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda()
                    .eq(Topic::getId, dbTopic.getId())
                    .set(Topic::getNumberOfShards, newTopic.getNumberOfShards())
                    .set(Topic::getNumberOfReplicas, newTopic.getNumberOfReplicas())
                    .set(Topic::getRefreshInterval, newTopic.getRefreshInterval())
                    .set(Topic::getAliasOfYears, newTopic.getAliasOfYears())
                    .set(Topic::getMapping, newTopic.getMapping())
                    .set(Topic::getDescription, newTopic.getDescription());
            this.update(updateWrapper);
        }
    }

    @TranSave
    public void insertIfNotExists(final Topic newTopic) {
        final Topic dbTopic = getByName(newTopic.getName());
        if (null == dbTopic) {
            this.save(newTopic);
        }
    }
}