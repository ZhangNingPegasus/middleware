package org.wyyt.kafka.monitor.service.dto;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.wyyt.kafka.monitor.anno.TranRead;
import org.wyyt.kafka.monitor.anno.TranSave;
import org.wyyt.kafka.monitor.common.Constants;
import org.wyyt.kafka.monitor.entity.dto.SysTableName;
import org.wyyt.kafka.monitor.entity.dto.SysTopicLag;
import org.wyyt.kafka.monitor.entity.dto.SysTopicSize;
import org.wyyt.kafka.monitor.entity.po.TopicSizeLag;
import org.wyyt.kafka.monitor.entity.vo.KafkaConsumerVo;
import org.wyyt.kafka.monitor.entity.vo.OffsetVo;
import org.wyyt.kafka.monitor.entity.vo.TopicRecordCountVo;
import org.wyyt.kafka.monitor.mapper.SysTopicSizeMapper;
import org.wyyt.kafka.monitor.service.common.KafkaService;
import org.wyyt.tool.cache.CacheService;
import org.wyyt.tool.exception.BusinessException;
import org.wyyt.tool.exception.ExceptionTool;

import java.util.*;

/**
 * The service for table 'sys_topic_size'.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class SysTopicSizeService extends ServiceImpl<SysTopicSizeMapper, SysTopicSize> {
    private final static int BATCH_SIZE = 1024;
    private final CacheService cacheService;
    private final SysTableNameService sysTableNameService;
    private final KafkaService kafkaService;
    private final PartitionService partitionService;

    public SysTopicSizeService(final CacheService cacheService,
                               final SysTableNameService sysTableNameService,
                               final KafkaService kafkaService,
                               final PartitionService partitionService) {
        this.cacheService = cacheService;
        this.sysTableNameService = sysTableNameService;
        this.kafkaService = kafkaService;
        this.partitionService = partitionService;
    }

    @TranRead
    public Long getHistoryLogSize(final String topicName,
                                  final int pastDays) {
        if (pastDays < 0) {
            throw new BusinessException("天数必须大于等于0");
        }

        final String key = String.format("SysTopicSizeService::getHistoryLogSize:%s:%s", topicName, pastDays);

        Long result = this.cacheService.get(key);
        if (null == result) {
            final Calendar calendar = Calendar.getInstance();
            calendar.set(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DATE),
                    0,
                    0,
                    0);
            final Date now = calendar.getTime();

            final Date from = DateUtils.addDays(now, -pastDays);
            final Date to = DateUtils.addDays(from, 1);
            result = this.getHistoryLogSize(topicName, from, to);
            if (pastDays != 0) {
                this.cacheService.put(key, result);
            }
        }
        return result;
    }

    @TranRead
    public Long getHistoryLogSize(final String topicName,
                                  final Date from,
                                  final Date to) {
        try {
            final SysTableName sysTableName = this.sysTableNameService.getByTopicName(topicName);
            if (null == sysTableName) {
                return 0L;
            }
            return this.baseMapper.getHistoryLogSize(sysTableName.getRecordTableName(), from, to);
        } catch (final Exception e) {
            log.error(ExceptionTool.getRootCauseMessage(e), e);
            return 0L;
        }
    }

    @TranRead
    public Map<String, List<SysTopicSize>> listByTopicNames(List<String> topicNameList, Date from, Date to) {
        Map<String, List<SysTopicSize>> result = null;

        final QueryWrapper<SysTopicSize> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .in(SysTopicSize::getTopicName, topicNameList)
                .ge(SysTopicSize::getRowCreateTime, from)
                .le(SysTopicSize::getRowCreateTime, to)
                .orderByAsc(SysTopicSize::getTopicName)
                .orderByDesc(SysTopicSize::getRowCreateTime);
        final List<SysTopicSize> sysLogSizeList = this.list(queryWrapper);

        if (null != sysLogSizeList && !sysLogSizeList.isEmpty()) {
            result = new HashMap<>((int) (sysLogSizeList.size() / 0.75));
            for (final SysTopicSize sysLogSize : sysLogSizeList) {
                final String key = sysLogSize.getTopicName();
                if (result.containsKey(key)) {
                    result.get(key).add(sysLogSize);
                } else {
                    final List<SysTopicSize> value = new ArrayList<>();
                    value.add(sysLogSize);
                    result.put(key, value);
                }
            }
        }
        return result;
    }

    @TranSave
    public void deleteTopic(final String topicName) {
        final QueryWrapper<SysTopicSize> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysTopicSize::getTopicName, topicName);
        this.remove(queryWrapper);
    }

    public TopicSizeLag kpi(Date now) throws Exception {
        final TopicSizeLag result = new TopicSizeLag();
        final List<SysTopicSize> sysTopicSizeList = this.getRecordsCount(now);
        final List<SysTopicLag> sysTopicLagList = new ArrayList<>(BATCH_SIZE);
        final List<KafkaConsumerVo> kafkaConsumerVoList = this.kafkaService.listKafkaConsumers();
        for (final KafkaConsumerVo kafkaConsumerVo : kafkaConsumerVoList) {
            final Set<String> topicNames = kafkaConsumerVo.getTopicNames();
            for (final String topicName : topicNames) {
                if (Constants.KAFKA_SYSTEM_TOPIC.contains(topicName)) {
                    continue;
                }
                long lag = 0L;
                long offset = 0L;
                try {
                    final List<OffsetVo> offsetVoList = this.kafkaService.listOffsetVo(kafkaConsumerVoList, kafkaConsumerVo.getGroupId(), topicName);
                    for (final OffsetVo offsetVo : offsetVoList) {
                        if (null != offsetVo.getLag() && offsetVo.getLag() > 0) {
                            lag += offsetVo.getLag();
                        }
                        if (null != offsetVo.getOffset() && offsetVo.getOffset() > 0) {
                            offset += offsetVo.getOffset();
                        }
                    }
                    final SysTopicLag sysTopicLag = new SysTopicLag();
                    sysTopicLag.setGroupId(kafkaConsumerVo.getGroupId());
                    sysTopicLag.setTopicName(topicName);
                    sysTopicLag.setOffset(offset);
                    sysTopicLag.setLag(lag);
                    sysTopicLag.setRowCreateTime(now);
                    sysTopicLagList.add(sysTopicLag);
                } catch (Exception exception) {
                    log.error(ExceptionTool.getRootCauseMessage(exception), exception);
                }
            }
        }
        result.setSysTopicLagList(sysTopicLagList);
        result.setSysTopicSizeList(sysTopicSizeList);
        return result;
    }

    private List<SysTopicSize> getRecordsCount(final Date now) throws Exception {
        final List<SysTopicSize> result = new ArrayList<>();
        final List<String> topicNames = this.kafkaService.listTopicNames();
        final Map<String, Long> topicLogSizeMap = this.kafkaService.getTopicLogSizes(topicNames);

        for (final Map.Entry<String, Long> pair : topicLogSizeMap.entrySet()) {
            final String topicName = pair.getKey();
            final SysTopicSize sysLogSize = new SysTopicSize(topicName, topicLogSizeMap.get(topicName));
            sysLogSize.setRowCreateTime(now);
            result.add(sysLogSize);
        }
        return result;
    }

    @TranRead
    public List<SysTopicSize> listByTopicName(final String topicName,
                                              final Date from,
                                              final Date to) {
        final QueryWrapper<SysTopicSize> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(SysTopicSize::getTopicName, topicName)
                .ge(SysTopicSize::getRowCreateTime, from)
                .le(SysTopicSize::getRowCreateTime, to)
                .orderByAsc(SysTopicSize::getRowCreateTime);
        return this.list(queryWrapper);
    }

    @TranRead
    public List<SysTopicSize> getTopicRank(final Integer rank,
                                           @Nullable final Date from,
                                           @Nullable final Date to) {
        return this.baseMapper.getTopicRank(rank, from, to);
    }

    @TranRead
    public Long getTotalRecordCount(final int recentDays) {
        final Date today = DateUtil.parse(DateUtil.today(), Constants.DATE_FORMAT);
        final Date toDate = DateUtils.addDays(today, 1);
        final Date fromDate = DateUtils.addDays(toDate, -recentDays + 1);
        final String toPartition = partitionService.generatePartitionName(toDate);
        final String fromPartition = partitionService.generatePartitionName(fromDate);
        return this.baseMapper.getTotalRecordCount(fromPartition, toPartition);
    }

    @TranRead
    public List<TopicRecordCountVo> listTotalRecordCount(final int top) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE),
                0,
                0,
                0);
        Date from0 = calendar.getTime();
        Date to0 = DateUtils.addDays(from0, 1);

        Date from1 = DateUtils.addDays(from0, -1);
        Date to1 = DateUtils.addDays(from1, 1);

        return this.baseMapper.listTotalRecordCount(top, from0, to0, from1, to1);
    }
}