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

                <script type="text/html" id="grid-toolbar">
                    <div class="layui-btn-container">
                        <@update>
                            <button class="layui-btn layui-btn-sm layui-btn-normal layuiadmin-btn-admin"
                                    lay-event="publish">
                                <i class="layui-icon layui-icon-release"></i>发布灰度策略
                            </button>
                        </@update>
                        <@insert>
                            <button class="layui-btn layui-btn-sm layuiadmin-btn-admin" lay-event="add">
                                <i class="layui-icon layui-icon-add-1"></i>新增调用分支
                            </button>
                        </@insert>
                        <@select>
                            <button class="layui-btn layui-btn-sm layuiadmin-btn-admin" lay-event="inspect">
                                <i class="layui-icon layui-icon-rss"></i>测试灰度调用链
                            </button>

                            <button class="layui-btn layui-btn-sm layuiadmin-btn-admin" lay-event="refresh">
                                <i class="layui-icon layui-icon-refresh-3"></i>刷新服务列表
                            </button>
                        </@select>
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
            const admin = layui.admin, $ = layui.$, form = layui.form, table = layui.table;
            tableErrorHandler();

            function reload(data) {
                table.reload('grid', {url: null, 'data': data});
            }

            form.on('submit(search)', function (data) {
                const field = data.field;
                table.reload('grid', {where: field, page: 1});
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
                    {field: 'id', title: '分支标识', width: 150},
                    {field: 'description', title: '描述信息', width: 200},
                    {
                        field: 'weight',
                        title: '流量比例',
                        align: "center",
                        templet: '<div>{{d.weight}}%</div>',
                        width: 150
                    },
                    {field: 'value', title: '调用分支(service_name : service_version)'}
                    <@select>
                    , {fixed: 'right', title: '操作', toolbar: '#grid-bar', width: 150}
                    </@select>
                ]]
            });

            table.on('toolbar(grid)', function (obj) {
                if (obj.event === 'add') {
                    layer.open({
                        type: 2,
                        title: '<i class="layui-icon layui-icon-add-1"></i>&nbsp;新增调用分支',
                        content: 'toadd',
                        area: ['1200px', '750px'],
                        btn: admin.BUTTONS,
                        resize: false,
                        yes: function (index, layero) {
                            const iframeWindow = window['layui-layer-iframe' + index], submitID = 'btn_confirm',
                                submit = layero.find('iframe').contents().find('#' + submitID);
                            let error = false;
                            iframeWindow.layui.form.on('submit(' + submitID + ')', function (d) {
                                const field = d.field;
                                const gridData = iframeWindow.layui.table.cache['grid'];
                                if (gridData.length < 1) {
                                    error = true;
                                    return;
                                }

                                const branch = {};
                                $.each(gridData, function (index, item) {
                                    if (item.version) {
                                        eval('branch["' + item.name + '"]=' + '"' + item.version + '"');
                                    }
                                });

                                if (JSON.stringify(branch) === "{}") {
                                    error = true;
                                    admin.error("系统提示", "请选择服务对应的版本号");
                                    return;
                                }

                                const data = table.cache['grid'];
                                $.each(data, function (index, item) {
                                    if (item.id === field.id) {
                                        error = true;
                                        admin.error("系统提示", "分支标识[" + item.id + "]重复");
                                        return;
                                    }
                                    if (item.value === JSON.stringify(branch)) {
                                        error = true;
                                        admin.error("系统提示", "调用分支[" + item.value + "]重复");
                                        return;
                                    }
                                });

                                if (!error) {
                                    data.push({
                                        "id": field.id,
                                        "description": field.description,
                                        "weight": field.weight,
                                        "value": JSON.stringify(branch)
                                    });
                                    reload(data);
                                    layer.close(index);
                                }
                            });
                            submit.trigger('click');
                        }
                    });
                } else if (obj.event === 'refresh') {
                    table.reload('grid', {url: 'list'});
                } else if (obj.event === 'inspect') {
                    const d = table.cache['grid'];

                    if (d.length < 1) {
                        admin.error("系统提示", "请先添加调用分支");
                        return;
                    }

                    layer.open({
                        type: 2,
                        title: '<i class="layui-icon layui-icon-rss" style="color: #1E9FFF;"></i>&nbsp;测试灰度调用链路',
                        content: 'toglobalinspect',
                        area: ['800px', '600px'],
                        btn: ['确定'],
                        closeBtn: 0,
                        resize: false
                    });
                }
            });

            table.on('tool(grid)', function (obj) {
                const data = obj.data;
                if (obj.event === 'del') {
                    layer.confirm(admin.DEL_QUESTION, function (index) {
                        const d = table.cache['grid'];
                        $.each(d, function (index, item) {
                            if (item) {
                                if (item.id === data.id) {
                                    d.remove(item);
                                    return;
                                }
                            }
                        });
                        reload(d);
                        layer.close(index);
                    });
                } else if (obj.event === 'edit') {
                    admin.post("seteditdata", {"data": JSON.stringify(data)}, function () {
                        layer.open({
                            type: 2,
                            title: '<i class="layui-icon layui-icon-edit" style="color: #1E9FFF;"></i>&nbsp;编辑调用分支',
                            content: 'toedit',
                            area: ['1200px', '750px'],
                            btn: admin.BUTTONS,
                            resize: false,
                            yes: function (index, layero) {
                                const iframeWindow = window['layui-layer-iframe' + index], submitID = 'btn_confirm',
                                    submit = layero.find('iframe').contents().find('#' + submitID);
                                let error = false, updateIndex = -1;
                                iframeWindow.layui.form.on('submit(' + submitID + ')', function (d) {
                                    const field = d.field;
                                    const gridData = iframeWindow.layui.table.cache['grid'];
                                    if (gridData.length < 1) {
                                        error = true;
                                        return;
                                    }

                                    const branch = {};
                                    $.each(gridData, function (index, item) {
                                        if (item.version) {
                                            eval('branch["' + item.name + '"]=' + '"' + item.version + '"');
                                        }
                                    });

                                    if (JSON.stringify(branch) === "{}") {
                                        error = true;
                                        admin.error("系统提示", "请选择服务对应的版本号");
                                        return;
                                    }
                                    const data = table.cache['grid'];
                                    $.each(data, function (index, item) {
                                        if (item.id !== field.id) {
                                            if (item.value === JSON.stringify(branch)) {
                                                error = true;
                                                admin.error("系统提示", "调用分支[" + item.value + "]重复");
                                                return;
                                            }
                                        } else {
                                            updateIndex = index;
                                        }
                                    });

                                    if (!error) {
                                        data[updateIndex] = {
                                            "id": field.id,
                                            "description": field.description,
                                            "weight": field.weight,
                                            "value": JSON.stringify(branch)
                                        };
                                        reload(data);
                                        layer.close(index);
                                    }

                                });
                                submit.trigger('click');
                            }
                        });
                    });
                }
            });
        });
    </script>
    </body>
    </html>
</@compress>