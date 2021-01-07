<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <#include "../common/layui.ftl">
</head>
<body style="overflow-y: hidden">
<div class="layui-fluid">

    <div class="layui-form layui-card-header layuiadmin-card-header-auto">
        <div class="layui-form-item">
            <div class="layui-inline">管理员名称</div>
            <div class="layui-inline">
                <select name="sysAdminId">
                    <option value="">请选择管理员</option>
                    <#list admins as admin>
                        <option value="${admin.id}">${admin.name}</option>
                    </#list>
                </select>
            </div>
            <div class="layui-inline">IP地址</div>
            <div class="layui-inline">
                <input type="text" name="ip" placeholder="请输入IP地址" autocomplete="off"
                       class="layui-input">
            </div>
            <div class="layui-inline">执行时长(单位: 毫秒)</div>
            <div class="layui-inline">
                <input type="number" name="fromExecutionTime" placeholder="请输入执行时长的上界" autocomplete="off"
                       class="layui-input">
            </div>
            <div class="layui-inline">
                至
            </div>
            <div class="layui-inline">
                <input type="number" name="toExecutionTime" placeholder="请输入执行时长的下界" autocomplete="off"
                       class="layui-input">
            </div>
            <div class="layui-inline">
                <button class="layui-btn layuiadmin-btn-admin" lay-submit lay-filter="search">
                    <i class="layui-icon layui-icon-search layuiadmin-button-btn"></i>
                </button>
            </div>
        </div>
    </div>

    <div class="layui-card-body" style="margin-top: -30px">
        <table class="layui-hide" id="grid" lay-filter="grid"></table>
        <script type="text/html" id="grid-bar">
            <a class="layui-btn layui-btn-normal layui-btn-xs" lay-event="detail"><i
                        class="layui-icon layui-icon-read"></i>查看详情</a>
        </script>
    </div>
</div>

<script type="text/javascript">
    layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table'], function () {
        const form = layui.form, table = layui.table;
        tableErrorHandler();
        form.on('submit(search)', function (data) {
            const field = data.field;
            table.reload('grid', {page: {curr: 1}, where: field});
        });
        table.render({
            elem: '#grid',
            url: 'list',
            height: 'full-90',
            method: 'post',
            cellMinWidth: 80,
            page: true,
            limit: 18,
            limits: [18],
            even: true,
            cols: [[
                {type: 'numbers', title: '序号', width: 50},
                {field: 'name', title: '操作员名字', width: 150},
                {field: 'ip', title: 'IP地址', width: 150},
                {field: 'shortSql', title: 'SQL', unresize: true},
                {field: 'strExecutionTime', title: '执行时间', unresize: true, sort: true, width: 200},
                {field: 'executionDuration', title: '执行时长(毫秒)', unresize: true, sort: true, width: 170},
                {fixed: 'right', title: '操作', align: "center", toolbar: '#grid-bar', width: 105}
            ]]
        });

        table.on('tool(grid)', function (obj) {
            const data = obj.data;
            if (obj.event === 'detail') {
                layer.open({
                    type: 2,
                    title: '<i class="layui-icon layui-icon-read" style="color: #1E9FFF;"></i>&nbsp;SQL历史',
                    content: 'todetail?id=' + data.id,
                    area: ['880px', '780px'],
                    btn: ['确定'],
                    resize: false
                });
            }
        });

    });
</script>
</body>
</html>