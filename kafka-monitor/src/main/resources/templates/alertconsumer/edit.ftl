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
            <label class="layui-form-label">消费组名称</label>
            <div class="layui-input-inline" style="width:700px">
                <select name="groupId" lay-filter="groupId" autofocus="autofocus"
                        lay-verify="required" lay-search>
                    <option value="">请选择消费组</option>
                    <#list consumers as consumer>
                        <option value="${consumer.groupId}" ${(item.groupId==consumer.groupId)?string('selected=selected','')}>${consumer.groupId}</option>
                    </#list>
                </select>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">主题名称</label>
            <div class="layui-input-inline" style="width:700px">
                <select name="topicName" lay-filter="topicName"
                        lay-verify="required" lay-search>
                    <option value="">请选择主题</option>
                    <#list topics as topic>
                        <option value="${topic}" ${(item.topicName==topic)? string('selected=selected','')}>${topic}</option>
                    </#list>
                </select>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">积累阀值</label>
            <div class="layui-input-inline" style="width:700px">
                <input type="number" name="lagThreshold" lay-verify="required" placeholder="请填写消费组的消息积累阀值"
                       autocomplete="off"
                       class="layui-input" value="${item.lagThreshold}">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">通知邮箱</label>
            <div class="layui-input-inline" style="width:700px">
                <input type="email" name="email" placeholder="请填写警告接受邮箱地址" autocomplete="off"
                       class="layui-input" value="${item.email}">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">AccessToken</label>
            <div class="layui-input-block" style="width:700px">
                <input type="text" name="accessToken" lay-verify="required" autocomplete="off"
                       placeholder="请输入钉钉机器人的access token" class="layui-input" value="${item.accessToken}">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">Secret</label>
            <div class="layui-input-block" style="width:700px">
                <input type="text" name="secret" lay-verify="required" placeholder="请输入钉钉机器人的加签secret"
                       autocomplete="off" class="layui-input" value="${item.secret}">
            </div>
        </div>

        <div class="layui-form-item layui-hide">
            <input type="button" lay-submit lay-filter="btn_confirm" id="btn_confirm" value="确认">
        </div>
    </div>
    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'form'], function () {
            const admin = layui.admin, $ = layui.$, form = layui.form;
            parent.layer.iframeAuto(parent.layer.getFrameIndex(window.name));

            form.on('select(groupId)', function (data) {
                $("select[name=topicName]").html("<option value=\"\">请选择主题名称</option>");
                form.render('select');
                if ($.trim(data.value) === '') {
                    return;
                }

                admin.post('listTopics', {'groupId': data.value, 'opt': 'update'}, function (res) {
                    $.each(res.data, function (key, val) {
                        const option = $("<option>").val(val).text(val);
                        $("select[name=topicName]").append(option);
                        form.render('select');
                    });
                    $("select[name=topicName]").get(0).selectedIndex = 0;
                });
            });

        });
    </script>
    </body>
    </html>
</@compress>