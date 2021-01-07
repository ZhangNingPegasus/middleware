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
                    <div class="layui-inline">索引名称</div>
                    <div class="layui-inline" style="width:500px">
                        <input type="text" name="indexName" placeholder="请输入索引名称" autocomplete="off"
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

                <script type="text/html" id="grid-bar">
                    {{#  if(d.inRebuild){ }}
                    <span class="layui-badge layui-bg-orange">
                       <h3><i>正在重建中</i></h3>
                    </span>
                    {{#  } else { }}

                    {{#  if(d.disableRebuild){ }}
                    <@update>
                        <a class="layui-btn layui-btn-normal layui-btn-xs layui-btn-disabled"><i
                                    class="layui-icon layui-icon-edit"></i>&nbsp;开始重建</a>
                    </@update>
                    {{#  } else { }}
                    <@update>
                        <a class="layui-btn layui-btn-normal layui-btn-xs" lay-event="rebuild"><i
                                    class="layui-icon layui-icon-cols"></i>&nbsp;开始重建</a>
                    </@update>
                    {{#  } }}

                    {{#  } }}
                </script>

                <script type="text/html" id="grid-toolbar">
                    <div class="layui-btn-container">
                        <@select>
                            <button class="layui-btn layui-btn-sm layuiadmin-btn-admin" lay-event="refresh">
                                <i class="layui-icon layui-icon-refresh"></i>&nbsp;刷新列表
                            </button>
                        </@select>
                    </div>
                </script>

                <script type="text/html" id="colRowUpdateTime">
                    {{#  if(d.rowUpdateTime!=null){ }}
                    <div>{{layui.util.toDateString(d.rowUpdateTime, "yyyy-MM-dd HH:mm:ss")}}</div>
                    {{#  } }}
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

            <#if inRebuild==true>
            showRebuildDetail('${(topic.name)!''}');
            </#if>

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
                    {field: 'name', title: '索引名称', width: 300},
                    {field: 'numberOfShards', title: '主分片数', width: 100},
                    {field: 'numberOfReplicas', title: '副本分片数', width: 100},
                    {field: 'aliasOfYears', title: '索引别名年份数', width: 130},
                    {field: 'refreshInterval', title: '刷盘间隔', width: 100},
                    {field: 'description', title: '描述信息'},
                    {field: 'rowUpdateTime', title: '最近一次重建索引时间', templet: '#colRowUpdateTime', width: 170},
                    {fixed: 'right', fixed: 'right', title: '操作', toolbar: '#grid-bar', width: 110}
                ]]
            });

            table.on('toolbar(grid)', function (obj) {
                if (obj.event === 'refresh') {
                    $("#btnSearch").click();
                }
            });

            table.on('tool(grid)', function (obj) {
                const data = obj.data;
                if (obj.event === 'rebuild') {
                    layer.open({
                        type: 2,
                        title: '<i class="layui-icon layui-icon-cols" style="color: #1E9FFF;"></i>&nbsp;重建索引' + ' - ' + data.name,
                        content: 'torebuild?name=' + data.name,
                        area: ['1280px', '800px'],
                        btn: admin.BUTTONS,
                        closeBtn: 0,
                        resize: false,
                        yes: function (index, layero) {
                            layer.confirm("确定进行主题[" + data.name + "]的索引重建吗?", function (i) {
                                layer.close(i);
                                const iframeWindow = window['layui-layer-iframe' + index], submitID = 'btn_confirm',
                                    submit = layero.find('iframe').contents().find('#' + submitID),
                                    mapping = layero.find('iframe').contents().find('#btnMapping');
                                mapping.click();
                                iframeWindow.layui.form.on('submit(' + submitID + ')', function (d) {
                                    const field = d.field;
                                    field.datasources = JSON.stringify(convertParam(field));
                                    admin.post('rebuild', field, function () {
                                        table.reload('grid');
                                        layer.close(index);
                                        showRebuildDetail(field.name);
                                    }, function (result) {
                                        table.reload('grid');
                                        admin.error(admin.OPT_FAILURE, result.error);
                                    });
                                });
                                submit.trigger('click');
                            });
                        },
                        btn2: function (index, layero) {
                            layer.confirm("确定取消主题[" + data.name + "]的索引重建设置吗?", function (i) {
                                layer.close(i);
                                layer.close(index);
                            });
                            return false;
                        }
                    });
                }
            });

            function convertParam(field) {
                const result = [];
                const data = new Map();
                const indexs = new Set();

                $.each(field, function (key, val) {
                    const name = analyseName(key);
                    indexs.add(name.index);
                    data.set(key, val);
                });

                for (const index of indexs) {
                    const json = {
                        "host": "",
                        "port": "",
                        "uid": "",
                        "pwd": "",
                        "databaseName": "",
                        "tableNames": ""
                    };
                    json.host = data.get("host__" + index);
                    json.port = data.get("port__" + index);
                    json.uid = data.get("uid__" + index);
                    json.pwd = data.get("pwd__" + index);
                    json.databaseName = data.get("databaseName__" + index);
                    json.tableNames = data.get("tableNames__" + index);
                    if (json.host !== undefined) {
                        result.push(json);
                    }
                }
                return result;
            }

            function analyseName(key) {
                const all = key.split("__");
                return {
                    "name": all[0],
                    "index": all[1]
                }
            }

            function showRebuildDetail(name) {
                layer.open({
                    type: 2,
                    title: '<i class="layui-icon layui-icon-cols"></i>&nbsp;&nbsp;重建索引:' + name,
                    content: 'toRebuildDetail',
                    area: ['1280px', '800px'],
                    closeBtn: 0,
                    btn: ['关闭窗口', '停止重建'],
                    resize: false,
                    yes: function (index, layero) {
                        const iframeWindow = window['layui-layer-iframe' + index], submitID = 'btn_confirm',
                            submit = layero.find('iframe').contents().find('#' + submitID);
                        iframeWindow.layui.form.on('submit(' + submitID + ')', function (d) {
                            const field = d.field;
                            if (field.complete) {
                                admin.postQuiet("clearRebuild", {}, function () {
                                    table.reload('grid');
                                    layer.close(index);
                                });
                            } else {
                                layer.msg('正在索引重建中, 无法关闭本窗口', {time: 1000});
                                return false;
                            }
                        });
                        submit.trigger('click');
                    },
                    btn2: function () {
                        layer.confirm("确定停止主题[" + name + "]的索引重建吗?", function (index) {
                            layer.close(index);
                            admin.post("stopRebuild", {}, function () {
                            });
                        });
                        return false;
                    }
                });
            }
        });
    </script>
    </body>
    </html>
</@compress>