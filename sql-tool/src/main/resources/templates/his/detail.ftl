<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
        <link rel="stylesheet" href="${ctx}/css/codemirror.css" media="all">
        <link rel="stylesheet" href="${ctx}/css/idea.css" media="all">
        <link rel="stylesheet" href="${ctx}/css/blackboard.css" media="all">
        <link rel="stylesheet" href="${ctx}/css/show-hint.css" media="all">
    </head>
    <body>

    <div class="layui-form" lay-filter="layuiadmin-form-admin" id="layuiadmin-form-admin"
         style="padding: 20px 30px 0 0;">

        <div class="layui-form-item">
            <label class="layui-form-label">操作员姓名</label>
            <div class="layui-input-inline">
                <input type="text" style="width: 720px" lay-verify="required" class="layui-input"
                       autocomplete="off" value="${sysSql.name}" readonly>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">IP地址</label>
            <div class="layui-input-inline">
                <input type="text" style="width: 720px" lay-verify="required" class="layui-input"
                       autocomplete="off" value="${sysSql.ip}" readonly>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">执行时间</label>
            <div class="layui-input-inline">
                <input type="text" style="width: 720px" lay-verify="required" class="layui-input"
                       autocomplete="off" value="${sysSql.strExecutionTime}" readonly>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">执行时长</label>
            <div class="layui-input-inline">
                <input type="text" style="width: 720px" lay-verify="required" class="layui-input"
                       autocomplete="off" value="${sysSql.executionDuration}" readonly>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">逻辑SQL</label>
            <div class="layui-input-inline" style="width: 700px">
                <textarea id="txtSQL" class="layui-input" autocomplete="off">${sysSql.logicSql}</textarea>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">实际SQL</label>
            <div class="layui-input-inline" style="width: 700px">
                <textarea id="txtFactSql" class="layui-input" autocomplete="off">${sysSql.factSql}</textarea>
            </div>
        </div>

        <div class="layui-form-item layui-hide">
            <input type="button" lay-submit lay-filter="btn_confirm" id="btn_confirm" value="确认">
        </div>
    </div>
    <script src="${ctx}/js/codemirror.js"></script>
    <script src="${ctx}/js/sql.js"></script>
    <script src="${ctx}/js/show-hint.js"></script>
    <script src="${ctx}/js/sql-hint.js"></script>
    <script src="${ctx}/js/autorefresh.js"></script>
    <script src="${ctx}/js/active-line.js"></script>
    <script src="${ctx}/js/matchbrackets.js"></script>
    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'form'], function () {
            CodeMirror.resolveMode("text/x-sql").keywords["left join"] = true;
            CodeMirror.resolveMode("text/x-sql").keywords["left"] = true;
            CodeMirror.resolveMode("text/x-sql").keywords["right join"] = true;
            CodeMirror.resolveMode("text/x-sql").keywords["right"] = true;
            CodeMirror.resolveMode("text/x-sql").keywords["inner join"] = true;
            CodeMirror.resolveMode("text/x-sql").keywords["inner"] = true;
            CodeMirror.resolveMode("text/x-sql").keywords["when"] = true;
            CodeMirror.resolveMode("text/x-sql").keywords["FROM_DAYS(N)"] = true;
            CodeMirror.resolveMode("text/x-sql").keywords["UPGRADE"] = true;

            const txtSql = CodeMirror.fromTextArea(document.getElementById("txtSQL"), {
                readOnly: "no",
                lineNumbers: true,
                mode: "sql",
                matchBrackets: true,
                spellcheck: true,
                theme: "blackboard"
            });

            const txtFactSql = CodeMirror.fromTextArea(document.getElementById("txtFactSql"), {
                readOnly: "no",
                lineNumbers: true,
                mode: "sql",
                matchBrackets: true,
                spellcheck: true,
                theme: "blackboard"
            });

            txtSql.setSize('auto', '200px');
            txtFactSql.setSize('auto', '200px');

        });
    </script>
    </body>
    </html>
</@compress>