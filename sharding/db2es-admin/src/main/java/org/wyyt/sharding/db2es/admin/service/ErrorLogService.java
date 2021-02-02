package org.wyyt.sharding.db2es.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wyyt.sharding.db2es.admin.entity.dto.ErrorLog;
import org.wyyt.sharding.db2es.admin.mapper.ErrorLogMapper;
import org.wyyt.tool.anno.TranSave;

import java.util.Date;

/**
 * The service for table 'error_log'.
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class ErrorLogService extends ServiceImpl<ErrorLogMapper, ErrorLog> {
    private final RepairService repairService;

    public ErrorLogService(final RepairService repairService) {
        this.repairService = repairService;
    }

    @TranSave
    public void repair(final Long errorLogId) throws Exception {
        final ErrorLog errorLog = this.getById(errorLogId);
        if (null == errorLog) {
            return;
        }
        final String databaseName = errorLog.getDatabaseName();
        final String tableName = errorLog.getTableName();
        final String topicName = errorLog.getTopicName();
        final String id = errorLog.getPrimaryKeyValue();

        if (ObjectUtils.isEmpty(databaseName) ||
                ObjectUtils.isEmpty(tableName) ||
                ObjectUtils.isEmpty(topicName) ||
                ObjectUtils.isEmpty(id)) {
            return;
        }

        this.repairService.repair(databaseName, tableName, topicName, id);
        resolve(errorLogId);
    }

    @TranSave
    public void resolve(final Long errorLogId) {
        final UpdateWrapper<ErrorLog> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(ErrorLog::getId, errorLogId)
                .set(ErrorLog::getIsResolved, true);
        this.update(updateWrapper);
    }

    @TranSave
    public void deleteExpired(final int days) {
        final Date now = new Date();
        final Date date = DateUtils.addDays(now, -days);
        final QueryWrapper<ErrorLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().lt(ErrorLog::getRowCreateTime, date);
        this.remove(queryWrapper);
    }
}