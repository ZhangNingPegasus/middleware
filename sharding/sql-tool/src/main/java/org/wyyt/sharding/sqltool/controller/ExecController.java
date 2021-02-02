package org.wyyt.sharding.sqltool.controller;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wyyt.admin.ui.common.Utils;
import org.wyyt.sharding.algorithm.MathsTool;
import org.wyyt.sharding.auto.property.DataSourceProperty;
import org.wyyt.sharding.auto.property.DimensionProperty;
import org.wyyt.sharding.auto.property.TableProperty;
import org.wyyt.sharding.entity.FieldInfo;
import org.wyyt.sharding.entity.IndexInfo;
import org.wyyt.sharding.service.ShardingService;
import org.wyyt.sharding.sqltool.database.Db;
import org.wyyt.sharding.sqltool.entity.dto.SysSql;
import org.wyyt.sharding.sqltool.entity.vo.AdminVo;
import org.wyyt.sharding.sqltool.entity.vo.AlgorithmVo;
import org.wyyt.sharding.sqltool.entity.vo.DataTableVo;
import org.wyyt.sharding.sqltool.service.SqlService;
import org.wyyt.sharding.sqltool.service.TreeService;
import org.wyyt.tool.rpc.Result;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The controller of SQL statement executing
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping("exec")
public final class ExecController {
    private final Db db;
    private final TreeService treeService;
    private final ShardingService shardingService;
    private final SqlService sqlService;

    public ExecController(final Db db,
                          final TreeService treeService,
                          final ShardingService shardingService,
                          final SqlService sqlService) {
        this.db = db;
        this.treeService = treeService;
        this.shardingService = shardingService;
        this.sqlService = sqlService;
    }

    @RequestMapping("tolist")
    public final String toList(final Model model) {
        model.addAttribute("iconDimension", TreeService.ICON_DIMENSION);
        model.addAttribute("iconDataSource", TreeService.ICON_DATASOURCE);
        model.addAttribute("iconTable", TreeService.ICON_TABLE);
        model.addAttribute("tables", shardingService.listTableProperties());
        model.addAttribute("data", JSON.toJSONString(treeService.listShardingTableSources()));
        return "exec/list";
    }

    @RequestMapping("todetail")
    public final String toDetail(final Model model,
                                 @RequestParam(value = "dimension") final String dimension,
                                 @RequestParam(value = "datasource") final String datasource,
                                 @RequestParam(value = "table") final String table) throws Exception {
        model.addAttribute("dimension", dimension);
        model.addAttribute("datasource", datasource);
        model.addAttribute("table", table);

        final DimensionProperty dimensionProperty = this.shardingService.getDimensionByName(dimension);
        final TableProperty.DimensionInfo tableDimensionInfo = this.shardingService.getTableDimensionInfo(table, dimension);
        final int dbCount = dimensionProperty.getDataSourceProperties().size();
        final DataSourceProperty dataSourceProperty = this.shardingService.getDataSourcePropertyByName(datasource);
        Assert.notNull(dataSourceProperty, String.format("不存在名为%s的数据源", datasource));
        final int lastIndexOf = dataSourceProperty.getDatabaseName().lastIndexOf("_");
        final int index = Integer.parseInt(dataSourceProperty.getDatabaseName().substring(lastIndexOf + 1));
        final int countPerDb = tableDimensionInfo.getTableCountNum() / dbCount;
        final int start = index * countPerDb;
        final int end = start + countPerDb - 1;
        final List<String> tableList = new ArrayList<>(countPerDb);
        for (int i = start; i <= end; i++) {
            tableList.add(String.format("%s.%s_%s", dataSourceProperty.getDatabaseName(), table, i));
        }
        model.addAttribute("tableList", StringUtils.join(tableList.toArray(new String[]{}), ','));

        final List<FieldInfo> fieldInfoList = this.shardingService.listFields(dimension, datasource, table);
        model.addAttribute("fields", JSON.toJSONString(fieldInfoList));

        final List<IndexInfo> indexInfoList = this.shardingService.listIndex(dimension, datasource, table);
        model.addAttribute("indexs", JSON.toJSONString(indexInfoList));
        return "exec/detail";
    }

