<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
    </head>
    <body>

    <div class="layui-form" lay-filter="layuiadmin-form-admin" id="layuiadmin-form-admin"
         style="padding: 20px 30px 0 0;">

        <div class="layui-card-body">
            <table id="grid" lay-filter="grid"></table>

            <script type="text/html" id="colPartitionId">
                <span class="layui-badge layui-bg-blue">{{ d.partitionId }}</span>
            </script>

            <script type="text/html" id="colLogSize">
                <span class="layui-badge layui-bg-blue">{{ d.logSize }}</span>
            </script>

            <div class="layui-form-item">
            <span>
                <i class="layui-icon layui-icon-about"></i>&nbsp;
                <b><i>偏移量</i></b>:&nbsp;&nbsp;直接指定该主题的分区从哪个偏移量开始消费
            </span>
            </div>
            <div class="layui-form-item">
            <span>
                <i class="layui-icon layui-icon-about"></i>&nbsp;
                <b><i>注意</i></b>:&nbsp;&nbsp;设置偏移量需要先停止消费端程序, 如果是强制关闭, 需要等待Kafka服务器自动回收消费端
            </span>
            </div>
        </div>


        <div class="layui-form-item layui-hide">
            <input type="button" lay-submit lay-filter="btn_confirm" id="btn_confirm" value="确认">
        </div>
    </div>

    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table', 'laydate', 'form'], function () {
            const table = layui.table, $ = layui.$, admin = layui.admin;
            table.render({
                elem: '#grid',
                url: 'listOffsetVo?groupId=${groupId}&topicName=${topicName}',
                method: 'post',
                cellMinWidth: 80,
                page: false,
                even: true,
                text: {
                    none: '暂无相关数据'
                },
                cols: [[
                    {field: 'partitionId', title: '分区号', templet: '#colPartitionId', width: 100},
                    {field: 'logSize', templet: "#colLogsize", title: '消息数量', templet: '#colLogSize', width: 151},
                    {field: 'tOffset', title: '偏移量(<span style="color:red">* 必填</span>)', edit: 'text', width: 282},
                    {field: 'tMetadata', title: '元信息(<span style="color:gray">可不填</span>)', edit: 'text', width: 282}
                ]],
                done: function () {
                    $("table[class='layui-table']").find("tr[data-index='0']").find("td[data-key='1-0-2']").click();
                }
            });
        });
    </script>
    </body>
    </html>
</@compress>