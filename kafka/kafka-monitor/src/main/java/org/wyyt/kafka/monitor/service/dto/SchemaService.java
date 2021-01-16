package org.wyyt.kafka.monitor.service.dto;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.wyyt.kafka.monitor.anno.TranRead;
import org.wyyt.kafka.monitor.anno.TranSave;
import org.wyyt.kafka.monitor.config.PropertyConfig;
import org.wyyt.kafka.monitor.entity.dto.SysAdmin;
import org.wyyt.kafka.monitor.mapper.SchemaMapper;
import org.wyyt.tool.sql.SqlTool;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.wyyt.kafka.monitor.common.Constants.IGNORE_TABLE;

/**
 * The service for initiate the database and tables.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class SchemaService extends ServiceImpl<SchemaMapper, SysAdmin> {
    private final PropertyConfig propertyConfig;
    private final PartitionService partitionService;

    public SchemaService(final PropertyConfig propertyConfig,
                         final PartitionService partitionService) {
        this.propertyConfig = propertyConfig;
        this.partitionService = partitionService;
    }

    @TranSave
    public void createTableIfNotExists() {
        this.baseMapper.createTableIfNotExists(this.partitionService.getPartitionInfoList());
    }

    @TranRead
    public Set<String> listTables() {
        return this.baseMapper.listTables(SqlTool.removeMySqlQualifier(propertyConfig.getDbName()));
    }

    public Set<String> listPartitionTables() {
        final Set<String> tableList = this.listTables();
        final Set<String> result = new HashSet<>(tableList.size());
        for (final String tableName : tableList) {
            if (IGNORE_TABLE.contains(tableName.toLowerCase(Locale.ROOT))) {
                continue;
            }
            result.add(tableName);
        }
        return result;
    }

}
