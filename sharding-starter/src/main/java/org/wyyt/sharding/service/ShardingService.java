package org.wyyt.sharding.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.wyyt.sharding.algorithm.MathsTool;
import org.wyyt.sharding.auto.property.DataSourceProperty;
import org.wyyt.sharding.auto.property.DimensionProperty;
import org.wyyt.sharding.auto.property.ShardingProperty;
import org.wyyt.sharding.auto.property.TableProperty;
import org.wyyt.sharding.cache.anno.EhCache;
import org.wyyt.sharding.entity.DbInfo;
import org.wyyt.sharding.entity.FieldInfo;
import org.wyyt.sharding.entity.IndexInfo;
import org.wyyt.sharding.entity.ShardingResult;
import org.wyyt.sharding.exception.ShardingException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * the service of providing ShardingSphere related methods
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
public class ShardingService {
    private final ShardingProperty shardingProperty;
    private final Map<DbInfo, DataSource> dataSourceMap;

    public ShardingService(final ShardingProperty shardingProperty,
                           final Map<DbInfo, DataSource> dataSourceMap) {
        this.shardingProperty = shardingProperty;
        this.dataSourceMap = dataSourceMap;
    }

    public final ShardingResult doSharding(final String logicTableName,
                                           final Object shardingValue) {
        Assert.notNull(shardingValue, "拆分键值不允许为空");
        final Set<String> shardingColumns = this.listShardingColumns(logicTableName);

        if (1 == shardingColumns.size()) {
            final ShardingResult result = new ShardingResult();
            final String shardingColumn = shardingColumns.iterator().next();

            final DimensionProperty dimensionProperty = this.getDimensionByShardingColumn(logicTableName, shardingColumn);
            final TableProperty.DimensionInfo dimensionInfo = this.getTableDimensionInfo(logicTableName, dimensionProperty.getName());

            final int databaseNumCount = dimensionProperty.getDataSourceProperties().size();
            final int tableCount = dimensionInfo.getTableCountNum();

            result.setDatabaseIndex(MathsTool.doDatabaseSharding(shardingValue.toString(), databaseNumCount, tableCount));
            result.setTableIndex(MathsTool.doTableSharding(shardingValue.toString(), tableCount));

            result.setTableDimensionInfo(dimensionInfo);
            result.setDataSourceProperty(this.getDataSourcePropertyByIndex(result.getDatabaseIndex()));
            return result;
        }

        throw new ShardingException(String.format("逻辑表[%s]中没有设定拆分键或设置了多个拆分键, 请指定具体的拆分键[%s]", logicTableName, StringUtils.join(shardingColumns, ", ")));
    }

    public final Map<Integer, Set<Object>> doDatabaseSharding(final String logicTableName,
                                                              final String shardingColumn,
                                                              final List<Object> shardingValueList) {
        Assert.notEmpty(shardingValueList, "拆分键值列表不允许为空");

        final DimensionProperty dimensionProperty = this.getDimensionByShardingColumn(logicTableName, shardingColumn);
        final TableProperty.DimensionInfo dimensionInfo = this.getTableDimensionInfo(logicTableName, dimensionProperty.getName());

        final int databaseNumCount = dimensionProperty.getDataSourceProperties().size();
        final int tableCount = dimensionInfo.getTableCountNum();

        return MathsTool.doDatabaseSharding(shardingValueList, databaseNumCount, tableCount);
    }

    public final Map<Integer, Set<Object>> doDatabaseSharding(final String logicTableName,
                                                              final List<Object> shardingValueList) {
        Assert.notEmpty(shardingValueList, "拆分键值列表不允许为空");

        final Set<String> shardingColumns = this.listShardingColumns(logicTableName);
        if (1 == shardingColumns.size()) {
            return doDatabaseSharding(logicTableName, shardingColumns.iterator().next(), shardingValueList);
        }
        throw new ShardingException(String.format("逻辑表[%s]中没有设定拆分键或设置了多个拆分键, 请指定具体的拆分键[%s]",
                logicTableName,
                StringUtils.join(shardingColumns, ", ")));
    }

