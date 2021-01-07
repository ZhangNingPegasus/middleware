<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
    </head>
    <body>

    <div class="layui-fluid">

        <div class="layui-form layui-card-header layuiadmin-card-header-auto">
            <div class="layui-form-item">
                <div class="layui-inline">服务实例ID</div>
                <div class="layui-inline" style="width:500px">
                    <input type="text" name="instanceId" placeholder="请输入服务实例ID" autocomplete="off"
                           class="layui-input">
                </div>
                <div class="layui-inline">
                    <button id="search" class="layui-btn layuiadmin-btn-admin" lay-submit lay-filter="search">
                        <i class="layui-icon layui-icon-search layuiadmin-button-btn"></i>
                    </button>
                </div>
            </div>
        </div>

        <div class="layui-card">
            <div class="layui-card-body">
                <table id="grid" lay-filter="grid"></table>

                <script type="text/html" id="colAlive">
                    {{# if(d.alive){ }}
                    &nbsp;&nbsp;&nbsp;&nbsp;<span class="layui-badge layui-bg-blue">正在运行</span>
                    {{#  } else { }}
                    &nbsp;&nbsp;&nbsp;&nbsp;<span class="layui-badge layui-bg-red">停止运行</span>
                    {{# } }}
                </script>

                <script type="text/html" id="grid-toolbar">
                    <div class="layui-btn-container">
                        <@select>
                            <button class="layui-btn layui-btn-sm layui-btn-primary layuiadmin-btn-admin"
                                    lay-event="refresh">
                                <i class="layui-icon layui-icon-refresh-3"></i>&nbsp;&nbsp;刷新服务列表
                            </button>
                        </@select>
                    </div>
                </script>

                <script type="text/html" id="grid-bar">
                    <@delete>
                        <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="del"><i
                                    class="layui-icon layui-icon-delete"></i>删除</a>
                    </@delete>
                </script>
            </div>
        </div>
    </div>

    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table'], function () {
            const admin = layui.admin, $ = layui.$, form = layui.form, table = layui.table;
            tableErrorHandler();
            form.on('submit(search)', function (data) {
                const field = data.field;
                table.reload('grid', {page: {curr: 1}, where: field});
            });
            table.render({
                elem: '#grid',
                url: 'list',
                toolbar: '#grid-toolbar',
                method: 'post',
                cellMinWidth: 80,
                page: true,
                limit: 15,
                limits: [15],
                even: true,
                text: {
                    none: '暂无相关数据'
                },
                cols: [[
                    {type: 'numbers', title: '序号', width: 50},
                    {field: 'id', title: '服务实例ID'},
                    {field: 'host', title: '主机地址', width: 200},
                    {field: 'port', title: '主机端口', width: 200},
                    {field: 'version', title: '服务版本', width: 200},
                    {field: 'group', title: '服务组名', width: 200},
                    {field: 'alive', title: '是否运行', align: "center", templet: '#colAlive', width: 200}
                    <@select>
                    , {fixed: 'right', title: '操作', align: "center", align: "center", toolbar: '#grid-bar', width: 150}
                    </@select>
                ]]
            });

            table.on('toolbar(grid)', function (obj) {
                if (obj.event === 'refresh') {
                    table.reload('grid');
                }
            });

            table.on('tool(grid)', function (obj) {
                const data = obj.data;
                if (obj.event === 'del') {
                    layer.confirm(admin.DEL_QUESTION, function (index) {
                        admin.post("remove", {"id": data.id}, function () {
                            admin.success("系统提示", "删除成功", function () {
                                table.reload('grid');
                                layer.close(index);
                            });
                        });
                    });
                }
            });
        });
    </script>
    </body>
    </html>
</@compress>