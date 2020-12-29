<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
    </head>
    <body>

    <div class="layui-fluid">
        <div class="layui-card">
            <div class="layui-form layui-card-header layuiadmin-card-header-auto">
                <div class="layui-form-item">
                    <div class="layui-inline">名称</div>
                    <div class="layui-inline" style="width:500px">
                        <input type="text" name="name" placeholder="请输入主题名称" autocomplete="off"
                               class="layui-input">
                    </div>
                    <div class="layui-inline">
                        <button id="btnSearch" class="layui-btn layuiadmin-btn-admin" lay-submit lay-filter="search">
                            <i class="layui-icon layui-icon-search layuiadmin-button-btn"></i>
                        </button>
                    </div>
                </div>
            </div>

            <div class="layui-card-body">
                <table id="grid" lay-filter="grid"></table>

                <script type="text/html" id="grid-toolbar">
                    <div class="layui-btn-container">
                        <@insert>
                            <button class="layui-btn layui-btn-sm layuiadmin-btn-admin" lay-event="add">
                                <i class="layui-icon layui-icon-add-1"></i>新增索引
                            </button>
                        </@insert>

                        <@select>
                            <button class="layui-btn layui-btn-sm layuiadmin-btn-admin" lay-event="refresh">
                                <i class="layui-icon layui-icon-refresh"></i>&nbsp;刷新列表
                            </button>
                        </@select>
                    </div>
                </script>

                <script type="text/html" id="grid-bar">
                    <@select>
                        <a class="layui-btn layui-btn-xs" lay-event="detail"><i
                                    class="layui-icon layui-icon-read"></i>详情</a>
                    </@select>
                    <@delete>
                        <a class="layui-btn layui-btn-normal layui-btn-xs" lay-event="delete"><i
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
                table.reload('grid', {where: field});
            });
            table.render({
                elem: '#grid',
                url: 'list',
                method: 'post',
                toolbar: '#grid-toolbar',
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
                    {field: 'name', title: '名称', width: 300},
                    {field: 'numberOfShards', title: '主分片数', width: 100},
                    {field: 'numberOfReplicas', title: '副本分片数', width: 100},
                    {field: 'aliasOfYears', title: '索引别名年份数', width: 130},
                    {field: 'refreshInterval', title: '刷盘间隔', width: 100},
                    {field: 'description', title: '描述信息'},
                    {
                        field: 'rowCreateTime',
                        title: '创建时间',
                        templet: '<div>{{layui.util.toDateString(d.rowCreateTime, "yyyy-MM-dd HH:mm:ss")}}</div>',
                        width: 170
                    },
                    {
                        field: 'rowUpdateTime',
                        title: '更新时间',
                        templet: '<div>{{layui.util.toDateString(d.rowCreateTime, "yyyy-MM-dd HH:mm:ss")}}</div>',
                        width: 170
                    }
                    <@select>
                    , {fixed: 'right', title: '操作', toolbar: '#grid-bar', width: 150}
                    </@select>
                ]]
            });

            table.on('toolbar(grid)', function (obj) {
                if (obj.event === 'add') {
                    layer.open({
                        type: 2,
                        title: '<i class="layui-icon layui-icon-add-1" style="color: #1E9FFF;"></i>&nbsp;新增ES索引',
                        content: 'toadd',
                        area: ['1280px', '800px'],
                        btn: admin.BUTTONS,
                        resize: false,
                        yes: function (index, layero) {
                            const iframeWindow = window['layui-layer-iframe' + index], submitID = 'btn_confirm',
                                submit = layero.find('iframe').contents().find('#' + submitID),
                                mapping = layero.find('iframe').contents().find('#btnMapping');
                            mapping.click();

                            iframeWindow.layui.form.on('submit(' + submitID + ')', function (data) {
                                const field = data.field;
                                if (field.name === '') {
                                    admin.error("系统错误", "请选择主题名称");
                                    return false;
                                } else if (field.numberOfShards === '') {
                                    admin.error("系统错误", "请填写主分片数");
                                    return false;
                                } else if (field.numberOfReplicas === '') {
                                    admin.error("系统错误", "请填写副本分片数");
                                    return false;
                                } else if (field.refreshInterval === '') {
                                    admin.error("系统错误", "请填写索引别名年份数");
                                    return false;
                                } else if (field.aliasOfYears === '') {
                                    admin.error("系统错误", "请填写刷盘间隔");
                                    return false;
                                } else if (field.mapping === '') {
                                    admin.error("系统错误", "请填写Mapping信息");
                                    return false;
                                } else if (field.numberOfShards < 1) {
                                    admin.error("系统错误", "主分片数至少是1个");
                                    return false;
                                } else if (field.aliasOfYears < 1) {
                                    admin.error("系统错误", "索引别名年份数至少是1年");
                                    return false;
                                }

                                admin.post('add', field, function () {
                                    table.reload('grid');
                                    layer.close(index);
                                }, function (result) {
                                    table.reload('grid');
                                    admin.error(admin.OPT_FAILURE, result.error);
                                    layer.close(index);
                                });
                            });
                            submit.trigger('click');
                        }
                    });
                } else if (obj.event === 'refresh') {
                    $("#btnSearch").click();
                }
            });

            table.on('tool(grid)', function (obj) {
                const data = obj.data;
                if (obj.event === 'detail') {
                    layer.open({
                        type: 2,
                        title: '<i class="layui-icon layui-icon-read" style="color: #1E9FFF;"></i>&nbsp;查看详情',
                        content: 'todetail?id=' + data.id,
                        area: ['1280px', '790px'],
                        btn: ['关闭'],
                        resize: false,
                        yes: function (index) {
                            layer.close(index);
                        }
                    });
                } else if (obj.event === 'delete') {
                    layer.confirm('确定要删除索引[' + data.name + ']的配置信息吗?', function (index) {
                        admin.post("delete", data, function () {
                            table.reload('grid');
                            layer.close(index);
                        });
                    });
                }
            });
        });
    </script>
    </body>
    </html>
</@compress>