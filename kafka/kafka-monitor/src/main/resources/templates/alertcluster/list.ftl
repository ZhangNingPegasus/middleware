<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
    </head>
    <body>

    <div class="layui-fluid">

        <div class="layui-card">
            <div class="layui-card-body">
                <table id="grid" lay-filter="grid"></table>
                <script type="text/html" id="type">
                    {{#  if(d.type==1){ }}
                    ZOOKEEPER
                    {{#  } else { }}
                    KAFKA
                    {{#  } }}
                </script>
                <script type="text/html" id="grid-toolbar">
                    <div class="layui-btn-container">
                        <@insert>
                            <button class="layui-btn layui-btn-sm layuiadmin-btn-admin" lay-event="add">创建</button>
                        </@insert>
                    </div>
                </script>
                <script type="text/html" id="grid-bar">
                    <@update>
                        <a class="layui-btn layui-btn-normal layui-btn-xs" lay-event="edit"><i
                                    class="layui-icon layui-icon-edit"></i>编辑</a>
                    </@update>
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
            const admin = layui.admin, $ = layui.$, table = layui.table;
            tableErrorHandler();
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
                    {field: 'type', title: '集群类型', templet: '#type', width: 200},
                    {field: 'server', title: '主机地址'},
                    {field: 'email', title: '通知邮箱', width: 300},
                    {
                        field: 'rowCreateTime',
                        title: '创建时间',
                        templet: '<div>{{layui.util.toDateString(d.rowCreateTime, "yyyy-MM-dd HH:mm:ss")}}</div>',
                        width: 200
                    }
                    <@select>
                    , {fixed: 'right', title: '操作', align: "center", toolbar: '#grid-bar', width: 160}
                    </@select>
                ]]
            });

            table.on('toolbar(grid)', function (obj) {
                if (obj.event === 'add') {
                    layer.open({
                        type: 2,
                        title: '创建集群主机警告',
                        content: 'toadd',
                        area: ['880px', '350px'],
                        btn: admin.BUTTONS,
                        resize: false,
                        yes: function (index, layero) {
                            const iframeWindow = window['layui-layer-iframe' + index], submitID = 'btn_confirm',
                                submit = layero.find('iframe').contents().find('#' + submitID);
                            iframeWindow.layui.form.on('submit(' + submitID + ')', function (data) {
                                const field = data.field;
                                admin.post('add', field, function () {
                                    table.reload('grid');
                                    layer.close(index);
                                }, function (result) {
                                    admin.error(admin.OPT_FAILURE, result.error);
                                });
                            });
                            submit.trigger('click');
                        }
                    });
                }
            });

            table.on('tool(grid)', function (obj) {

                const data = obj.data;
                if (obj.event === 'del') {
                    layer.confirm(admin.DEL_QUESTION, function (index) {
                        admin.post("del", {'id': data.id}, function () {
                            if (table.cache.grid.length < 2) {
                                const skip = $(".layui-laypage-skip");
                                const curPage = skip.find("input").val();
                                let page = parseInt(curPage) - 1;
                                if (page < 1) {
                                    page = 1;
                                }
                                skip.find("input").val(page);
                                $(".layui-laypage-btn").click();
                            } else {
                                table.reload('grid');
                            }
                            layer.close(index);
                        });
                    });
                } else if (obj.event === 'edit') {
                    const id = data.id;
                    layer.open({
                        type: 2,
                        title: '编辑消费组警告',
                        content: 'toedit?id=' + id,
                        area: ['880px', '400px'],
                        btn: admin.BUTTONS,
                        resize: false,
                        yes: function (index, layero) {
                            const iframeWindow = window['layui-layer-iframe' + index], submitID = 'btn_confirm',
                                submit = layero.find('iframe').contents().find('#' + submitID);
                            iframeWindow.layui.form.on('submit(' + submitID + ')', function (data) {
                                const field = data.field;
                                field.id = id;
                                admin.post('edit', field, function () {
                                    table.reload('grid');
                                    layer.close(index);
                                }, function (result) {
                                    admin.error(admin.OPT_FAILURE, result.error);
                                });
                            });
                            submit.trigger('click');
                        }
                    });
                }
            });
        });
    </script>
    </body>
    </html>
</@compress>