    @EhCache
    public boolean needSharding(final String logicTableName) {
        for (final TableProperty tableProperty : this.shardingProperty.getTableProperties()) {
            if (tableProperty.getName().equals(logicTableName)) {
                return true;
            }
        }
        return false;
    }

    @EhCache
    public DimensionProperty getDimensionByName(final String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        return this.shardingProperty.getDimensionProperties().get(name);
    }

    @EhCache
    public TableProperty.DimensionInfo getTableDimensionInfo(final String logicTableName,
                                                             final String dimensionName) {
        final Optional<TableProperty> first = shardingProperty.getTableProperties().stream().filter(p -> p.getName().equals(logicTableName)).findFirst();
        if (first.isPresent()) {
            if (!first.get().getDimensionInfos().containsKey(dimensionName)) {
                throw new ShardingException(String.format("在表[%s]配置中, 没有找到关于维度[%s]的配置", logicTableName, dimensionName));
            }
            return first.get().getDimensionInfos().get(dimensionName);
        }
        throw new ShardingException(String.format("没有找到逻辑表[%s]相关的配置", logicTableName));
    }

    @EhCache
    public String getPrimaryDimensionShardingColumn(final String logicTableName) {
        final DimensionProperty primaryDimension = getPrimaryDimension();
        return getTableDimensionInfo(logicTableName, primaryDimension.getName()).getShardingColumn();
    }

    @EhCache
    public DimensionProperty getPrimaryDimensionProperty(final String logicTableName) {
        final String primaryDimensionShardingColumn = this.getPrimaryDimensionShardingColumn(logicTableName);
        return this.getDimensionByShardingColumn(logicTableName, primaryDimensionShardingColumn);
    }

    @EhCache
    public DimensionProperty getPrimaryDimension() {
        final Optional<DimensionProperty> first = this.shardingProperty.getDimensionProperties().values().stream().filter(p -> p.getPriority().equals(0)).findFirst();
        return first.orElse(null);
    }

    @EhCache
    public Set<String> listShardingColumns(final String logicTableName) {
        final Set<String> result = new HashSet<>();
        final List<TableProperty> tablePropertyList = listTableByName(logicTableName);
        for (final TableProperty tableProperty : tablePropertyList) {
            result.addAll(tableProperty.getDimensionInfos().values().stream()
                    .sorted(Comparator.comparingInt(o -> o.getDimensionProperty().getPriority()))
                    .map(TableProperty.DimensionInfo::getShardingColumn)
                    .collect(Collectors.toSet()));
        }
        return result;
    }

    @EhCache
    public DimensionProperty getDimensionByShardingColumn(final String logicTableName,
                                                          final String shardingColumn) {
        final List<DimensionProperty> result = new ArrayList<>();
        final List<TableProperty> tablePropertyList = listTableByName(logicTableName);
        for (final TableProperty tableProperty : tablePropertyList) {
            result.addAll(tableProperty.getDimensionInfos().values().stream()
                    .filter(p -> p.getShardingColumn().equals(shardingColumn))
                    .sorted(Comparator.comparingInt(o -> o.getDimensionProperty().getPriority()))
                    .map(TableProperty.DimensionInfo::getDimensionProperty)
                    .collect(Collectors.toList()));
        }

        if (result.isEmpty()) {
            throw new ShardingException(String.format("没有找到拆分键[%s]相关的配置", shardingColumn));
        }
        return result.get(0);
    }

    @EhCache
    public List<TableProperty> listTableByName(final String logicTableName) {
        List<TableProperty> result = this.shardingProperty.getTableProperties().stream().filter(p -> p.getName().equals(logicTableName)).collect(Collectors.toList());
        if (result.isEmpty()) {
            throw new ShardingException(String.format("没有找到逻辑表名[%s]相关的配置", logicTableName));
        }
        return result;
    }

    @EhCache
    public List<String> listBroadcastTables() {
        return this.shardingProperty.getTableProperties().stream().filter(TableProperty::getBroadcast).map(TableProperty::getName).collect(Collectors.toList());
    }

