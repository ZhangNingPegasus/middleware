<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <#include "../common/layui.ftl">
    <link rel="stylesheet" href="${ctx}/css/codemirror.css" media="all">
    <link rel="stylesheet" href="${ctx}/css/idea.css" media="all">
</head>
<body>

<div class="layui-form" lay-filter="layuiadmin-form-admin" id="layuiadmin-form-admin"
     style="padding: 20px 30px 0 0;">
    <div class="layui-form-item">
        <label class="layui-form-label">主键值</label>
        <div class="layui-input-inline" style="width:700px">
            <input type="text" class="layui-input" readonly="readonly" value="${errorLog.primaryKeyValue!''}">
        </div>
    </div>

    <div class="layui-form-item">
        <label class="layui-form-label">数据库名</label>
        <div class="layui-input-inline" style="width:700px">
            <input type="text" class="layui-input" readonly="readonly" value="${errorLog.databaseName!''}">
        </div>
    </div>

    <div class="layui-form-item">
        <label class="layui-form-label">数据表名</label>
        <div class="layui-input-inline" style="width:700px">
            <input type="text" class="layui-input" readonly="readonly" value="${errorLog.tableName!''}">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">索引名称</label>
        <div class="layui-input-inline" style="width:700px">
            <input type="text" class="layui-input" readonly="readonly" value="${errorLog.indexName!''}">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">异常消息</label>
        <div class="layui-input-inline" style="width:700px">
            <textarea class="layui-input" autocomplete="off" style="resize: none;height: 80px"
                      readonly="readonly">${errorLog.errorMessage!''}</textarea>
        </div>
    </div>

    <div class="layui-form-item">
        <label class="layui-form-label">消息主题</label>
        <div class="layui-input-inline" style="width:700px">
            <input type="text" class="layui-input" readonly="readonly" value="${errorLog.topicName!''}">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">消息分区</label>
        <div class="layui-input-inline" style="width:700px">
            <input type="text" class="layui-input" readonly="readonly" value="${errorLog.partition!''}">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">消息偏移量</label>
        <div class="layui-input-inline" style="width:700px">
            <input type="text" class="layui-input" readonly="readonly" value="${errorLog.offset!''}">
        </div>
    </div>
    <div class="layui-form-item">
        <label class="layui-form-label">消息体</label>
        <div class="layui-input-inline" style="width:700px">
            <textarea id="txtJson" class="layui-input" autocomplete="off"
                      style="resize: none"><#if consumerRecordJson??>${consumerRecordJson}<#else>${errorLog.consumerRecord!''}</#if></textarea>
        </div>
    </div>
</div>

<script src="${ctx}/js/codemirror.js"></script>
<script src="${ctx}/js/autorefresh.js"></script>
<script src="${ctx}/js/active-line.js"></script>
<script src="${ctx}/js/matchbrackets.js"></script>
<script src="${ctx}/js/javascript.js"></script>
<script type="text/javascript">
    layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table'], function () {
        const txtJson = CodeMirror.fromTextArea(document.getElementById("txtJson"), {
            readOnly: "no",
            lineNumbers: false,
            indentUnit: 4,
            mode: "application/json",
            matchBrackets: true,
            theme: "idea",
            styleActiveLine: true,
            autoRefresh: true
        });
        txtJson.setSize('auto', '2000px');
    });
</script>
</body>
</html>