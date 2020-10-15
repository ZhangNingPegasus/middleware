<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
    </head>

    <style type="text/css">
        body {
            -webkit-user-select: none;
            -moz-user-select: none;
            -ms-user-select: none;
            user-select: none;
        }

        .layui-table tbody tr:hover, .layui-table tr, .layui-table-click, .layui-table-header, .layui-table-hover, .layui-table-mend, .layui-table-patch, .layui-table-tool, .layui-table[lay-even] tr:nth-child(even) {
            background-color: transparent;
        }
    </style>
    <body>

    <div class="layui-form" style="padding: 10px 30px 0 30px;">
        <div class="layui-tab layui-tab-brief" lay-filter="form">
            <div lay-filter="demo" class="layui-progress layui-progress-big" lay-showPercent="true">
                <div class="layui-progress-bar layui-bg-green" lay-percent="0%"></div>
            </div>

            <blockquote id="message" class="layui-elem-quote">
            </blockquote>

            <div id="gridDiv" class="layui-card-body">
                <table id="grid" lay-filter="grid"></table>
                <script type="text/html" id="colSource">
                    {{ d.source }}
                    {{# if(d.done){ }}
                    &nbsp;&nbsp;&nbsp;&nbsp;<span class="layui-badge layui-bg-blue"><small><i>已完成</i></small></span>
                    {{#  } else { }}
                    {{# if(d.startTime>0){ }}
                    &nbsp;&nbsp;&nbsp;&nbsp;<span class="layui-badge layui-bg-green"><small><i>进行中</i></small></span>
                    {{#  } else { }}
                    &nbsp;&nbsp;&nbsp;&nbsp;<span class="layui-badge layui-bg-gray"><small><i>等待中</i></small></span>
                    {{# } }}
                    {{# } }}
                </script>
            </div>

            <div class="layui-form-item layui-hide">
                <input type="button" lay-submit lay-filter="btn_confirm" id="btn_confirm" value="确认">
                <input id="complete" name="complete"/>
            </div>
        </div>
    </div>

    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'layer', 'element', 'form', 'table'], function () {
            const admin = layui.admin, $ = layui.$, element = layui.element, layer = layui.layer, table = layui.table;
            tableErrorHandler();
            table.render({
                elem: '#grid',
                cellMinWidth: 80,
                page: false,
                limit: 9999999,
                limits: [9999999],
                even: false,
                text: {
                    none: '暂无相关数据'
                },
                cols: [[
                    {field: 'name', title: '索引名'},
                    {field: 'source', templet: '#colSource', title: '数据源'},
                    {field: 'count', title: '总共同步数量'},
                    {field: 'tps', title: '每秒同步数量'},
                    {field: 'second', title: '耗时时长', width: 150}
                ]],
                done: function () {
                    layuiRowspan('name', 1);
                }
            });

            function refresh() {
                admin.postQuiet('rebuilddetail', {}, function (data) {
                    data = data.data;
                    $("#message").html(data.message);
                    element.progress('demo', data.progress + '%');
                    table.reload('grid', {
                        data: [{
                            'type': 'full',
                            'done': data.dbStatus.done,
                            'startTime': data.dbStatus.startTime,
                            'name': data.dbStatus.name,
                            'source': '全量数据',
                            'count': data.dbStatus.count,
                            'tps': data.dbStatus.tps,
                            'second': formatSeconds(data.dbStatus.duringSeconds)
                        }, {
                            'type': 'increment',
                            'done': data.kafkaStatus.done,
                            'startTime': data.kafkaStatus.startTime,
                            'name': data.kafkaStatus.name,
                            'source': '增量数据',
                            'count': data.kafkaStatus.count,
                            'tps': data.kafkaStatus.tps,
                            'second': formatSeconds(data.kafkaStatus.duringSeconds)
                        }]
                    });
                    if (data.error) {
                        $("#gridDiv").hide();
                    }
                    if (data.complete) {
                        $("#complete").val(data.complete);
                        clearInterval(interval);
                    }
                });
            }

            function formatSeconds(value) {
                let theTime = parseInt(value);
                let theTime1 = 0;
                let theTime2 = 0;
                let theTime3 = 0;
                if (theTime > 60) {
                    theTime1 = parseInt(theTime / 60);
                    theTime = parseInt(theTime % 60);
                    if (theTime1 > 60) {
                        theTime2 = parseInt(theTime1 / 60);
                        theTime1 = parseInt(theTime1 % 60);
                        if (theTime2 > 24) {
                            theTime3 = parseInt(theTime2 / 24);
                            theTime2 = parseInt(theTime2 % 24);
                        }
                    }
                }
                let result = '';
                if (theTime > 0) {
                    result = ("" + parseInt(theTime)).padStart(2, '0') + "秒";
                }
                if (theTime1 > 0) {
                    result = ("" + parseInt(theTime1)).padStart(2, '0') + "分" + result;
                }
                if (theTime2 > 0) {
                    result = ("" + parseInt(theTime2)).padStart(2, '0') + "小时" + result;
                }
                if (theTime3 > 0) {
                    result = ("" + parseInt(theTime3)).padStart(2, '0') + "天" + result;
                }
                return result;
            }

            refresh();
            const interval = setInterval(refresh, 1000);
        });
    </script>
    </body>
    </html>
</@compress>