package org.wyyt.sharding.sqltool.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.wyyt.sharding.auto.property.DataSourceProperty;
import org.wyyt.sharding.auto.property.TableProperty;
import org.wyyt.sharding.entity.FieldInfo;
import org.wyyt.sharding.entity.IndexInfo;
import org.wyyt.sharding.service.ShardingService;
import org.wyyt.sharding.sqltool.entity.vo.FieldVo;
import org.wyyt.sharding.sqltool.entity.vo.IndexVo;
import org.wyyt.sharding.sqltool.entity.vo.TableInfoVo;
import org.wyyt.sharding.sqltool.service.TableService;
import org.wyyt.sharding.sqltool.service.TreeService;
import org.wyyt.tool.rpc.Result;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.wyyt.sharding.sqltool.controller.TableController.PREFIX;

/**
 * The controller for table's creation.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public final class TableController {
    public static final String PREFIX = "table";
    private final TableService tableService;
    private final TreeService treeService;
    private final ShardingService shardingService;

    public TableController(final TableService tableService,
                           final TreeService treeService,
                           final ShardingService shardingService) {
        this.tableService = tableService;
        this.treeService = treeService;
        this.shardingService = shardingService;
    }

    @GetMapping("tolist")
    public final String toList(final Model model) {
        final List<String> typeList = Arrays.asList("bigint",
                "binary",
                "bit",
                "blob",
                "char",
                "date",
                "datetime",
                "decimal",
                "double",
                "enum",
                "float",
                "geometry",
                "geometrycollection",
                "int",
                "integer",
                "json",
                "linestring",
                "longblob",
                "longtext",
                "mediumblob",
                "mediumint",
                "mediumtext",
                "multilinestring",
                "multipoint",
                "multipolygon",
                "numeric",
                "point",
                "polygon",
                "real",
                "set",
                "smallint",
                "text",
                "time",
                "timestamp",
                "tinyblob",
                "tinyint",
                "tinytext",
                "varbinary",
                "varchar",
                "year");
        model.addAttribute("types", typeList);
        model.addAttribute("tables", treeService.listTables());
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("toIndex")
    public final String toIndex(final Model model,
                                final HttpSession session) {
        final String names = session.getAttribute("names").toString();
        String data = session.getAttribute("data").toString();
        if ("null".equalsIgnoreCase(data)) {
            data = "";
        }
        model.addAttribute("names", names);
        model.addAttribute("data", ObjectUtils.isEmpty(data) ? "[]" : data);
        return String.format("%s/%s", PREFIX, "index");
    }

    @GetMapping("toFieldName")
    public final String toFieldName(final Model model,
                                    @RequestParam(value = "names") final String names,
                                    @RequestParam(value = "enames") final String enames) {
        model.addAttribute("names", names);
        model.addAttribute("enames", enames);
        return String.format("%s/%s", PREFIX, "fieldname");
    }

    @GetMapping("toresult")
    public final String toResult(final Model model,
                                  final HttpSession session) {
        final String tableName = session.getAttribute("tableName").toString();
        final Integer dbCount = Integer.valueOf(session.getAttribute("dbCount").toString());
        final Integer tableCount = Integer.valueOf(session.getAttribute("tableCount").toString());
        final String data = session.getAttribute("data").toString();
        final String index = session.getAttribute("index").toString();

        final FieldVo[] fieldVos = JSON.parseObject(data, FieldVo[].class);
        final IndexVo[] indexVos = JSON.parseObject(index, IndexVo[].class);
        final Map<String, String> result = this.tableService.createTable(tableName,
                dbCount,
                tableCount,
                (fieldVos == null) ? null : Arrays.asList(fieldVos),
                (indexVos == null) ? null : Arrays.asList(indexVos)
        );
        model.addAttribute("result", result);
        return String.format("%s/%s", PREFIX, "result");
    }

    @RequestMapping(value = "download")
    public final void downloads(final HttpServletResponse response,
                                final HttpSession session) throws Exception {
        final String tableName = session.getAttribute("tableName").toString();
        final Integer dbCount = Integer.valueOf(session.getAttribute("dbCount").toString());
        final Integer tableCount = Integer.valueOf(session.getAttribute("tableCount").toString());
        final String data = session.getAttribute("data").toString();
        final String index = session.getAttribute("index").toString();

        response.reset();
        response.setCharacterEncoding("UTF-8");
        response.setContentType("multipart/form-data");
        response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("建表SQL.sql", StandardCharsets.UTF_8.name()));

        final FieldVo[] fieldVos = JSON.parseObject(data, FieldVo[].class);
        final IndexVo[] indexVos = JSON.parseObject(index, IndexVo[].class);
        final Map<String, String> result = this.tableService.createTable(tableName,
                dbCount,
                tableCount,
                (fieldVos == null) ? null : Arrays.asList(fieldVos),
                (indexVos == null) ? null : Arrays.asList(indexVos)
        );

        final StringBuilder content = new StringBuilder();
        for (final Map.Entry<String, String> pair : result.entrySet()) {
            content.append(String.format("------------------------------------------ %s -----------------------------------------\r\n", pair.getKey()));
            content.append(pair.getValue().replaceAll("&#13;&#10;", "\r\n"));
            content.append("\r\n\n");
        }

        final byte[] bytes = content.toString().getBytes(StandardCharsets.UTF_8);
        final OutputStream out = response.getOutputStream();
        out.write(bytes);
        out.flush();
        out.close();
    }

    @PostMapping("session")
    @ResponseBody
    public final Result<?> session(final HttpSession session,
                                   @RequestParam(value = "tableName") final String tableName,
                                   @RequestParam(value = "dbCount") final Integer dbCount,
                                   @RequestParam(value = "tableCount") final Integer tableCount,
                                   @RequestParam(value = "data") final String data,
                                   @RequestParam(value = "index") final String index) {
        session.setAttribute("tableName", tableName);
        session.setAttribute("dbCount", dbCount);
        session.setAttribute("tableCount", tableCount);
        session.setAttribute("data", data);
        session.setAttribute("index", index);
        return Result.ok();
    }

    @PostMapping("session1")
    @ResponseBody
    public final Result<?> session1(final HttpSession session,
                                    @RequestParam(value = "names") final String names,
                                    @RequestParam(value = "data") final String data) {
        session.setAttribute("names", names);
        session.setAttribute("data", data);
        return Result.ok();
    }

    @PostMapping("getTableDetail")
    @ResponseBody
    public final Result<TableInfoVo> getTableDetail(@RequestParam(value = "dimension") final String dimension,
                                                    @RequestParam(value = "datasource") final String datasource,
                                                    @RequestParam(value = "table") final String table) throws Exception {
        final List<DataSourceProperty> dataSourcePropertyList = this.shardingService.listDataSourceProperties(dimension);
        final TableProperty.DimensionInfo tableDimensionInfo = this.shardingService.getTableDimensionInfo(table, dimension);
        final int databaseCount = dataSourcePropertyList.size();
        final int tableCount = tableDimensionInfo.getTableCountNum();

        final List<IndexInfo> indexInfoList = this.shardingService.listIndex(dimension, datasource, table);
        final List<FieldInfo> fieldInfoList = this.shardingService.listFields(dimension, datasource, table);
        return Result.ok(new TableInfoVo(databaseCount, tableCount / databaseCount, indexInfoList, fieldInfoList));
    }
}