<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <#include "../common/layui.ftl">
    <link rel="stylesheet" href="${ctx}/css/codemirror.css" media="all">
    <link rel="stylesheet" href="${ctx}/css/idea.css" media="all">
    <link rel="stylesheet" href="${ctx}/css/merge.css" media="all">
</head>
<style>
    .CodeMirror-merge, .CodeMirror-merge .CodeMirror {
        height: 590px;
    }

    .CodeMirror-merge-2pane .CodeMirror-merge-gap {
        width: 0%;
    }

    .CodeMirror-merge-2pane .CodeMirror-merge-pane {
        width: 50%;
    }

    .CodeMirror-merge-r-chunk-end {
        border-bottom: 1px solid #9E9E00;
    }

    .CodeMirror-merge-r-chunk-start {
        border-top: 1px solid #9E9E00;
    }

    .CodeMirror-merge-r-chunk {
        background: #9E9E32;
    }
</style>
<body>

<div class="layui-form" lay-filter="layuiadmin-form-admin" id="layuiadmin-form-admin" style="padding: 20px 30px 0 0;">
    <div id="view"></div>
</div>

<div id="val1" style="display: none">${dbJson!''}</div>
<div id="val2" style="display: none">${esJson!''}</div>
<script src="${ctx}/js/codemirror.js"></script>
<script src="${ctx}/js/autorefresh.js"></script>
<script src="${ctx}/js/active-line.js"></script>
<script src="${ctx}/js/matchbrackets.js"></script>
<script src="${ctx}/js/javascript.js"></script>
<script src="${ctx}/js/diff_match_patch.js"></script>
<script src="${ctx}/js/merge.js"></script>

<script type="text/javascript">
    layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table'], function () {
        const $ = layui.$;
        CodeMirror.MergeView(document.getElementById("view"), {
            value: $("#val1").html(),
            orig: $("#val2").html(),
            origLeft: null,
            lineNumbers: true,
            mode: "application/json",
            highlightDifferences: true,
            connect: "align",
            theme: "idea",
            collapseIdentical: false,
            revertButtons: false,
            allowEditingOriginals: false
        });
        $(".CodeMirror-merge-gap").remove();

        $(".CodeMirror-merge-pane.CodeMirror-merge-editor").prepend("<span style='width: 100%' class='layui-badge layui-bg-green'><h3 style='margin-left: 30px;'>数据库</h3></span>");
        $(".CodeMirror-merge-pane.CodeMirror-merge-right.CodeMirror-merge-pane-rightmost").prepend("<span style='width: 100%' class='layui-badge layui-bg-blue'><h3 style='margin-left: 30px;'>Elastic-Search</h3></span>");
    });
</script>
</body>
</html>