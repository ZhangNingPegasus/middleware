<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <#include "../common/layui.ftl">
    <link rel="stylesheet" href="${ctx}/css/codemirror.css" media="all">
    <link rel="stylesheet" href="${ctx}/css/idea.css" media="all">
    <link rel="stylesheet" href="${ctx}/css/blackboard.css" media="all">
    <link rel="stylesheet" href="${ctx}/css/show-hint.css" media="all">
</head>
<body>
<div class="layui-row">

    <div class="layui-col-md2" style="border: #e6e6e6 solid 1px">
        <div id="divTree" class="layui-card">
            <div class="layui-card-header">
                &nbsp;&nbsp;&nbsp;&nbsp;${name}
            </div>
            <div class="layui-card-body">
                <div id="tree"></div>
            </div>
        </div>
    </div>

    <div class="layui-col-md10" style="border: #e6e6e6 solid 1px">
        <div id="divResult" class="layui-card">
            <div class="layui-card-header">
                <button id="left" class="layui-btn layui-btn-primary layui-btn-xs">关闭左侧表结构栏</button>
                <button id="right" class="pull-right layui-btn layui-btn-primary layui-btn-xs">
                    打开右侧实际SQL栏
                </button>&nbsp;&nbsp;&nbsp;&nbsp;
                alt + /&nbsp;:&nbsp;智能提醒&nbsp;&nbsp;&nbsp;&nbsp;
                <button id="exec" class="layui-btn layui-btn-primary layui-btn-xs"
                        style="width: 120px;background-color:#009688;color:white;font-weight: bold">&nbsp;&nbsp;▷&nbsp;&nbsp;执&nbsp;&nbsp;行&nbsp;&nbsp;
                </button>&nbsp;

                <button id="previous" class="layui-btn layui-btn-primary layui-btn-xs">↑&nbsp;&nbsp;上一条</button>&nbsp;
                <button id="next" class="layui-btn layui-btn-primary layui-btn-xs">↓&nbsp;&nbsp;下一条</button>&nbsp;&nbsp;&nbsp;&nbsp;

                <label style="cursor: pointer;margin-top: -2px">
                    <input id="limit" type="checkbox" tag="100" checked="checked"/>limit 100
                </label>
            </div>
            <div class="layui-card-body">
                <div style="border: #e6e6e6 solid 1px" class="layui-row">
                    <div id="divTxtSql" class="layui-col-md12" style="border: #e6e6e6 solid 1px">
                        <textarea id="txtSQL" class="layui-input" autocomplete="off" style="resize: none"></textarea>
                    </div>
                    <div id="divTxtFactSql" class="layui-col-md6" style="border: #e6e6e6 solid 1px;display: none">
                        <textarea id="txtFactSql" class="layui-input" autocomplete="off"
                                  style="resize: none"></textarea>
                    </div>
                </div>

                <br/>
                <div class="layui-tab layui-tab-card" lay-filter="result" style="height: 532px;margin-top:-20px">
                    <ul class="layui-tab-title">
                    </ul>
                    <div class="layui-tab-content">
                    </div>
                </div>
            </div>
        </div>
    </div>

</div>