    @EhCache
    public List<String> listBindingTables() {
        final List<String> result = new ArrayList<>();
        final Map<String, List<String>> bindingGroupMap = new HashMap<>();
        for (final TableProperty tableProperty : this.shardingProperty.getTableProperties()) {
            final String bindingGroupName = tableProperty.getBindingGroupName();
            final String logicTableName = tableProperty.getName();
            if (bindingGroupMap.containsKey(bindingGroupName)) {
                bindingGroupMap.get(bindingGroupName).add(logicTableName);
            } else {
                final List<String> logicTableNames = new ArrayList<>();
                logicTableNames.add(logicTableName);
                bindingGroupMap.put(bindingGroupName, logicTableNames);
            }
        }
        bindingGroupMap.forEach((s, v) -> result.add(StringUtils.join(v, ",")));
        return result;
    }

    @EhCache
    public List<String> listActualDataNodes(final String logicTableName) {
        final List<String> result = new ArrayList<>();
        final List<TableProperty> tablePropertyList = this.shardingProperty.getTableProperties().stream().filter(p -> p.getName().equals(logicTableName)).collect(Collectors.toList());

        for (final TableProperty tableProperty : tablePropertyList) {
            tableProperty.getDimensionInfos().forEach((s, dimensionInfo) -> {

                final String tableName = dimensionInfo.getTableNameFormat();
                final Integer tableCountNum = dimensionInfo.getTableCountNum();

                final List<String> dbNames = dimensionInfo.getDimensionProperty().getDataSourceProperties().values().stream().map(DataSourceProperty::getName).sorted(String::compareTo).collect(Collectors.toList());

                final int tableCountPerDatabase = tableCountNum / dbNames.size();
                int startIndex = 0;
                int endIndex;

                for (final String dbName : dbNames) {
                    endIndex = startIndex + tableCountPerDatabase - 1;
                    final String groovy = String.format("${%s..%s}", startIndex, endIndex);
                    final String tbName = String.format(tableName, groovy);
                    result.add(String.format("%s.%s", dbName, tbName));
                    startIndex = endIndex + 1;
                }
            });
        }

        result.sort(String::compareTo);
        return result;
    }

    @EhCache
    public Map<String, DimensionProperty> listDimensionPropertyMap() {
        return this.shardingProperty.getDimensionProperties();
    }

    @EhCache
    public List<DimensionProperty> listDimensionProperties() {
        final List<DimensionProperty> dimensionProperties = new ArrayList<>(listDimensionPropertyMap().values());
        dimensionProperties.sort(Comparator.comparing(DimensionProperty::getPriority));
        return dimensionProperties;
    }

    @EhCache
    public Map<String, DataSourceProperty> listDataSourcePropertyMap() {
        return this.shardingProperty.getDataSourceProperties();
    }

    @EhCache
    public List<DataSourceProperty> listDataSourceProperties(String dimension) {
        final DimensionProperty dimensionProperty = listDimensionPropertyMap().get(dimension);
        if (null != dimensionProperty) {
            final List<DataSourceProperty> dataSourceProperties = new ArrayList<>(dimensionProperty.getDataSourceProperties().values());
            dataSourceProperties.sort(Comparator.comparing(DataSourceProperty::getName));
            return dataSourceProperties;
        }
        return null;
    }

    @EhCache
    public List<TableProperty> listTableProperties() {
        return this.shardingProperty.getTableProperties();
    }

    @EhCache
    public TableProperty getTableProperty(final String logicTableName) {
        final List<TableProperty> tableProperties = this.shardingProperty.getTableProperties();
        for (final TableProperty tableProperty : tableProperties) {
            if (tableProperty.getName().equals(logicTableName)) {
                return tableProperty;
            }
        }
        return null;
    }

    @EhCache
    public List<TableProperty> listTableProperties(final String dimension,
                                                   final String datasource) {
        final List<TableProperty> result = new ArrayList<>();
        final List<TableProperty> tableProperties = listTableProperties();
        if (null != tableProperties) {
            for (final TableProperty tableProperty : tableProperties) {
                if (tableProperty.getDimensionInfos().containsKey(dimension)) {
                    final TableProperty.DimensionInfo dimensionInfo = tableProperty.getDimensionInfos().get(dimension);
                    if (dimensionInfo.getDimensionProperty().getDataSourceProperties().containsKey(datasource)) {
                        result.add(tableProperty);
                    }
                }
            }
        }
        result.sort(Comparator.comparing(TableProperty::getName));
        return result;
    }

