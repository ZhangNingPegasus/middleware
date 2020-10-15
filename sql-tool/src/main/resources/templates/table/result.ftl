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

    <div class="layui-form" lay-filter="layuiadmin-form-admin" id="layuiadmin-form-admin">

        <div class="layui-tab layui-tab-brief" lay-filter="tabSql">
            <ul class="layui-tab-title">
                <#if result??>
                    <#list result?keys as key>
                        <li lay-id="id_${key_index}" <#if key_index==0>class="layui-this"</#if>><i
                                    class="layui-icon layui-icon-align-left"></i>&nbsp;&nbsp;${key}</li>
                    </#list>
                </#if>
            </ul>
            <div class="layui-tab-content" style="height: 100px;">
                <#if result??>
                    <#list result?keys as key>
                        <div class="layui-tab-item layui-show">
                            <textarea id="txtSql_${key_index}" class="layui-input" autocomplete="off"
                                      style="resize: none">${result[key]}</textarea>
                        </div>
                    </#list>
                </#if>
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
            const element = layui.element, $ = layui.$;
            CodeMirror.resolveMode("text/x-sql").keywords["left join"] = true;
            CodeMirror.resolveMode("text/x-sql").keywords["left"] = true;
            CodeMirror.resolveMode("text/x-sql").keywords["right join"] = true;
            CodeMirror.resolveMode("text/x-sql").keywords["right"] = true;
            CodeMirror.resolveMode("text/x-sql").keywords["inner join"] = true;
            CodeMirror.resolveMode("text/x-sql").keywords["inner"] = true;
            CodeMirror.resolveMode("text/x-sql").keywords["when"] = true;
            CodeMirror.resolveMode("text/x-sql").keywords["FROM_DAYS(N)"] = true;
            CodeMirror.resolveMode("text/x-sql").keywords["UPGRADE"] = true;

            <#if result??>
            <#list result?keys as key>

            setTimeout(() => {
                const txtSql_${key_index} = CodeMirror.fromTextArea(document.getElementById('txtSql_${key_index}'), {
                    readOnly: "no",
                    lineNumbers: true,
                    mode: "sql",
                    matchBrackets: true,
                    spellcheck: true,
                    theme: "blackboard"
                });
                txtSql_${key_index}.setSize('auto', '610px');

                setTimeout(() => {
                    txtSql_${key_index}.focus();
                    txtSql_${key_index}.refresh();
                }, 50);

            }, 100);

            setTimeout(() => {
                element.tabChange('tabSql', 'id_0');
            }, 200);

            </#list>
            </#if>
        });
    </script>
    </body>
    </html>
</@compress>