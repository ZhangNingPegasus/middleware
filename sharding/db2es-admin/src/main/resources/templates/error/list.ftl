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
                    <div class="layui-inline">主键值</div>
                    <div class="layui-inline" style="width:180px">
                        <input type="text" name="primaryKeyValue" placeholder="请输入主键值" autocomplete="off"
                               class="layui-input">
                    </div>

                    <div class="layui-inline">时间区间</div>
                    <div class="layui-inline" style="width:300px">
                        <input type="text" id="timeRange" name="timeRange" class="layui-input"
                               placeholder="请选择时间范围" autocomplete="off">
                    </div>

                    <div class="layui-inline">是否解决</div>
                    <div class="layui-inline" style="width:100px">
                        <select id="isResolved" name="isResolved" lay-filter="topicName" lay-search>
                            <option value="" selected="selected">所有</option>
                            <option value="0">未解决</option>
                            <option value="1">已解决</option>
                        </select>
                    </div>

                    <div class="layui-inline">消息主题</div>
                    <div class="layui-inline" style="width:160px">
                        <input type="text" name="topicName" placeholder="请输入消息主题" autocomplete="off"
                               class="layui-input">
                    </div>

                    <div class="layui-inline">消息分区</div>
                    <div class="layui-inline" style="width:140px">
                        <input type="number" name="partition" placeholder="请输入消息分区" autocomplete="off"
                               class="layui-input">
                    </div>

                    <div class="layui-inline">消息偏移量</div>
                    <div class="layui-inline" style="width:140px">
                        <input type="number" name="offset" placeholder="请输入消息偏移量" autocomplete="off"
                               class="layui-input">
                    </div>

                    <div class="layui-inline">
                        <button class="layui-btn layuiadmin-btn-admin" lay-submit lay-filter="search">
                            <i class="layui-icon layui-icon-search layuiadmin-button-btn"></i>
                        </button>
                    </div>
                </div>
            </div>

            <div class="layui-card-body">
                <table id="grid" lay-filter="grid"></table>

                <script type="text/html" id="colIsResolved">
                    {{#  if(d.isResolved == 0){ }}
                    <span class="layui-badge layui-bg-orange">
                        未解决
                    </span>
                    {{#  } else { }}
                    <span class="layui-badge layui-bg-green">
                        已解决
                    </span>
                    {{#  } }}
                </script>

                <script type="text/html" id="grid-bar">
                    <@update>
                        <a class="layui-btn layui-btn-xs" lay-event="showDetail"><i
                                    class="layui-icon layui-icon-form"></i>错误详情</a>
                        {{#  if(d.isResolved == 0){ }}
                        <a class="layui-btn layui-btn-xs layui-btn-warm" lay-event="compareData"><i
                                    class="layui-icon layui-icon-link"></i>数据比对</a>
                        {{#  } else { }}
                        <a class="layui-btn layui-btn-xs" lay-event="compareData"><i
                                    class="layui-icon layui-icon-link"></i>数据比对</a>
                        {{#  } }}
                    </@update>
                </script>
            </div>
        </div>
    </div>

    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'laydate', 'table'], function () {
            const admin = layui.admin, $ = layui.$, laydate = layui.laydate, form = layui.form, table = layui.table;
            tableErrorHandler();
            form.on('submit(search)', function (data) {
                const field = data.field;
                table.reload('grid', {where: field});
            });

            laydate.render({
                elem: '#timeRange',
                type: 'datetime',
                range: true,
                min: -90,
                max: 1,
                btns: ['confirm']
            });

            table.render({
                elem: '#grid',
                url: 'list',
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
                    {field: 'primaryKeyValue', title: '主键值', width: 180},
                    {field: 'tableName', title: '数据表名', width: 200},
                    {field: 'errorMessage', title: '错误消息'},
                    {field: 'topicName', title: '消息主题', width: 180},
                    {field: 'partition', title: '消息分区', width: 90},
                    {field: 'offset', title: '消息偏移量', width: 100},
                    {
                        field: 'rowCreateTime',
                        title: '创建时间',
                        templet: '<div>{{layui.util.toDateString(d.rowCreateTime, "yyyy-MM-dd HH:mm:ss")}}</div>',
                        width: 160
                    },
                    {field: 'isResolved', title: '是否解决', templet: '#colIsResolved', width: 90}
                    <@update>
                    , {title: '操作', toolbar: '#grid-bar', width: 200}
                    </@update>
                ]]
            });

            table.on('tool(grid)', function (obj) {
                const row = obj.data;
                if (obj.event === 'compareData') {
                    let btns = ['修复此数据', '标记已解决', '关闭'];
                    if (row.isResolved == 1) {
                        btns = ['关闭'];
                    }
                    layer.open({
                        type: 2,
                        title: '<i class="layui-icon layui-icon-util" style="color: #1E9FFF;"></i>&nbsp;数据修复',
                        content: 'torepair?id=' + row.id,
                        area: ['1280px', '780px'],
                        btn: btns,
                        resize: false,
                        yes: function (index) {
                            if (row.isResolved == 1) {
                                layer.close(index);
                            } else {
                                layer.confirm("确定要修复此条记录吗?", function (i) {
                                    admin.post("repair", {"id": row.id}, function () {
                                        table.reload('grid');
                                        layer.close(i);
                                        layer.close(index);
                                    });
                                });
                            }
                            return false;
                        },
                        btn2: function (index) {
                            layer.confirm("确定要将此条记录标记为已解决吗?", function (i) {
                                admin.post("resolve", {"id": row.id}, function () {
                                    table.reload('grid');
                                    layer.close(i);
                                    layer.close(index);
                                });
                            });
                            return false;
                        },
                        btn3: function (index) {
                            layer.close(index);
                        }
                    });
                } else if (obj.event === 'showDetail') {
                    let resolved = '<span class="layui-badge layui-bg-orange">未解决</span>';
                    if (row.isResolved == 1) {
                        resolved = '<span class="layui-badge layui-bg-green">已解决</span>';
                    }
                    layer.open({
                        type: 2,
                        title: '<i class="layui-icon layui-icon-form" style="color: #1E9FFF;"></i>&nbsp;错误详情, 主题: ' + row.topicName + '&nbsp;&nbsp;' + resolved + '&nbsp;&nbsp;' + layui.util.toDateString(row.rowCreateTime, "yyyy-MM-dd HH:mm:ss"),
                        content: 'todetail?id=' + row.id,
                        area: ['880px', '780px'],
                        btn: ['关闭'],
                        resize: false,
                        yes: function (index) {
                            layer.close(index);
                        }
                    });
                }
            });
        });
    </script>
    </body>
    </html>
</@compress>