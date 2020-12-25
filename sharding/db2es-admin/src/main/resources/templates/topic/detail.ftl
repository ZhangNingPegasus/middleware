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
                        <input type="text" class="layui-input" value="${topic.name!''}" readonly="readonly">
                    </div>

                    <div class="layui-inline">主分片数:</div>
                    <div class="layui-inline" style="width: 160px">
                        <input type="text" class="layui-input" value="${topic.numberOfShards!''}" readonly="readonly">
                    </div>

                    <div class="layui-inline">副本分片数:</div>
                    <div class="layui-inline" style="width: 160px">
                        <input type="text" class="layui-input" value="${topic.numberOfReplicas!''}" readonly="readonly">
                    </div>

                    <div class="layui-inline">索引别名年份数:</div>
                    <div class="layui-inline" style="width: 160px">
                        <input type="text" class="layui-input" value="${topic.aliasOfYears!''}" readonly="readonly">
                    </div>

                    <div class="layui-inline">刷盘间隔:</div>
                    <div class="layui-inline" style="width: 160px">
                        <input type="text" class="layui-input" value="${topic.refreshInterval!''}" readonly="readonly">
                    </div>
                    <div class="layui-inline">描述信息:</div>
                    <div class="layui-inline" style="width: 882px">
                        <input type="text" class="layui-input" value="${topic.description!''}" readonly="readonly">
                    </div>
                </div>
            </div>

            <div class="layui-card-body">
                <textarea id="txtJson" class="layui-input"></textarea>
            </div>
        </div>
    </div>

    <script src="${ctx}/js/codemirror.js"></script>
    <script src="${ctx}/js/autorefresh.js"></script>
    <script src="${ctx}/js/active-line.js"></script>
    <script src="${ctx}/js/matchbrackets.js"></script>
    <script src="${ctx}/js/javascript.js"></script>
    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table'], function () {
            const txtJson = CodeMirror.fromTextArea(document.getElementById("txtJson"), {
                value: '',
                readOnly: "no",
                lineNumbers: true,
                indentUnit: 4,
                mode: "application/json",
                matchBrackets: true,
                theme: "idea",
                styleActiveLine: true,
                autoRefresh: true
            });
            txtJson.setSize('auto', '510px');
            txtJson.setValue(JSON.stringify(${topic.mapping}, null, "\t"));
        });
    </script>
    </body>
    </html>
</@compress>