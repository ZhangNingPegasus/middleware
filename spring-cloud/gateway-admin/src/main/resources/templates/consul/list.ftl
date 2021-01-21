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
                        <@delete>
                            <button class="layui-btn layui-btn-sm layui-btn-danger" lay-event="del">
                                <i class="layui-icon layui-icon-delete"></i>&nbsp;&nbsp;删除服务实例
                            </button>
                        </@delete>
                    </div>
                </script>
            </div>
        </div>
    </div>

    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table'], function () {
            const admin = layui.admin, form = layui.form, table = layui.table;
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
                    {type: 'checkbox'},
                    {type: 'numbers', title: '序号', width: 50},
                    {field: 'id', title: '服务实例ID'},
                    {field: 'host', title: '主机地址', width: 200},
                    {field: 'port', title: '主机端口', width: 200},
                    {field: 'version', title: '服务版本', width: 200},
                    {field: 'group', title: '服务组名', width: 200},
                    {field: 'alive', title: '是否运行', align: "center", templet: '#colAlive', width: 200}
                ]]
            });

            table.on('toolbar(grid)', function (obj) {
                if (obj.event === 'refresh') {
                    table.reload('grid');
                } else if (obj.event === 'del') {
                    const checkedId = admin.getCheckedData(table, obj, "id");
                    if (checkedId.length > 0) {
                        layer.confirm(admin.DEL_QUESTION, function (index) {
                            admin.post("del", {'ids': checkedId.join(",")}, function () {
                                admin.closeDelete(table, obj, index);
                            });
                        });
                    } else {
                        admin.error(admin.SYSTEM_PROMPT, admin.DEL_ERROR);
                    }
                }
            });
        });
    </script>
    </body>
    </html>
</@compress>