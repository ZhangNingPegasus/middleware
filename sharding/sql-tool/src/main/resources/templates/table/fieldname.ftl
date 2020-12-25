<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
    </head>
    <body>

    <div class="layui-form" lay-filter="layuiadmin-form-admin" id="layuiadmin-form-admin">

        <table class="layui-hide" id="grid" lay-filter="grid"></table>

        <div class="layui-form-item layui-hide">
            <input type="button" lay-submit lay-filter="btn_confirm" id="btn_confirm" value="确认">
        </div>
    </div>

    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'form', 'table'], function () {
            const $ = layui.$, table = layui.table;
            tableErrorHandler();
            const data = [];
            const names = '${names}'.split(',');
            const enames = '${enames}'.split(',');

            $.each(names, function (i, j) {
                if ($.trim(j) === '') {
                    return false;
                }
                if (enames.indexOf(j) > -1 || enames.indexOf('`' + j + '`') > -1) {
                    data.push({'fieldName': j, 'LAY_CHECKED': true});
                } else {
                    data.push({'fieldName': j});
                }
            });

            table.render({
                elem: '#grid',
                cellMinWidth: 80,
                page: false,
                limit: 9999999,
                limits: [9999999],
                even: false,
                loading: false,
                cols: [[
                    {type: 'checkbox', width: 50},
                    {type: 'numbers', title: '序号', width: 50},
                    {field: 'fieldName', title: '字段名称'}
                ]],
                data: data
            });

        });
    </script>
    </body>
    </html>
</@compress>