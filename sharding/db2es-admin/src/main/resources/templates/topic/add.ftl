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
                            <option value="">选择对应的逻辑表可自动生成MAPPING</option>
                            <#list tables as table>
                                <option value="${table}">${table}</option>
                            </#list>
                        </select>
                    </div>

                    <div class="layui-inline">主分片数:</div>
                    <div class="layui-inline" style="width: 160px">
                        <input type="number" id="numberOfShards" name="numberOfShards" lay-verify="required"
                               class="layui-input" placeholder="请填写主分片数" value="5" autocomplete="off">
                    </div>

                    <div class="layui-inline">副本分片数:</div>
                    <div class="layui-inline" style="width: 160px">
                        <input type="number" id="numberOfReplicas" name="numberOfReplicas" lay-verify="required"
                               class="layui-input" placeholder="请填写副本分片数" value="${replicaNum}" autocomplete="off">
                    </div>

                    <div class="layui-inline">索引别名年份数:</div>
                    <div class="layui-inline" style="width: 160px">
                        <input type="number" id="aliasOfYears" name="aliasOfYears" lay-verify="required"
                               class="layui-input" placeholder="请填写索引别名年份数" value="3" autocomplete="off">
                    </div>

                    <div class="layui-inline">刷盘间隔:</div>
                    <div class="layui-inline" style="width: 160px">
                        <input type="text" id="refreshInterval" name="refreshInterval" lay-verify="required"
                               class="layui-input" placeholder="请填写索引刷盘间隔" value="1s" autocomplete="off">
                    </div>
                    <div class="layui-inline">描述信息:</div>
                    <div class="layui-inline" style="width: 882px">
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
            <input type="button" lay-submit lay-filter="btnMapping" id="btnMapping" value="">
            <textarea id="mapping" name="mapping" class="layui-input" autocomplete="off"
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
            txtJson.setSize('auto', '510px');

            form.on('select(name)', function (data) {
                const tableName = data.value;
                if ($.trim(tableName) === '') {
                    return;
                }
                admin.post('getMapping', {'tableName': tableName}, function (data) {
                    data = data.data;
                    txtJson.setValue(data);
                });
            });

            $("#btnMapping").click(function () {
                $("#mapping").html(txtJson.getValue());
            });
        });
    </script>
    </body>
    </html>
</@compress>