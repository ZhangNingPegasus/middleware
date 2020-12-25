<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
    </head>
    <body>

    <div class="layui-form" lay-filter="layuiadmin-form-admin" id="layuiadmin-form-admin"
         style="padding: 20px 30px 0 0;">

        <div class="layui-form-item">
            <label class="layui-form-label">DB2ES ID</label>
            <div class="layui-input-inline" style="width: 730px">
                <select name="db2esId" lay-verify="required" lay-search>
                    <option value="">请选择已上线的DB2ES</option>
                    <#list db2esList as db2es>
                        <option value="${db2es.id}">${db2es.id} - (${db2es.ip} : ${db2es.port})</option>
                    </#list>
                </select>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">索引名称</label>
            <div class="layui-input-inline" style="width: 730px">
                <select name="topicId" lay-verify="required" lay-search>
                    <option value="">请选择已设定的索引名称</option>
                    <#list topicList as topic>
                        <option value="${topic.id}">${topic.name}</option>
                    </#list>
                </select>
            </div>
        </div>

        <div class="layui-form-item" style="margin-left: 20px;">
            <table id="grid" lay-filter="grid"></table>

            <script type="text/html" id="grid-bar">
                <@update>
                    {{#  if(!d.isActive){ }}
                    <a class="layui-btn layui-btn-xs layui-btn-danger" lay-event="remove"><i
                                class="layui-icon layui-icon-delete"></i>删除</a>
                    {{#  } }}
                </@update>
            </script>
        </div>

        <div class="layui-form-item layui-hide">
            <input type="button" lay-submit lay-filter="btn_confirm" id="btn_confirm" value="确认">
        </div>
    </div>
    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'form', 'table'], function () {
            const admin = layui.admin, $ = layui.$, form = layui.form, table = layui.table;
            tableErrorHandler();
            table.render({
                elem: '#grid',
                url: 'listTopicDb2Es',
                method: 'post',
                cellMinWidth: 80,
                page: false,
                limit: 9999999,
                limits: [9999999],
                even: true,
                text: {
                    none: '暂无相关数据'
                },
                cols: [[
                    {field: 'db2esId', title: 'DB2ES ID', width: 250},
                    {field: 'name', title: '主题(ES索引)名称'},
                    {title: '操作', toolbar: '#grid-bar', width: 80}
                ]],
                done: function (r) {
                    if (!r.success) {
                        return;
                    }
                    layuiRowspan('key', 1);
                }
            });

            table.on('tool(grid)', function (obj) {
                const row = obj.data;
                if (obj.event === 'remove') {
                    if (row.isActive) {
                        return;
                    }
                    layer.confirm("确定要把主题[" + row.name + "]从db2es_id=[" + row.db2esId + "]删除吗?", function (index) {
                        admin.post("remove", row, function () {
                            admin.post("listUnused", {}, function (data) {
                                data = data.data;
                                const topicId = $("select[name=topicId]");
                                topicId.html("<option value=\"\">请选择已设定的索引名称</option>");

                                $.each(data, function (i, item) {
                                    topicId.append($("<option>").val(item.id).text(item.name));
                                });
                                layui.form.render('select');

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