    @EhCache
    public boolean isShowSql() {
        return this.shardingProperty.isShowSql();
    }

    @EhCache
    public DataSourceProperty getDataSourcePropertyByIndex(final int index) {
        return this.shardingProperty.getDataSourceProperties().values().stream().sorted(Comparator.comparing(DataSourceProperty::getName)).collect(Collectors.toList()).get(index);
    }

    @EhCache
    public DataSourceProperty getDataSourcePropertyByName(final String name) {
        for (final Map.Entry<String, DataSourceProperty> pair : this.shardingProperty.getDataSourceProperties().entrySet()) {
            if (pair.getValue().getName().equals(name)) {
                return pair.getValue();
            }
        }
        return null;
    }

    @EhCache
    public DataSource getDataSourceByName(final String name) {
        if (null == this.dataSourceMap || this.dataSourceMap.isEmpty()) {
            return null;
        }

        for (final Map.Entry<DbInfo, DataSource> pair : this.dataSourceMap.entrySet()) {
            if (pair.getKey().getName().equals(name)) {
                return pair.getValue();
            }
        }
        return null;
    }

    @EhCache
    public DataSource getDataSourceByDatabaseName(final String databaseName) {
        if (null == this.dataSourceMap || this.dataSourceMap.isEmpty()) {
            return null;
        }
        for (final Map.Entry<DbInfo, DataSource> pair : this.dataSourceMap.entrySet()) {
            if (pair.getKey().getDatabaseName().equals(databaseName)) {
                return pair.getValue();
            }
        }
        return null;
    }


    public final Map<String, List<FieldInfo>> listFieldsMap(final String logicTable) throws Exception {
        return listFieldsMap(logicTable, null);
    }

    public final Map<String, List<FieldInfo>> listFieldsMap(final String logicTable,
                                                            final Integer limit) throws Exception {
        final Map<String, List<FieldInfo>> result = new HashMap<>();
        final List<TableProperty> tableProperties = this.listTableProperties();
        for (final TableProperty tableProperty : tableProperties) {
            final String table = tableProperty.getName();
            if (!logicTable.equalsIgnoreCase(table)) {
                continue;
            }
            for (final Map.Entry<String, TableProperty.DimensionInfo> pairDimension : tableProperty.getDimensionInfos().entrySet()) {
                final String dimensionName = pairDimension.getKey();
                for (final Map.Entry<String, DataSourceProperty> pairDataSource : pairDimension.getValue().getDimensionProperty().getDataSourceProperties().entrySet()) {
                    final String databaseName = pairDataSource.getKey();
                    final List<String> factTables = this.getFactTables(dimensionName, databaseName, table);

                    for (final String factTable : factTables) {
                        final List<FieldInfo> fieldInfoList = this.listFields(pairDataSource.getValue(), factTable);
                        if (result.containsKey(factTable)) {
                            result.get(factTable).addAll(fieldInfoList);
                        } else {
                            result.put(factTable, fieldInfoList);
                        }
                        if (null != limit && result.size() >= limit) {
                            return result;
                        }
                    }
                }
            }
        }
        return result;
    }

