package org.wyyt.db2es.admin.service.common;

import com.sijibao.nacos.spring.util.NacosRsaUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import org.wyyt.db2es.core.exception.Db2EsException;
import org.wyyt.sharding.auto.property.DataSourceProperty;
import org.wyyt.sharding.entity.DbInfo;
import org.wyyt.sharding.entity.ShardingResult;
import org.wyyt.sharding.service.ShardingService;
import org.wyyt.tool.db.CrudService;
import org.wyyt.tool.db.DataSourceTool;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The service used for database of db2es-admin
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Service
public class ShardingDbService implements DisposableBean {
    private final Map<DbInfo, CrudService> dataSourceMap;
    private final ShardingService shardingService;

    public Map<String, Object> getByIdValue(final String logicTableName,
                                            final String shardingValue,
                                            final String id) throws Exception {
        final String shardingColumn = this.shardingService.listShardingColumns(logicTableName).iterator().next();
        final ShardingResult shardingResult = this.shardingService.doSharding(logicTableName, shardingValue);
        return getById(shardingResult.getDatabaseName(), shardingResult.getTableName(), id);
    }

    public List<Map<String, Object>> getByShardingValue(final String logicTableName,
                                                        final String shardingValue) throws Exception {
        final String shardingColumn = this.shardingService.listShardingColumns(logicTableName).iterator().next();
        final ShardingResult shardingResult = this.shardingService.doSharding(logicTableName, shardingValue);
        return getByColumnValue(shardingResult.getDatabaseName(),
                shardingResult.getTableName(),
                new String[]{shardingColumn}, new String[]{shardingValue});
    }

    public void getByIdInTransaction(final CrudService.Handle handle,
                                     final String databaseName,
                                     final String tableName,
                                     final String id) throws Exception {
        if (StringUtils.isEmpty(databaseName) || StringUtils.isEmpty(tableName) || StringUtils.isEmpty(id)) {
            throw new Db2EsException("数据库名、表名和主键值都不允许为空");
        }

        final CrudService crudService = this.getByDatabaseName(databaseName);

        if (null == crudService) {
            throw new Db2EsException(String.format("不存在名为[%s]的数据源", databaseName));
        }

        crudService.queryInTransaction(handle, String.format("SELECT * FROM `%s` WHERE `id`=? FOR UPDATE", tableName), id);
    }

    public List<Map<String, Object>> getByColumnValue(final String databaseName,
                                                      final String tableName,
                                                      final String[] columnNames,
                                                      final String[] columnValues) throws Exception {
        if (StringUtils.isEmpty(databaseName) || StringUtils.isEmpty(tableName)) {
            throw new Db2EsException("数据库名和表名不允许为空");
        }

        final CrudService crudService = this.getByDatabaseName(databaseName);
        if (null == crudService) {
            throw new Db2EsException(String.format("不存在名为[%s]的数据源", databaseName));
        }
        final List<String> whereCaluse = new ArrayList<>();
        for (final String name : columnNames) {
            whereCaluse.add(String.format("`%s`=?", name));
        }

        return crudService.select(String.format("SELECT * FROM `%s` WHERE %s", tableName, StringUtils.join(whereCaluse, " AND")), (Object[]) columnValues);
    }

    public DataSource getDataSourceByDatabaseName(final String databaseName) {
        if (null == this.dataSourceMap || this.dataSourceMap.isEmpty()) {
            throw new Db2EsException("数据源为空, 请先配置数据源");
        }

        final CrudService crudService = this.getByDatabaseName(databaseName);
        if (null == crudService) {
            throw new Db2EsException(String.format("不存在数据库名为[%s]的数据源", databaseName));
        }
        return crudService.getDataSource();
    }

    public DataSource getDataSourceByName(final String name) {
        if (null == this.dataSourceMap || this.dataSourceMap.isEmpty()) {
            throw new Db2EsException("数据源为空, 请先配置数据源");
        }

        final CrudService crudService = this.getByName(name);
        if (null == crudService) {
            throw new Db2EsException(String.format("不存在名为[%s]的数据源", name));
        }
        return crudService.getDataSource();
    }

    public Map<String, Object> getById(final String databaseName,
                                       final String tableName,
                                       final String id) throws Exception {
        if (StringUtils.isEmpty(databaseName) || StringUtils.isEmpty(tableName) || StringUtils.isEmpty(id)) {
            throw new Db2EsException("数据库名、表名和主键值都不允许为空");
        }
        final CrudService crudService = this.getByDatabaseName(databaseName);
        if (null == crudService) {
            throw new Db2EsException(String.format("不存在名为[%s]的数据源", databaseName));
        }
        return crudService.selectOne(String.format("SELECT * FROM `%s` WHERE `id`=?", tableName), id);
    }

    public ShardingDbService(final ShardingService shardingService) {
        this.shardingService = shardingService;
        this.dataSourceMap = new HashMap<>();
        this.createDataSourceMap();
    }

    @Override
    public void destroy() {
        if (null == this.dataSourceMap) {
            return;
        }

        this.dataSourceMap.forEach((name, dataSource) -> dataSource.destroy());
        this.dataSourceMap.clear();
    }

    private void createDataSourceMap() {
        for (final Map.Entry<String, DataSourceProperty> pair : this.shardingService.listDataSourcePropertyMap().entrySet()) {
            final String name = pair.getKey();
            final DataSourceProperty dataSourceProperty = pair.getValue();
            this.dataSourceMap.put(new DbInfo(dataSourceProperty.getName(), dataSourceProperty.getDatabaseName()),
                    new CrudService(DataSourceTool.createHikariDataSource(
                            name,
                            dataSourceProperty.getHost(),
                            dataSourceProperty.getPort().toString(),
                            dataSourceProperty.getDatabaseName(),
                            dataSourceProperty.getUsername(),
                            NacosRsaUtils.decrypt(dataSourceProperty.getPassword()),
                            10,
                            20)));
        }
    }

    private CrudService getByDatabaseName(final String databaseName) {
        for (final Map.Entry<DbInfo, CrudService> pair : this.dataSourceMap.entrySet()) {
            if (pair.getKey().getDatabaseName().equals(databaseName)) {
                return pair.getValue();
            }
        }
        return null;
    }

    private CrudService getByName(final String name) {
        for (final Map.Entry<DbInfo, CrudService> pair : this.dataSourceMap.entrySet()) {
            if (pair.getKey().getName().equals(name)) {
                return pair.getValue();
            }
        }
        return null;
    }

    private int getDataSourceIndex(final String datasourceName) {
        final int i = datasourceName.lastIndexOf('_');
        return Integer.parseInt(datasourceName.substring(i + 1));
    }
}