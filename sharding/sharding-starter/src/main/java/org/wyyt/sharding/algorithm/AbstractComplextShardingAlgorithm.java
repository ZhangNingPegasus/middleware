package org.wyyt.sharding.algorithm;

import cn.hutool.core.util.StrUtil;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.wyyt.sharding.auto.property.DimensionProperty;
import org.wyyt.sharding.auto.property.TableProperty;
import org.wyyt.sharding.context.DbContext;
import org.wyyt.sharding.service.ShardingService;

import java.util.*;

/**
 * Base class for splitting of database and table algorithms
 * <p>
 * Let the total number of databases be M; the total number of tables is N, and the value of the split key is X
 * Then: the number of tables in each database is P = N / M
 * <p>
 * Database splitting algorithm: [murmur3_128(X) / P] % M
 * Table splitting algorithm: murmur3_128(X) % N
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public abstract class AbstractComplextShardingAlgorithm {
    @Autowired
    protected ShardingService shardingService;

    protected void sharding(final ComplexKeysShardingValue<Long> shardingValue,
                            final Action action) {
        final String logicTableName = shardingValue.getLogicTableName();
        final CaseInsensitiveMap<String, Collection<Long>> columnNameAndShardingValuesMap = new CaseInsensitiveMap<>(shardingValue.getColumnNameAndShardingValuesMap());

        final String keyDimensionName = DbContext.get();
        final DimensionProperty keyDimensionProperty = this.shardingService.getDimensionByName(keyDimensionName);
        String keyShardingColumn = null;

        if (null != keyDimensionProperty) {
            final TableProperty.DimensionInfo dimensionInfo = this.shardingService.getTableDimensionInfo(logicTableName, keyDimensionProperty.getName());
            keyShardingColumn = dimensionInfo.getShardingColumn();
        }

        final Set<String> shardingColumns = new LinkedHashSet<>();
        if (!StrUtil.isBlank(keyShardingColumn)) {
            shardingColumns.add(keyShardingColumn);
        }
        shardingColumns.addAll(this.shardingService.listShardingColumns(logicTableName));

        for (final String shardingColumn : shardingColumns) {
            if (!StrUtil.isBlank(keyShardingColumn) && !shardingColumn.equals(keyShardingColumn)) {
                continue;
            }

            if (columnNameAndShardingValuesMap.containsKey(shardingColumn)) {
                final DimensionProperty dimensionProperty = this.shardingService.getDimensionByShardingColumn(logicTableName, shardingColumn);
                final TableProperty.DimensionInfo dimensionInfo = this.shardingService.getTableDimensionInfo(logicTableName, dimensionProperty.getName());

                final int databaseNumCount = dimensionProperty.getDataSourceProperties().size();
                final int tableCount = dimensionInfo.getTableCountNum();

                final List<String> datasourceNames = new ArrayList<>(dimensionProperty.getDataSourceProperties().keySet());
                datasourceNames.sort(String::compareTo);

                final Collection<Long> shardingColumnValues = columnNameAndShardingValuesMap.get(shardingColumn);
                for (final Object shardingColumnValue : shardingColumnValues) {
                    action.action(String.valueOf(shardingColumnValue), databaseNumCount, tableCount, dimensionInfo, datasourceNames);
                }
                break;
            }
        }
    }

    protected interface Action {
        void action(final String shardingValue,
                    final int databaseNumCount,
                    final int tableCount,
                    final TableProperty.DimensionInfo dimensionInfo,
                    final List<String> datasourceNames);
    }
}