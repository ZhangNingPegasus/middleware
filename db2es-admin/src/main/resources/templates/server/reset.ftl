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
            <label class="layui-form-label">位点时间</label>
            <div class="layui-input-inline" style="width:700px">
                <input type="text" id="timestamp" name="timestamp" class="layui-input"
                       placeholder="请选择偏移量时间 (推荐)" autocomplete="off">
                <input type="button" class="layui-btn" lay-filter="btnCalc" id="btnCalc" value="计算偏移量">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">偏移量</label>
            <div class="layui-input-inline" style="width:700px">
                <input type="number" id="offset" name="offset" placeholder="请填写消息的偏移量" autocomplete="off"
                       class="layui-input">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label"></label>
            <span>
                <i class="layui-icon layui-icon-about"></i>&nbsp;
                <b><i>位点时间</i></b>:&nbsp;&nbsp;根据时间重置该主题的消费位点&nbsp;&nbsp;&nbsp;&nbsp;<span
                        class="layui-badge layui-bg-blue">推荐</span>
            </span>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label"></label>
            <span>
                <i class="layui-icon layui-icon-about"></i>&nbsp;
                <b><i>偏移量</i></b>:&nbsp;&nbsp;直接指定该主题从哪个偏移量开始消费
            </span>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label"></label>
            <span>
                <i class="layui-icon layui-icon-about"></i>&nbsp;
                <b><i>位点时间</i></b>&nbsp;&nbsp;的优先级要高于&nbsp;<b><i>偏移量</i></b>
            </span>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label"></label>
            <span>
                <i class="layui-icon layui-icon-about"></i>&nbsp;
                <b><i>${topicName}</i></b>的当前位点时间: <b><i>${timestamp}</i></b>
            </span>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label"></label>
            <span>
                <i class="layui-icon layui-icon-about"></i>&nbsp;
                <b><i>${topicName}</i></b>的当前偏移量: <b><i>${offset}</i></b>
            </span>
        </div>

        <div class="layui-form-item layui-hide">
            <input type="button" lay-submit lay-filter="btn_confirm" id="btn_confirm" value="确认">
        </div>
    </div>

    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'laydate', 'form'], function () {
            const $ = layui.$, laydate = layui.laydate, admin = layui.admin;
            laydate.render({
                elem: '#timestamp',
                type: 'datetime',
                min: -7,
                max: 0,
                btns: ['confirm']
            });

            $("#btnCalc").click(function () {
                if ($("#timestamp").val().trim() === '') {
                    admin.error("系统提醒", "位点时间不允许为空");
                    return;
                }
                admin.post('calcOffset', {
                    topicName: '${topicName}',
                    timestamp: $("#timestamp").val()
                }, function (res) {
                    const data = res.data;
                    if (data.offset >= 0) {
                        $("#offset").val(data.offset);
                    } else {
                        $("#offset").val("");
                        admin.error("操作失败", "主题[${topicName}]无法根据时间戳[" + $("#timestamp").val() + "]定位到消费位点");
                    }
                });
            });
        });
    </script>
    </body>
    </html>
</@compress>