    @PostMapping("check")
    @ResponseBody
    public final Result<List<String>> check(@RequestParam(value = "table") final String table) throws Exception {
        final List<String> result = new ArrayList<>();

        final List<String> diffTableList = this.shardingService.diffFields(table);
        if (diffTableList.isEmpty()) {
            result.add("检查通过");
        } else {
            result.add(String.format("下列%s个数据表的结构与其他大多数的表结构不一致, 请检查修正\n%s", diffTableList.size(), StringUtils.join(diffTableList, ", ")));
        }

        final List<String> diffIndexList = this.shardingService.diffIndexs(table);
        if (diffIndexList.isEmpty()) {
            result.add("检查通过");
        } else {
            result.add(String.format("下列%s个数据表的索引与其他大多数的索引不一致, 请检查修正\n%s", diffIndexList.size(), StringUtils.join(diffIndexList, ", ")));
        }
        return Result.ok(result);
    }

    @PostMapping("exec")
    @ResponseBody
    public final Result<List<DataTableVo>> exec(final AdminVo adminVo,
                                                final HttpServletRequest request,
                                                final @RequestParam(name = "sql") String sql,
                                                final @RequestParam(name = "limit") Integer limit) {
        try {
            return Result.ok(this.sqlService.exec(Utils.getCliectIp(request), adminVo, sql, limit));
        } catch (final Exception exception) {
            return Result.ok(new ArrayList<>(Collections.singleton(this.sqlService.exception(exception))));
        }
    }

    @PostMapping("getNext")
    @ResponseBody
    public final Result<SysSql> getNext(final AdminVo adminVo,
                                        final @RequestParam(name = "id") Long id) throws Exception {
        final SysSql sysSql = this.db.getNextSql(adminVo.getId(), id);
        return Result.ok(sysSql);
    }

    @PostMapping("getPrevious")
    @ResponseBody
    public final Result<SysSql> getPrevious(final AdminVo adminVo,
                                            final @RequestParam(name = "id") Long id) throws Exception {
        final SysSql sysSql = this.db.getPreviousSql(adminVo.getId(), id);
        return Result.ok(sysSql);
    }

    @PostMapping("doalgorithm")
    @ResponseBody
    public final Result<AlgorithmVo> doalgorithm(@RequestParam(name = "value") String value,
                                                 @RequestParam(value = "dimension") String dimension,
                                                 @RequestParam(value = "datasource") String datasource,
                                                 @RequestParam(value = "table") String table) {
        value = value.trim();
        dimension = dimension.trim();
        table = table.trim();
        final AlgorithmVo result = new AlgorithmVo();
        final long hash = MathsTool.hash(value);

        final DimensionProperty dimensionProperty = this.shardingService.getDimensionByName(dimension);
        if (null == dimensionProperty) {
            return Result.error(String.format("不存在名为[%s]的维度", dimension));
        }
        final TableProperty.DimensionInfo dimensionInfo = this.shardingService.getTableDimensionInfo(table, dimensionProperty.getName());
        final List<String> datasourceNames = new ArrayList<>(dimensionProperty.getDataSourceProperties().keySet());
        datasourceNames.sort(String::compareTo);

        final int databaseNumCount = dimensionProperty.getDataSourceProperties().size();
        final int tableCount = dimensionInfo.getTableCountNum();
        result.setHash(Long.toString(hash));

        long remainder = MathsTool.doDatabaseSharding(value, databaseNumCount, tableCount);
        String formular = String.format("(%s) %% (%s / %s) %% %s = %s", result.getHash(), tableCount, databaseNumCount, databaseNumCount, remainder);
        result.setDatabase(String.format("%s    计算公式: %s", datasourceNames.get((int) remainder), formular));

        remainder = MathsTool.doTableSharding(value, tableCount);
        formular = String.format("%s %% %s = %s", result.getHash(), tableCount, remainder);
        result.setTable(String.format("%s    计算公式: %s", String.format(dimensionInfo.getTableNameFormat(), remainder), formular));

        return Result.ok(result);
    }
}