<script src="${ctx}/js/codemirror.js"></script>
<script src="${ctx}/js/sql.js"></script>
<script src="${ctx}/js/show-hint.js"></script>
<script src="${ctx}/js/sql-hint.js"></script>
<script src="${ctx}/js/autorefresh.js"></script>
<script src="${ctx}/js/active-line.js"></script>
<script src="${ctx}/js/matchbrackets.js"></script>
<script type="text/javascript">
    layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table', 'tree'], function () {
        const admin = layui.admin, $ = layui.$, tree = layui.tree, element = layui.element, table = layui.table;
        tableErrorHandler();
        CodeMirror.resolveMode("text/x-sql").keywords["left join"] = true;
        CodeMirror.resolveMode("text/x-sql").keywords["left"] = true;
        CodeMirror.resolveMode("text/x-sql").keywords["right join"] = true;
        CodeMirror.resolveMode("text/x-sql").keywords["right"] = true;
        CodeMirror.resolveMode("text/x-sql").keywords["inner join"] = true;
        CodeMirror.resolveMode("text/x-sql").keywords["inner"] = true;
        CodeMirror.resolveMode("text/x-sql").keywords["when"] = true;
        CodeMirror.resolveMode("text/x-sql").keywords["FROM_DAYS(N)"] = true;
        CodeMirror.resolveMode("text/x-sql").keywords["UPGRADE"] = true;

        const tables = {};
        <#list tables as table>
        eval('tables.' + '${table.name}' + '=[]');
        </#list>

        const editor = CodeMirror.fromTextArea(document.getElementById("txtSQL"), {
            value: "",
            lineNumbers: true,
            mode: "text/x-sql",
            matchBrackets: true,
            spellcheck: true,
            theme: "idea",
            styleActiveLine: true,
            autoRefresh: true,
            extraKeys: {
                "Alt-/": "autocomplete"
            },
            hint: CodeMirror.hint.sql,
            hintOptions: {
                tables: tables
            }
        });
        const txtFactSql = CodeMirror.fromTextArea(document.getElementById("txtFactSql"), {
            readOnly: "no",
            lineNumbers: true,
            mode: "sql",
            matchBrackets: true,
            spellcheck: true,
            theme: "blackboard"
        });
        editor.setSize('auto', '268px');
        txtFactSql.setSize('auto', '268px');

        tree.render({
            elem: '#tree',
            data: ${data},
            showLine: false,
            showCheckbox: false,
            accordion: false,
            isJump: false,
            onlyIconControl: false,
            text: {
                none: '无数据',
                defaultNodeName: '未命名'
            },
            click: function (obj) {
                if (obj.data.children) {
                    if ($(obj.elem).find(".layui-icon.layui-icon-triangle-d").length > 0) {
                        $(obj.elem).find(".layui-icon.layui-icon-triangle-d").eq(0).attr("class", "layui-icon layui-icon-triangle-r");
                    } else {
                        $(obj.elem).find(".layui-icon.layui-icon-triangle-r").eq(0).attr("class", "layui-icon layui-icon-triangle-d");
                    }
                } else {
                    layer.open({
                        type: 2,
                        title: '<i class="layui-icon layui-icon-table" style="color:green"></i>&nbsp;&nbsp;逻辑表 [<b>' + obj.data.field + '</b>]',
                        shadeClose: true,
                        shade: 0.8,
                        area: ['90%', '90%'],
                        content: 'todetail?dimension=' + obj.data.dimension + '&datasource=' + obj.data.datasource + '&table=' + obj.data.table
                    });
                }
            }
        });

        $(".layui-tree-iconClick").click(function () {
            if ($(this).find(".layui-icon.layui-icon-triangle-d").length > 0) {
                $(this).find(".layui-icon.layui-icon-triangle-d").eq(0).attr("class", "layui-icon layui-icon-triangle-r");
            } else {
                $(this).find(".layui-icon.layui-icon-triangle-r").eq(0).attr("class", "layui-icon layui-icon-triangle-d");
            }
        });


        $("i[class='layui-icon layui-icon-file']").attr("class", "layui-icon layui-icon-table").attr("style", "color:blue");

        $(".layui-tree-set.layui-tree-spread .layui-tree-iconArrow").not(".layui-hide").attr("class", "layui-icon layui-icon-triangle-d");
        $(".layui-tree-set .layui-tree-iconArrow").not(".layui-hide").attr("class", "layui-icon layui-icon-triangle-r");

        const height = $(window).height() - 5;
        $("#divTree,#divResult").attr("style", "height: " + height + "px;overflow-x: auto;overflow-y: auto");

        $("#left").click(function () {
            if ($(this).html() === "关闭左侧表结构栏") {
                $("#divTree").parent().hide();
                $("#divResult").parent().attr("class", "layui-col-md12");
                $(this).html("打开左侧表结构栏");
            } else {
                $("#divTree").parent().show();
                $("#divResult").parent().attr("class", "layui-col-md10");
                $(this).html("关闭左侧表结构栏");
            }
        });

        $("#right").click(function () {
            if ($.trim($(this).html()) === "关闭右侧实际SQL栏") {
                $("#divTxtSql").attr("class", "layui-col-md12");
                $("#divTxtFactSql").hide();
                $(this).html("打开左侧实际SQL栏");
                setTimeout(() => {
                    editor.refresh()
                }, 1);
            } else {
                $("#divTxtFactSql").show();
                $("#divTxtSql").attr("class", "layui-col-md6");
                $(this).html("关闭右侧实际SQL栏");
                setTimeout(() => {
                    txtFactSql.refresh()
                }, 1);
            }
        });

        $("#previous").click(function () {
            admin.post("getPrevious", {"id": id}, function (res) {
                if (res.data) {
                    id = res.data.id;
                    editor.setValue(res.data.logicSql);
                }
            });
        });

        let id = 0;
        $("#next").click(function () {
            admin.post("getNext", {"id": id}, function (res) {
                if (res.data) {
                    id = res.data.id;
                    editor.setValue(res.data.logicSql);
                }
            });
        });

        $("#exec").click(function () {
            let limit = -1;
            if ($("#limit").is(':checked')) {
                limit = $("#limit").attr("tag");
            }

            let sql = $.trim(editor.getSelection());
            if (sql === '') {
                sql = $.trim(editor.getValue());
            }

            if (sql !== '') {
                admin.post("exec", {"sql": sql, 'limit': limit}, function (res) {
                    console.log(res);
                    $('div[lay-filter="result"]').find(".layui-tab-title > li").each(function (i, o) {
                        element.tabDelete('result', $(o).attr("lay-id"));
                    });
                    txtFactSql.setValue('');
                    for (let i = 0; i < res.data.length; i++) {
                        showSqlResultGrid(limit, res.data[i], parseInt(i) + 1);
                    }
                    if (res.data.length > 0) {
                        element.tabChange('result', 'tabGrid1');
                    }
                    element.render();
                });
            }
        });

        function processString(str) {
            return str.replace(/\"/g, '\\"');
        }

        function showSqlResultGrid(limit, data, index) {
            element.tabAdd('result', {
                title: '结果 ' + index,
                content: '<table id="grid' + index + '" lay-filter="grid' + index + '"></table>',
                id: 'tabGrid' + index
            });

            const columnList = data.columnNameList;
            const valueList = data.valueList;
            const cols = [];
            const vals = [];
            for (let i = 0; i < columnList.length; i++) {
                cols.push({field: columnList[i], title: columnList[i], sort: true});
            }
            for (let i = 0; i < valueList.length; i++) {
                const line = {};
                const vLine = valueList[i];
                for (let j = 0; j < vLine.length; j++) {
                    const c = columnList[j];
                    if (data.goodSql) {
                        eval("line['" + c + "']=\"" + processString(vLine[j]) + "\"");
                    } else {
                        eval("line['" + c + "']=\"<span style='color:darkred'>" + processString(vLine[j]) + "</span>\"");
                    }
                }
                vals.push(line);
            }

            if (data.goodSql) {
                let content = "";
                for (let i = 0; i < data.factSql.length; i++) {
                    const factSql = data.factSql[i];
                    content = content + '-- 数据源: ' + factSql.datasourceName + "  :  \r\n" + factSql.sql;
                    if (i < data.factSql.length - 1) {
                        content = content + "\r\n";
                    }
                }
                txtFactSql.setValue(txtFactSql.getValue() + content + '\r\n');
            } else {
                txtFactSql.setValue("Error");
            }

            table.render({
                elem: '#grid' + index,
                toolbar: '#grid-toolbar',
                cellMinWidth: 180,
                height: '470',
                even: true,
                page: true,
                limit: limit,
                limits: [limit],
                text: {none: '暂无相关数据'},
                cols: [cols],
                data: vals,
                done: function () {
                    $('div[lay-id="grid' + index + '"]').find('div[class="layui-box layui-laypage layui-laypage-default"]').append('<span class="layui-laypage-count">查询时间: ' + (data.execTime / 1000.0).toFixed(3) + '秒</span>');
                }
            });
        }
    });
</script>
</body>
</html>