    public final Map<String, List<IndexInfo>> listIndexsMap(final String logicTable) throws Exception {
        final Map<String, List<IndexInfo>> result = new HashMap<>();
        final List<TableProperty> tableProperties = this.listTableProperties();

        for (final TableProperty tableProperty : tableProperties) {
            final String table = tableProperty.getName();
            if (!logicTable.equalsIgnoreCase(table)) {
                continue;
            }
            for (final Map.Entry<String, TableProperty.DimensionInfo> pairDimension : tableProperty.getDimensionInfos().entrySet()) {
                final String dimensionName = pairDimension.getKey();
                for (final Map.Entry<String, DataSourceProperty> pairDataSource : pairDimension.getValue().getDimensionProperty().getDataSourceProperties().entrySet()) {
                    final String databaseName = pairDataSource.getKey();
                    final List<String> factTables = this.getFactTables(dimensionName, databaseName, table);
                    for (final String factTable : factTables) {
                        final List<IndexInfo> indexInfoList = this.listIndex(pairDataSource.getValue(), factTable);
                        if (result.containsKey(factTable)) {
                            result.get(factTable).addAll(indexInfoList);
                        } else {
                            result.put(factTable, indexInfoList);
                        }
                    }
                }
            }
        }
        return result;
    }

    public final List<String> diffFields(final String logicTable) throws Exception {
        final Map<String, List<FieldInfo>> fieldsMap = this.listFieldsMap(logicTable);
        return anaylseDiffTableList(fieldsMap, FieldInfo::getName);
    }

    public final List<String> diffIndexs(final String logicTable) throws Exception {
        final Map<String, List<IndexInfo>> indexsMap = this.listIndexsMap(logicTable);
        return anaylseDiffTableList(indexsMap, IndexInfo::getIndexName);
    }

    public final List<FieldInfo> listFields(final DataSource dataSource, final String factTable) throws Exception {
        final List<FieldInfo> result = new ArrayList<>();
        query(kvs -> {
                    for (final Map<String, Object> kv : kvs) {
                        FieldType fieldType = analyseFieldType(kv.get("COLUMN_TYPE").toString().trim());
                        FieldInfo fieldInfo = new FieldInfo();
                        fieldInfo.setName(kv.get("COLUMN_NAME").toString());
                        fieldInfo.setDataType(kv.get("DATA_TYPE").toString());
                        fieldInfo.setTypeDesc(kv.get("COLUMN_TYPE").toString());
                        fieldInfo.setType(fieldType.getType());
                        fieldInfo.setSize(fieldType.getLength());
                        fieldInfo.setDecimal(fieldType.getDecimal());
                        fieldInfo.setNotNull("YES".equalsIgnoreCase(kv.get("IS_NULLABLE").toString()));
                        fieldInfo.setIsPrimary("PRI".equalsIgnoreCase(kv.get("COLUMN_KEY").toString()));
                        fieldInfo.setAutoUpdateByTimestampt(kv.get("EXTRA").toString().contains("on update CURRENT_TIMESTAMP"));
                        fieldInfo.setUnsigned(fieldType.getUnsigned());
                        fieldInfo.setAutoIncrement(kv.get("EXTRA").toString().contains("auto_increment"));
                        fieldInfo.setDefaultValue(kv.get("COLUMN_DEFAULT").toString());
                        fieldInfo.setKey(kv.get("COLUMN_KEY").toString());
                        fieldInfo.setComment(kv.get("COLUMN_COMMENT").toString());
                        result.add(fieldInfo);
                    }
                }, dataSource,
                "SELECT IFNULL(`COLUMN_NAME`,'') AS `COLUMN_NAME`,IFNULL(`DATA_TYPE`, '') AS `DATA_TYPE`,IFNULL(`COLUMN_TYPE`,'') AS `COLUMN_TYPE`,IFNULL(`COLUMN_DEFAULT`,'') AS `COLUMN_DEFAULT`,IFNULL(`IS_NULLABLE`,'') AS `IS_NULLABLE`,IFNULL(`COLUMN_KEY`,'') AS `COLUMN_KEY`, IFNULL(`CHARACTER_SET_NAME`,'') AS CHARACTER_SET_NAME,IFNULL(`COLLATION_NAME`,'') AS COLLATION_NAME,IFNULL(`EXTRA`,'') AS EXTRA,IFNULL(`COLUMN_COMMENT`,'') AS `COLUMN_COMMENT` FROM `information_schema`.`COLUMNS` WHERE `TABLE_NAME`=? ORDER BY `ORDINAL_POSITION` ASC",
                factTable);
        return result;
    }

