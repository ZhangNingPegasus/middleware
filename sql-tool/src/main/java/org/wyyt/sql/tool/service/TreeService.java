package org.wyyt.sql.tool.service;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wyyt.sharding.auto.property.DataSourceProperty;
import org.wyyt.sharding.auto.property.DimensionProperty;
import org.wyyt.sharding.auto.property.TableProperty;
import org.wyyt.sharding.service.ShardingService;
import org.wyyt.sql.tool.entity.vo.TreeVo;

import java.util.*;

/**
 * the service which providing the data of tree list control
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Service
public class TreeService {
    public static final String ICON_DIMENSION = "layui-icon-template-1";
    public static final String ICON_DATASOURCE = "layui-icon-templeate-1";
    public static final String ICON_TABLE = "layui-icon-table";
    private final ShardingService shardingService;

    public TreeService(final ShardingService shardingService) {
        this.shardingService = shardingService;
    }

    public final List<TreeVo> listTables() {
        final List<TreeVo> result = new ArrayList<>();
        populateLeafNodes(listShardingTableSources(), result);

        final Map<String, TreeVo> tableMap = new HashMap<>();
        for (final TreeVo treeVo : result) {
            tableMap.put(treeVo.getId(), treeVo);
        }

        final List<TreeVo> buffer = new ArrayList<>(tableMap.values());
        buffer.sort(Comparator.comparing(TreeVo::getId));
        return buffer;
    }

    public final List<TreeVo> listShardingTableSources() {
        final List<TreeVo> result = new ArrayList<>();
        final List<DimensionProperty> dimensionPropertyList = shardingService.listDimensionProperties();

        //加载维度信息
        for (final DimensionProperty dimensionProperty : dimensionPropertyList) {
            String id = dimensionProperty.getName();
            String title = String.format("<i class=\"layui-icon %s\" style=\"color:green\"></i>&nbsp;%s&nbsp;(%s)", ICON_DIMENSION, dimensionProperty.getDescription(), dimensionProperty.getName());
            String field = dimensionProperty.getName();
            TreeVo dimension = new TreeVo();
            dimension.setId(id);
            dimension.setTitle(title);
            dimension.setField(field);
            dimension.setSpread(false);
            fillDataSource(dimension);
            result.add(dimension);
        }
        return result;
    }

    private List<TreeVo> populateLeafNodes(final List<TreeVo> source,
                                           final List<TreeVo> result) {
        for (final TreeVo treeVo : source) {
            if (null != treeVo.getChildren() && !treeVo.getChildren().isEmpty()) {
                populateLeafNodes(treeVo.getChildren(), result);
            } else {
                result.add(treeVo);
            }
        }
        return result;
    }

    private void fillDataSource(final TreeVo treeVo) {
        final List<DataSourceProperty> dataSourceProperties = shardingService.listDataSourceProperties(treeVo.getField());
        if (null == dataSourceProperties || dataSourceProperties.isEmpty()) {
            return;
        }
        treeVo.setSpread(true);
        treeVo.setChildren(new ArrayList<>(dataSourceProperties.size()));
        for (final DataSourceProperty dataSourceProperty : dataSourceProperties) {
            final String id = dataSourceProperty.getName();
            final String title = String.format("<i class=\"layui-icon %s\" style=\"color:green\"></i>&nbsp;<span title=\"%s\">%s</span>",
                    ICON_DATASOURCE,
                    dataSourceProperty.getHost(),
                    dataSourceProperty.getName());
            final String field = dataSourceProperty.getName();
            final TreeVo datasource = new TreeVo();
            datasource.setId(id);
            datasource.setTitle(title);
            datasource.setField(field);
            datasource.setSpread(false);
            fillTable(treeVo.getField(), datasource);
            treeVo.getChildren().add(datasource);
        }
    }

    private void fillTable(final String dimension,
                           final TreeVo treeVo) {
        final List<TableProperty> tablePropertyList = this.shardingService.listTableProperties(dimension, treeVo.getField());
        if (null == tablePropertyList || tablePropertyList.isEmpty()) {
            return;
        }
        treeVo.setSpread(true);
        treeVo.setChildren(new ArrayList<>(tablePropertyList.size()));
        for (final TableProperty tableProperty : tablePropertyList) {
            final String id = tableProperty.getName();
            String title;
            if (ObjectUtils.isEmpty(ICON_TABLE)) {
                title = tableProperty.getName();
            } else {
                title = String.format("<i class=\"layui-icon %s\" style=\"color:green\"></i>&nbsp;%s", ICON_TABLE, tableProperty.getName());
            }
            final String field = tableProperty.getName();
            final TreeVo table = new TreeVo();
            table.setId(id);
            table.setTitle(title);
            table.setField(field);
            table.setSpread(false);
            table.setDimension(dimension);
            table.setDatasource(treeVo.getField());
            table.setTable(field);
            treeVo.getChildren().add(table);
        }
    }
}