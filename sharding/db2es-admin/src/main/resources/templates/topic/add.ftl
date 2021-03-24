<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
        <link rel="stylesheet" href="${ctx}/css/codemirror.css" media="all">
        <link rel="stylesheet" href="${ctx}/css/idea.css" media="all">
    </head>
    <body>

    <div class="layui-fluid layui-form">
        <div class="layui-card">
            <div class="layui-form layui-card-header layuiadmin-card-header-auto">
                <div class="layui-form-item">
                    <div class="layui-inline">主题名称:</div>
                    <div class="layui-inline" style="width: 330px">
                        <select name="name" lay-filter="name" lay-verify="required" lay-search>
                            <option value="">选择对应的逻辑表可自动生成索引Source</option>
                            <#list tables as table>
                                <option value="${table}">${table}</option>
                            </#list>
                        </select>
                    </div>

                    <div class="layui-inline">数据保留年份数:</div>
                    <div class="layui-inline" style="width: 160px">
                        <input type="number" id="aliasOfYears" name="aliasOfYears" lay-verify="required"
                               class="layui-input" placeholder="请填写数据保留年份数" value="3" autocomplete="off">
                    </div>

                    <div class="layui-inline">描述信息:</div>
                    <div class="layui-inline" style="width: 426px">
                        <input type="text" id="description" name="description" class="layui-input" placeholder="请填写描述信息"
                               autocomplete="off">
                    </div>
                </div>
            </div>

            <div class="layui-card-body">
                <textarea id="txtJson" name="txtJson" class="layui-input" autocomplete="off"
                          style="resize: none"></textarea>
            </div>
        </div>

        <div class="layui-form-item layui-hide">
            <input type="button" lay-submit lay-filter="btn_confirm" id="btn_confirm" value="">
            <input type="button" lay-submit lay-filter="btnSource" id="btnSource" value="">
            <textarea id="source" name="source" class="layui-input" autocomplete="off"
                      style="resize: none"></textarea>
        </div>
    </div>

    <script src="${ctx}/js/codemirror.js"></script>
    <script src="${ctx}/js/autorefresh.js"></script>
    <script src="${ctx}/js/active-line.js"></script>
    <script src="${ctx}/js/matchbrackets.js"></script>
    <script src="${ctx}/js/javascript.js"></script>
    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table'], function () {
            const $ = layui.$, admin = layui.admin, form = layui.form;
            const txtJson = CodeMirror.fromTextArea(document.getElementById("txtJson"), {
                lineNumbers: true,
                indentUnit: 4,
                mode: "application/json",
                matchBrackets: true,
                theme: "idea",
                styleActiveLine: true,
                autoRefresh: true
            });
            txtJson.setSize('auto', '555px');

            form.on('select(name)', function (data) {
                const tableName = data.value;
                if ($.trim(tableName) === '') {
                    return;
                }
                admin.post('getSource', {'tableName': tableName}, function (data) {
                    data = data.data;
                    txtJson.setValue(data);
                });
            });

            $("#btnSource").click(function () {
                $("#source").html(txtJson.getValue());
            });
        });
    </script>
    </body>
    </html>
</@compress>