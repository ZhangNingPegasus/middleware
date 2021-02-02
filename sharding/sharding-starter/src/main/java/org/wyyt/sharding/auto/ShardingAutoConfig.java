package org.wyyt.sharding.auto;

import cn.hutool.core.util.StrUtil;
import com.sijibao.nacos.spring.util.NacosNativeUtils;
import com.sijibao.nacos.spring.util.NacosRsaUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.api.config.sharding.KeyGeneratorConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.ShardingRuntimeContext;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.ObjectUtils;
import org.wyyt.sharding.algorithm.impl.DatabaseComplexShardingAlgorithm;
import org.wyyt.sharding.algorithm.impl.TableComplexShardingAlgorithm;
import org.wyyt.sharding.aop.TransactionAop;
import org.wyyt.sharding.auto.config.XmlConfig;
import org.wyyt.sharding.auto.property.DataSourceProperty;
import org.wyyt.sharding.auto.property.DimensionProperty;
import org.wyyt.sharding.auto.property.ShardingProperty;
import org.wyyt.sharding.auto.property.TableProperty;
import org.wyyt.sharding.cache.aop.LocalCacheAop;
import org.wyyt.sharding.constant.Name;
import org.wyyt.sharding.context.DbContext;
import org.wyyt.sharding.entity.DbInfo;
import org.wyyt.sharding.exception.ShardingException;
import org.wyyt.sharding.interceptor.MainMybatisInterceptor;
import org.wyyt.sharding.interceptor.plugin.impl.CheckSqlInterceptor;
import org.wyyt.sharding.service.RewriteService;
import org.wyyt.sharding.service.ShardingService;
import org.wyyt.tool.cache.CacheService;
import org.wyyt.tool.db.DataSourceTool;
import org.wyyt.tool.sql.SqlTool;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

