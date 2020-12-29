<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
        <script src="${ctx}/js/jquery-3.5.1.min.js"></script>
        <style type="text/css">
            .key {
                color: #009688;
                font-family: "Times New Roman";
                font-weight: bold;
                font-style: italic
            }
        </style>
    </head>
    <body>

    <div class="layui-fluid">
        <table id="grid" lay-filter="grid"></table>
        <script type="text/html" id="colName">
            <span style="color: blue">{{ d.name }}</span>
        </script>
    </div>

    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table', 'carousel', 'echarts'], function () {
            const table = layui.table;
            tableErrorHandler();
            table.render({
                elem: '#grid',
                url: 'listSetting?topicName=${topicName}',
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
                    {field: 'name', title: '配置名称', templet: '#colName', width: 280},
                    {field: 'value', title: '配置项值', width: 350},
                    {field: 'description', title: '配置说明'}
                ]]
            });

        });
    </script>
    </body>
    </html>
</@compress>