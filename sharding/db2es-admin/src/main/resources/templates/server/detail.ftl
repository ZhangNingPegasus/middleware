<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
        <link rel="stylesheet" href="${ctx}/css/codemirror.css" media="all">
        <link rel="stylesheet" href="${ctx}/css/idea.css" media="all">
        <link rel="stylesheet" href="${ctx}/css/merge.css" media="all">

        <script src="${ctx}/js/jquery-3.5.1.min.js"></script>
        <style type="text/css">
            .key {
                color: #009688;
                font-family: "Times New Roman";
                font-weight: bold;
                font-style: italic
            }

            .CodeMirror-merge, .CodeMirror-merge .CodeMirror {
                height: 590px;
            }

            .CodeMirror-merge-2pane .CodeMirror-merge-gap {
                width: 0%;
            }

            .CodeMirror-merge-2pane .CodeMirror-merge-pane {
                width: 50%;
            }

            .CodeMirror-merge-r-chunk-end {
                border-bottom: 1px solid #9E9E00;
            }

            .CodeMirror-merge-r-chunk-start {
                border-top: 1px solid #9E9E00;
            }

            .CodeMirror-merge-r-chunk {
                background: #9E9E32;
            }
        </style>
    </head>
    <body>

    <div class="layui-fluid">
        <div class="layui-tab layui-tab-card">
            <ul class="layui-tab-title">
                <li class="layui-this"><i class="layui-icon layui-icon-console"></i>&nbsp;&nbsp;索引状态</li>
                <li><i class="layui-icon layui-icon-share"></i>&nbsp;&nbsp;数据比较</li>
            </ul>
            <div class="layui-tab-content">
                <div class="layui-tab-item layui-show">
                    <table id="grid" lay-filter="grid"></table>

                    <script type="text/html" id="grid-toolbar">
                        <div class="layui-btn-container">
                            <button class="layui-btn layui-btn-sm" lay-event="refresh">
                                <i class="layui-icon layui-icon-refresh"></i>
                                刷新
                            </button>
                        </div>
                    </script>

                    <script type="text/html" id="colPrirep">
                        {{#  if(d.prirep=='p'){ }}
                        <span class="layui-badge layui-bg-blue">
                             主分片
                        </span>
                        {{#  } else if(d.prirep=='r') { }}
                        <span class="layui-badge layui-bg-green">
                             副本分片
                        </span>
                        {{#  } else { }}
                        <span class="layui-badge">
                            {{d.prirep}}
                        </span>
                        {{#  } }}
                    </script>
                </div>

                <div class="layui-tab-item">
                    <div class="layui-form-item">
                        <div class="layui-inline">主键(id):</div>
                        <div class="layui-inline" style="width: 350px;">
                            <input type="text" id="pkValue" name="pkValue" class="layui-input"
                                   placeholder="请填写数据表${indexName}的主键值"
                                   autocomplete="off">
                        </div>

                        <div class="layui-inline">拆分键(${shardingColumn!''}):</div>
                        <div class="layui-inline" style="width: 350px;">
                            <input type="text" id="shardingValue" name="shardingValue" class="layui-input"
                                   placeholder="请填写数据表${indexName}的拆分键值" autocomplete="off">
                        </div>

                        <div class="layui-inline">
                            <button id="btnDiff" type="button" class="layui-btn">
                                <i class="layui-icon layui-icon-link"></i>&nbsp;比&nbsp;较&nbsp;
                            </button>

                            <button id="btnSync" type="button" class="layui-btn layui-btn-normal">
                                <i class="layui-icon layui-icon-senior"></i>&nbsp;同&nbsp;步&nbsp;
                            </button>
                        </div>
                    </div>

                    <div id="view">

                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="first" style="display: none"></div>
    <div id="second" style="display: none"></div>
    <script src="${ctx}/js/codemirror.js"></script>
    <script src="${ctx}/js/autorefresh.js"></script>
    <script src="${ctx}/js/active-line.js"></script>
    <script src="${ctx}/js/matchbrackets.js"></script>
    <script src="${ctx}/js/javascript.js"></script>
    <script src="${ctx}/js/diff_match_patch.js"></script>
    <script src="${ctx}/js/merge.js"></script>
    <script type="text/javascript">
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table', 'carousel', 'echarts'], function () {
            const admin = layui.admin, table = layui.table, $ = layui.$;
            let mv = null;
            tableErrorHandler();
            table.render({
                elem: '#grid',
                toolbar: '#grid-toolbar',
                url: 'listShards?indexName=${indexName}',
                method: 'post',
                cellMinWidth: 80,
                page: false,
                limit: 99999999,
                limits: [99999999],
                even: true,
                text: {
                    none: '暂无相关数据'
                },
                cols: [[
                    {type: 'numbers', title: '序号', width: 50},
                    {field: 'index', title: '物理索引名称'},
                    {field: 'node', title: '节点名称', width: 320},
                    {field: 'ip', title: '节点地址', width: 150},
                    {field: 'docs', title: '分片记录数量', width: 150},
                    {field: 'store', title: '分片占用体积', width: 150},
                    {field: 'shard', title: '分片号', width: 120},
                    {field: 'prirep', title: '分片类型', templet: '#colPrirep', width: 140},
                    {field: 'state', title: '分片状态', width: 150}
                ]]
            });

            table.on('toolbar(grid)', function (obj) {
                switch (obj.event) {
                    case 'refresh':
                        table.reload('grid');
                        break;
                }
            });

            $("#btnDiff").click(function () {
                const pkValue = $("#pkValue").val().trim();
                const shardingValue = $("#shardingValue").val().trim();
                if (pkValue === '') {
                    admin.error("操作错误", "主键值是必填项");
                    return;
                }

                admin.post("diffData", {
                    "indexName": "${indexName}",
                    "pkValue": pkValue,
                    "shardingValue": shardingValue
                }, function (data) {
                    data = data.data;
                    const dbData = data.dbData;
                    const esData = data.esData;

                    $("#first").html(dbData);
                    $("#second").html(esData);

                    $("#diff").remove();
                    $("#view").append('<div id="diff"></div>');

                    mv = CodeMirror.MergeView(document.getElementById("diff"), {
                        value: $("#first").html(),
                        orig: $("#second").html(),
                        origLeft: null,
                        lineNumbers: true,
                        mode: "application/json",
                        highlightDifferences: true,
                        connect: "align",
                        theme: "idea",
                        readOnly: "no",
                        collapseIdentical: false,
                        revertButtons: false,
                        allowEditingOriginals: false
                    });
                    $(".CodeMirror-merge-gap").remove();

                    $(".CodeMirror-merge-pane.CodeMirror-merge-editor").prepend("<span style='width: 100%' class='layui-badge layui-bg-green'><h3 style='margin-left: 30px;'>数据库</h3></span>");
                    $(".CodeMirror-merge-pane.CodeMirror-merge-right.CodeMirror-merge-pane-rightmost").prepend("<span style='width: 100%' class='layui-badge layui-bg-blue'><h3 style='margin-left: 30px;'>Elastic-Search</h3></span>");
                });
            });

            $("#btnSync").click(function () {
                const pkValue = $("#pkValue").val().trim();
                const shardingValue = $("#shardingValue").val().trim();
                const dbData = $("#first").html();
                const esData = $("#second").html();
                if (pkValue === '') {
                    admin.error("操作错误", "主键值是必填项");
                    return;
                }

                if (mv === null) {
                    admin.error("操作错误", "同步前请先进行[比较]操作");
                    return;
                }

                if (dbData.trim() === esData.trim()) {
                    admin.error("操作错误", "在数据库和Elastic-Search中, 主键值是[" + pkValue + "]的数据目前完全一致, 无需同步");
                    return;
                }

                layer.confirm("确定要把主键值是[" + pkValue + "]的数据从数据库同步到Elastic-Search吗?", function (index) {
                    admin.post("sync", {
                        "indexName": "${indexName}",
                        "pkValue": pkValue,
                        "shardingValue": shardingValue
                    }, function () {
                        layer.close(index);
                        $("#btnDiff").click();
                    });
                });

            });

        });
    </script>
    </body>
    </html>
</@compress>