/**
 * Auto-configuration of ShardingSphere property
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Configuration
@EnableConfigurationProperties({XmlConfig.class})
public class ShardingAutoConfig implements DisposableBean {
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public MainMybatisInterceptor mainMybatisInterceptor(final ApplicationContext applicationContext) {
        return new MainMybatisInterceptor(applicationContext);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public CheckSqlInterceptor checkSqlInterceptor(final ShardingService shardingService) {
        return new CheckSqlInterceptor(shardingService);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public ShardingService shardingService(final ShardingProperty shardingProperty) {
        return new ShardingService(shardingProperty, this.dataSourceMap);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public DbContext dbContext(final ShardingService shardingService) {
        return new DbContext(shardingService);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public ShardingProperty shardingProperty() throws Exception {
        final ShardingProperty shardingProperty = new ShardingProperty();
        shardingProperty.setDataSourceProperties(analyseDataSourceXML(shardingProperty));
        shardingProperty.setDimensionProperties(analyseDimensionXml(shardingProperty));
        shardingProperty.setTableProperties(analyseTableXml(shardingProperty));
        return shardingProperty;
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public DatabaseComplexShardingAlgorithm databaseComplexShardingAlgorithm() {
        return new DatabaseComplexShardingAlgorithm();
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public TableComplexShardingAlgorithm tableComplexShardingAlgorithm() {
        return new TableComplexShardingAlgorithm();
    }

    @Bean(destroyMethod = "destroy")
    @Primary
    @ConditionalOnMissingBean
    public CacheService cacheService() {
        return new CacheService(null, 128, 1024L);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public TransactionAop transactionAop() {
        return new TransactionAop();
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public LocalCacheAop ehCacheAop(final CacheService cacheService) {
        return new LocalCacheAop(cacheService);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = Name.SHARDING, name = Name.ENABLED, havingValue = Name.TRUE)
    public RewriteService rewriteService(final ShardingRuntimeContext shardingRuntimeContext) {
        return new RewriteService(shardingRuntimeContext);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = Name.SHARDING, name = Name.ENABLED, havingValue = Name.TRUE)
    public ShardingDataSource shardingDataSource(final ShardingService shardingService,
                                                 final DatabaseComplexShardingAlgorithm databaseComplexShardingAlgorithm,
                                                 final TableComplexShardingAlgorithm tableComplexShardingAlgorithm) throws SQLException {
        createDataSourceMap(shardingService);
        final ShardingRuleConfiguration shardingRuleConfiguration = this.createShardingRuleConfiguration();
        shardingRuleConfiguration.getTableRuleConfigs().addAll(this.createTableRuleConfiguration(shardingService, databaseComplexShardingAlgorithm, tableComplexShardingAlgorithm));

        for (final Map.Entry<String, DataSourceProperty> pair : shardingService.listDataSourcePropertyMap().entrySet()) {
            if (pair.getValue().getIsDefault()) {
                shardingRuleConfiguration.setDefaultDataSourceName(pair.getKey());
                break;
            }
        }

        final List<String> bindingTableList = shardingService.listBindingTables();
        for (final String bindingTable : bindingTableList) {
            shardingRuleConfiguration.getBindingTableGroups().add(bindingTable);
        }

        final List<String> broadcastTableList = shardingService.listBroadcastTables();
        for (final String broadcastTable : broadcastTableList) {
            shardingRuleConfiguration.getBroadcastTables().add(broadcastTable);
        }

        final Map<String, DataSource> dsMap = new HashMap<>();
        for (final Map.Entry<DbInfo, DataSource> pair : this.dataSourceMap.entrySet()) {
            dsMap.put(pair.getKey().getName(), pair.getValue());
        }
        return (ShardingDataSource) ShardingDataSourceFactory.createDataSource(dsMap, shardingRuleConfiguration, createProperties(shardingService));
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = Name.SHARDING, name = Name.ENABLED, havingValue = Name.TRUE)
    public ShardingRuntimeContext shardingRuntimeContext(final ShardingDataSource shardingDataSource) {
        return shardingDataSource.getRuntimeContext();
    }

    @Override
    public void destroy() {
        this.clearDataSources();
    }

    public ShardingAutoConfig(final XmlConfig xmlConfig) {
        this.xmlConfig = xmlConfig;
        this.dataSourceMap = new HashMap<>();
    }

    private final XmlConfig xmlConfig;
    private final Map<DbInfo, DataSource> dataSourceMap;

    private void createDataSourceMap(final ShardingService shardingService) {
        this.clearDataSources();
        final Map<String, DataSourceProperty> dataSourceProperties = shardingService.listDataSourcePropertyMap();
        dataSourceProperties.forEach((key, dataSourceProperty) -> this.dataSourceMap.put(
                new DbInfo(dataSourceProperty.getName(), dataSourceProperty.getDatabaseName()),
                DataSourceTool.createHikariDataSource(
                        key,
                        dataSourceProperty.getHost(),
                        dataSourceProperty.getPort().toString(),
                        dataSourceProperty.getDatabaseName(),
                        dataSourceProperty.getUsername(),
                        NacosRsaUtils.decrypt(dataSourceProperty.getPassword()),
                        dataSourceProperty.getMinIdle(),
                        dataSourceProperty.getMaxActive())));
    }

    private void clearDataSources() {
        this.dataSourceMap.forEach((dbInfo, dataSource) -> DataSourceTool.close(dataSource));
        this.dataSourceMap.clear();
    }

    private ShardingRuleConfiguration createShardingRuleConfiguration() {
        final ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        shardingRuleConfiguration.setDefaultKeyGeneratorConfig(keyGeneratorConfiguration(Name.FIELD_PRIMARY_KEY));
        return shardingRuleConfiguration;
    }

    private List<TableRuleConfiguration> createTableRuleConfiguration(final ShardingService shardingService,
                                                                      final DatabaseComplexShardingAlgorithm databaseComplexShardingAlgorithm,
                                                                      final TableComplexShardingAlgorithm tableComplexShardingAlgorithm) {
        final List<TableProperty> tableProperties = shardingService.listTableProperties();
        final List<TableRuleConfiguration> result = new ArrayList<>(tableProperties.size());

        tableProperties.forEach(tableProperty -> {
            final String shardingColumns = StringUtils.join(shardingService.listShardingColumns(tableProperty.getName()), ",");
            final String actualDataNodes = StringUtils.join(shardingService.listActualDataNodes(tableProperty.getName()), ",");

            final TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration(tableProperty.getName(), actualDataNodes);

            tableRuleConfiguration.setDatabaseShardingStrategyConfig(new ComplexShardingStrategyConfiguration(shardingColumns, databaseComplexShardingAlgorithm));
            tableRuleConfiguration.setTableShardingStrategyConfig(new ComplexShardingStrategyConfiguration(shardingColumns, tableComplexShardingAlgorithm));
            if (!ObjectUtils.isEmpty(tableProperty.getPkName())) {
                tableRuleConfiguration.setKeyGeneratorConfig(keyGeneratorConfiguration(tableProperty.getPkName()));
            }
            result.add(tableRuleConfiguration);
        });
        return result;
    }

    private Properties createProperties(final ShardingService shardingService) {
        final Properties properties = new Properties();
        properties.setProperty("sql.show", String.valueOf(shardingService.isShowSql()));
        return properties;
    }

    private KeyGeneratorConfiguration keyGeneratorConfiguration(String primaryKey) {
        final Properties properties = new Properties();
        properties.setProperty("worker.id", this.xmlConfig.getWorkId().toString());
        properties.setProperty("max.tolerate.time.difference.milliseconds", "1000"); //容忍的时间回拨毫秒数
        return new KeyGeneratorConfiguration("SNOWFLAKE", primaryKey, properties);
    }

    private Map<String, DataSourceProperty> analyseDataSourceXML(final ShardingProperty shardingProperty) throws Exception {
        NacosNativeUtils.loadAcmInfo(
                this.xmlConfig.getAcm().getDatasource().getDataId(),
                this.xmlConfig.getAcm().getDatasource().getGroup(),
                this.xmlConfig.getAcm().getAcmConfigPath(),
                this.xmlConfig.getAcm().getNacosLocalSnapshotPath(),
                this.xmlConfig.getAcm().getNacosLogPath());
        final String xml = NacosNativeUtils.getContent();

        final Map<String, DataSourceProperty> result = new HashMap<>();
        analyse(xml, root -> {
            shardingProperty.setShowSql(this.xmlConfig.getShowSql());
            for (final Iterator<Element> it = root.elementIterator(); it.hasNext(); ) {
                final Element element = it.next();
                final DataSourceProperty property = new DataSourceProperty();

                assertAttributeNotEmpty(element, Name.NAME, xml);
                assertElementNotEmpty(element, Name.HOST, xml);
                assertElementNotEmpty(element, Name.PORT, xml);
                assertElementNotEmpty(element, Name.DATA_BASE_NAME, xml);
                assertElementNotEmpty(element, Name.USER_NAME, xml);
                assertElementNotEmpty(element, Name.PASSWORD, xml);
                assertElementNotEmpty(element, Name.MIN_IDLE, xml);
                assertElementNotEmpty(element, Name.MAX_ACTIVE, xml);

                property.setName(element.attributeValue(Name.NAME).trim());
                property.setIndex(isAttributeEmpty(element, Name.INDEX) ? 0 : Integer.parseInt(element.attributeValue(Name.INDEX).trim()));

                property.setHost(element.elements(Name.HOST).get(0).getTextTrim());
                property.setPort(Integer.parseInt(element.elements(Name.PORT).get(0).getTextTrim()));
                property.setDatabaseName(element.elements(Name.DATA_BASE_NAME).get(0).getTextTrim());
                property.setUsername(element.elements(Name.USER_NAME).get(0).getTextTrim());
                property.setPassword(element.elements(Name.PASSWORD).get(0).getTextTrim());
                property.setIsDefault(!isAttributeEmpty(element, Name.IS_DEFAULT) && Boolean.parseBoolean(element.attributeValue(Name.IS_DEFAULT).trim()));
                property.setMinIdle(Integer.parseInt(element.elements(Name.MIN_IDLE).get(0).getTextTrim()));
                property.setMaxActive(Integer.parseInt(element.elements(Name.MAX_ACTIVE).get(0).getTextTrim()));
                result.put(property.getName(), property);
            }
        });
        return result;
    }

    private Map<String, DimensionProperty> analyseDimensionXml(final ShardingProperty shardingProperty) throws Exception {
        NacosNativeUtils.loadAcmInfo(
                this.xmlConfig.getAcm().getDimenstion().getDataId(),
                this.xmlConfig.getAcm().getDimenstion().getGroup(),
                this.xmlConfig.getAcm().getAcmConfigPath(),
                this.xmlConfig.getAcm().getNacosLocalSnapshotPath(),
                this.xmlConfig.getAcm().getNacosLogPath());
        final String xml = NacosNativeUtils.getContent();

        final List<DimensionProperty> dimensionPropertyList = new ArrayList<>();
        final Set<Integer> priorityList = new HashSet<>(64);

        analyse(xml, root -> {
            for (final Iterator<Element> itElement = root.elementIterator(); itElement.hasNext(); ) {
                final Element element = itElement.next();
                DimensionProperty property = new DimensionProperty();

                assertAttributeNotEmpty(element, Name.NAME, xml);
                assertAttributeNotEmpty(element, Name.PRIORITY, xml);
                assertAttributeNotEmpty(element, Name.DESCRIPTION, xml);

                property.setName(element.attributeValue(Name.NAME).trim());
                property.setPriority(Integer.parseInt(element.attributeValue(Name.PRIORITY).trim()));
                property.setDescription(element.attributeValue(Name.DESCRIPTION).trim());

                if (element.elements().size() < 1) {
                    throw new ShardingException(String.format("[%s]中的[datasource]信息是必填项", xml));
                }

                final Map<String, DataSourceProperty> dataSourceProperties = new HashMap<>();
                for (final Iterator<Element> itDataSource = element.elementIterator(); itDataSource.hasNext(); ) {
                    final Element datasource = itDataSource.next();
                    assertAttributeNotEmpty(datasource, Name.REF, xml);
                    final String dsName = datasource.attributeValue(Name.REF).trim();
                    final DataSourceProperty value = shardingProperty.getDataSourceProperties().get(dsName);
                    if (null == value) {
                        throw new ShardingException(String.format("没有找到名称为[%s]的数据源", dsName));
                    }
                    dataSourceProperties.put(dsName, value);
                }
                property.setDataSourceProperties(dataSourceProperties);

                if (priorityList.contains(property.getPriority())) {
                    throw new ShardingException(String.format("在维度配置中,已经存在[priority=%s]的配置项", property.getPriority()));
                }
                priorityList.add(property.getPriority());
                dimensionPropertyList.add(property);
            }
        });

        if (!priorityList.contains(0)) {
            throw new ShardingException("在维度配置中,缺少主维度[priority=0]的配置");
        }

        dimensionPropertyList.sort(Comparator.comparingInt(DimensionProperty::getPriority));

        final Map<String, DimensionProperty> result = new HashMap<>();
        for (final DimensionProperty property : dimensionPropertyList) {
            result.put(property.getName(), property);
        }
        return result;
    }

    private List<TableProperty> analyseTableXml(final ShardingProperty shardingProperty) throws Exception {
        NacosNativeUtils.loadAcmInfo(
                this.xmlConfig.getAcm().getTable().getDataId(),
                this.xmlConfig.getAcm().getTable().getGroup(),
                this.xmlConfig.getAcm().getAcmConfigPath(),
                this.xmlConfig.getAcm().getNacosLocalSnapshotPath(),
                this.xmlConfig.getAcm().getNacosLogPath());
        final String xml = NacosNativeUtils.getContent();

        final List<TableProperty> result = new ArrayList<>();

        analyse(xml, root -> {
            for (final Iterator<Element> itElement = root.elementIterator(); itElement.hasNext(); ) {
                final Element element = itElement.next();
                final TableProperty property = new TableProperty();

                assertAttributeNotEmpty(element, Name.NAME, xml);

                property.setName(SqlTool.removeMySqlQualifier(element.attributeValue(Name.NAME).trim()));
                property.setPkName(SqlTool.removeMySqlQualifier(isAttributeEmpty(element, Name.PK_NAME) ? Name.FIELD_PRIMARY_KEY : element.attributeValue(Name.PK_NAME).trim()));
                property.setRowCreateTime(SqlTool.removeMySqlQualifier(isAttributeEmpty(element, Name.ROW_CREATE_TIME) ? Name.FIELD_ROW_CREATE_TIME : element.attributeValue(Name.ROW_CREATE_TIME).trim()));
                property.setRowUpdateTime(SqlTool.removeMySqlQualifier(isAttributeEmpty(element, Name.ROW_UPDATE_TIME) ? Name.FIELD_ROW_UPDATE_TIME : element.attributeValue(Name.ROW_UPDATE_TIME).trim()));
                property.setBindingGroupName(isAttributeEmpty(element, Name.BINDING_GROUP_NAME) ? "" : element.attributeValue(Name.BINDING_GROUP_NAME).trim());
                property.setBroadcast(!isAttributeEmpty(element, Name.BROADCAST) && Boolean.parseBoolean(element.attributeValue(Name.BROADCAST).trim()));

                final Map<String, TableProperty.DimensionInfo> dimensionInfoList = new HashMap<>();
                for (final Iterator<Element> itDimension = element.elementIterator(); itDimension.hasNext(); ) {
                    final TableProperty.DimensionInfo dimensionInfo = new TableProperty.DimensionInfo();
                    final Element dimension = itDimension.next();

                    assertAttributeNotEmpty(dimension, Name.REF, xml);

                    final String dimensionName = dimension.attributeValue(Name.REF).trim();
                    final DimensionProperty value = shardingProperty.getDimensionProperties().get(dimensionName);
                    if (null == value) {
                        throw new ShardingException(String.format("没有找到名称为[%s]的维度信息", dimensionName));
                    }

                    dimensionInfo.setDimensionProperty(value);

                    dimensionInfo.setTableCountNum(isAttributeEmpty(dimension, Name.TABLE_COUNT_NUM) ? 1 : Integer.parseInt(dimension.attributeValue(Name.TABLE_COUNT_NUM).trim()));
                    dimensionInfo.setShardingColumn(isAttributeEmpty(dimension, Name.TABLE_COUNT_NUM) ? Name.FIELD_PRIMARY_KEY : dimension.attributeValue(Name.SHARDING_COLUMN).trim());
                    dimensionInfo.setTableNameFormat(isAttributeEmpty(dimension, Name.TABLE_NAME_FORMAT) ? property.getName() : dimension.attributeValue(Name.TABLE_NAME_FORMAT).trim());

                    dimensionInfoList.put(dimensionName, dimensionInfo);
                }
                property.setDimensionInfos(dimensionInfoList);
                result.add(property);
            }
        });
        return result;
    }

    private void assertAttributeNotEmpty(final Element element,
                                         final String attribute,
                                         final String xml) {
        if (this.isAttributeEmpty(element, attribute)) {
            throw new ShardingException(String.format("[%s]配置项中的[%s]信息是必填项", xml, attribute));
        }
    }

    private void assertElementNotEmpty(final Element element,
                                       final String elementName,
                                       final String xml) {
        if (this.isElementEmpty(element, elementName)) {
            throw new ShardingException(String.format("[%s]配置项中的[%s]信息是必填项", xml, elementName));
        }
    }

    private boolean isAttributeEmpty(final Element element,
                                     final String attribute) {
        return null == element.attribute(attribute) || StrUtil.isBlank(element.attribute(attribute).getValue().trim());
    }

    private boolean isElementEmpty(final Element element,
                                   final String elementName) {
        return element.elements(elementName).isEmpty() || StrUtil.isBlank(element.elements(elementName).get(0).getTextTrim());
    }

    private void analyse(final String xml,
                         final XmlReader xmlReader) throws DocumentException {
        final Document document = DocumentHelper.parseText(xml);
        xmlReader.analyse(document.getRootElement());
    }

    private interface XmlReader {
        void analyse(final Element root);
    }
}