    public final List<FieldInfo> listFields(final DataSourceProperty dataSourceProperty,
                                            final String factTable) throws Exception {
        final DataSource dataSource = this.getDataSourceByName(dataSourceProperty.getName());
        return listFields(dataSource, factTable);
    }

    public final List<FieldInfo> listFields(final String dimension,
                                            final String datasource,
                                            final String logicTable) throws Exception {
        final DimensionProperty dimensionProperty = this.getDimensionByName(dimension);
        final DataSourceProperty dataSourceProperty = dimensionProperty.getDataSourceProperties().get(datasource);
        final String factTable = getFactTableNameRandom(dimension, datasource, logicTable);
        return listFields(dataSourceProperty, factTable);
    }

    public final List<IndexInfo> listIndex(final DataSourceProperty dataSourceProperty,
                                           final String factTable) throws Exception {
        final List<IndexInfo> result = new ArrayList<>();

        query(kvs -> {
            final Map<String, IndexInfo> indexInfoMap = new HashMap<>((int) (result.size() / 0.75));
            for (final Map<String, Object> kv : kvs) {
                IndexInfo indexInfo = new IndexInfo();
                indexInfo.setIndexName(kv.get("Key_name").toString());
                indexInfo.setFieldName(kv.get("Column_name").toString());
                indexInfo.setType("0".equals(kv.get("Non_unique").toString()) ? "UNIQUE" : "NORMAL");
                indexInfo.setMethod(kv.get("Index_type").toString());
                indexInfo.setComment(kv.get("Index_comment").toString());
                if (indexInfoMap.containsKey(indexInfo.getIndexName())) {
                    IndexInfo buffer = indexInfoMap.get(indexInfo.getIndexName());
                    String newIndexName = buffer.getIndexName().concat(", ").concat(indexInfo.getIndexName());
                    buffer.setIndexName(newIndexName);
                } else {
                    indexInfoMap.put(indexInfo.getIndexName(), indexInfo);
                    result.add(indexInfo);
                }
            }
        }, dataSourceProperty, String.format("SHOW INDEX FROM `%s` WHERE `Key_name`!='PRIMARY'", factTable));
        return result;
    }

    public final List<IndexInfo> listIndex(final String dimension,
                                           final String datasource,
                                           final String logicTable) throws Exception {
        final DimensionProperty dimensionProperty = this.getDimensionByName(dimension);
        final DataSourceProperty dataSourceProperty = dimensionProperty.getDataSourceProperties().get(datasource);
        final String factTable = this.getFactTableNameRandom(dimension, datasource, logicTable);
        return this.listIndex(dataSourceProperty, factTable);
    }

    public final List<String> getFactTables(final String dimension,
                                            final String datasource,
                                            final String logicTable) throws Exception {
        final List<String> result1 = new ArrayList<>();
        final List<String> result2 = new ArrayList<>();

        final DimensionProperty dimensionProperty = this.getDimensionByName(dimension);
        final DataSourceProperty dataSourceProperty = dimensionProperty.getDataSourceProperties().get(datasource);

        final TableProperty.DimensionInfo tableDimensionInfo = this.getTableDimensionInfo(logicTable, dimension);

        for (int index = 0; index < tableDimensionInfo.getTableCountNum(); index++) {
            result1.add(String.format(tableDimensionInfo.getTableNameFormat(), index));
        }

        query(kvs -> {
            for (final Map<String, Object> kv : kvs) {
                result2.add(kv.get("TABLE_NAME").toString());
            }
        }, dataSourceProperty, String.format("SELECT `TABLE_NAME` FROM `information_schema`.`TABLES` WHERE `TABLE_SCHEMA`='%s' AND `TABLE_NAME` LIKE '%s%%'", dataSourceProperty.getDatabaseName(), logicTable));
        result1.retainAll(result2);
        return result1;
    }

