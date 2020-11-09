package org.wyyt.kafka.monitor.service.dto;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.wyyt.kafka.monitor.anno.TranRead;
import org.wyyt.kafka.monitor.anno.TranSave;
import org.wyyt.kafka.monitor.config.PropertyConfig;
import org.wyyt.kafka.monitor.entity.dto.SysAdmin;
import org.wyyt.kafka.monitor.mapper.SchemaMapper;
import org.wyyt.tool.sql.SqlTool;

import java.util.Date;
import java.util.Set;

/**
 * The service for initiate the database and tables.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Service
public class SchemaService extends ServiceImpl<SchemaMapper, SysAdmin> {
    private final PropertyConfig propertyConfig;

    public SchemaService(final PropertyConfig propertyConfig) {
        this.propertyConfig = propertyConfig;
    }

    @TranSave
    public void createTableIfNotExists() {
        this.baseMapper.createTableIfNotExists();
    }

    @TranSave
    public void deleteExpired(final Set<String> tableNames) {
        final Date now = new Date();
        final Date date = DateUtils.addDays(now, -propertyConfig.getRetentionDays());
        this.baseMapper.deleteExpired(tableNames, date);
    }

    @TranRead
    public Set<String> listTables() {
        return this.baseMapper.listTables(SqlTool.removeMySqlQualifier(propertyConfig.getDbName()));
    }
}