    public final String getFactTableNameRandom(final String dimension,
                                               final String datasource,
                                               final String logicTable) throws Exception {
        final AtomicReference<String> result = new AtomicReference<>("");
        final DimensionProperty dimensionProperty = this.getDimensionByName(dimension);
        final DataSourceProperty dataSourceProperty = dimensionProperty.getDataSourceProperties().get(datasource);

        query(kvs -> {
            if (!kvs.isEmpty()) {
                result.set(kvs.get(0).get("TABLE_NAME").toString());
            }
        }, dataSourceProperty, String.format("SELECT `TABLE_NAME` FROM `information_schema`.`TABLES` WHERE `TABLE_SCHEMA`='%s' AND `TABLE_NAME` LIKE '%s%%' LIMIT 1", dataSourceProperty.getDatabaseName(), logicTable));
        return result.get();
    }

    private <T> List<String> anaylseDiffTableList(final Map<String, List<T>> source,
                                                  final Function<? super T, ? extends String> fieldComparator) {
        final List<String> result = new ArrayList<>();
        final Map<String, Long> fieldsHashMap = new HashMap<>();

        for (final Map.Entry<String, List<T>> pair : source.entrySet()) {
            String targetName = pair.getKey();
            List<T> targetValue = pair.getValue();
            targetValue.sort(Comparator.comparing(fieldComparator));
            String strFieldVoList = StringUtils.join(targetValue, ",");
            Long hash = MathsTool.hash(strFieldVoList);
            fieldsHashMap.put(targetName, hash);
        }

        long hash = 0L;
        long max = 0L;
        final Map<Long, Long> collect = fieldsHashMap.values().stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        for (final Map.Entry<Long, Long> pair : collect.entrySet()) {
            if (pair.getValue() > max) {
                max = pair.getValue();
                hash = pair.getKey();
            }
        }

        for (final Map.Entry<String, Long> pair : fieldsHashMap.entrySet()) {
            if (!pair.getValue().equals(hash)) {
                result.add(pair.getKey());
            }
        }

        result.sort(Comparator.naturalOrder());
        return result;
    }

    private void query(final Handle handle,
                       final DataSource dataSource,
                       final String sql,
                       final Object... params) throws Exception {
        List<Map<String, Object>> kv;
        ResultSet resultSet = null;

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
            resultSet = preparedStatement.executeQuery();
            final ResultSetMetaData metaData = resultSet.getMetaData();
            final int columnCount = metaData.getColumnCount();
            kv = new ArrayList<>(resultSet.getRow());
            final List<String> columnLabelList = new ArrayList<>(columnCount);
            for (int i = 0; i < columnCount; i++) {
                columnLabelList.add(metaData.getColumnLabel(i + 1));
            }
            while (resultSet.next()) {
                final Map<String, Object> row = new HashMap<>();
                for (final String columnLabel : columnLabelList) {
                    row.put(columnLabel, resultSet.getObject(columnLabel));
                }
                kv.add(row);
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        handle.process(kv);
    }

    private void query(final Handle handle,
                       final DataSourceProperty dataSourceProperty,
                       final String sql,
                       final Object... params) throws Exception {
        final DataSource dataSource = this.getDataSourceByName(dataSourceProperty.getName());
        query(handle, dataSource, sql, params);
    }

    private FieldType analyseFieldType(final String columnType) {
        int start = columnType.indexOf("(");
        int end = columnType.lastIndexOf(")");

        String name;
        int length = 0;
        int deciaml = 0;
        boolean unsigned;

        if (start > -1) {
            name = columnType.substring(0, start);
            final String precision = columnType.substring(start + 1, end);
            final int commaIndex = precision.indexOf(",");
            if (commaIndex > -1) {
                final String[] all = precision.split(",");
                length = Integer.parseInt(all[0]);
                deciaml = Integer.parseInt(all[1]);
            } else {
                length = Integer.parseInt(precision);
            }

            final String remaining = columnType.substring(end + 1);
            unsigned = remaining.toLowerCase().contains("unsigned");
        } else {
            name = columnType;
            unsigned = false;
        }
        return new FieldType(name, length, deciaml, unsigned);
    }

    private interface Handle {
        void process(final List<Map<String, Object>> kvs) throws Exception;
    }

    @AllArgsConstructor
    @Data
    private static class FieldType {
        private String type;
        private Integer length;
        private Integer decimal;
        private Boolean unsigned;
    }
}