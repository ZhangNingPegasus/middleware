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
            <label class="layui-form-label">集群类型</label>
            <div class="layui-input-inline" style="width:700px">
                <select name="type" lay-filter="type" lay-verify="required" lay-search>
                    <option value="">请选择集群类型</option>
                    <#list type as t>
                        <option value="${t.code}">${t}</option>
                    </#list>
                </select>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">主机地址</label>
            <div class="layui-input-inline" style="width:700px">
                <select name="server" lay-filter="server" lay-verify="required" lay-search>
                    <option value="">请选择主机地址</option>
                </select>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">通知邮箱</label>
            <div class="layui-input-inline" style="width:700px">
                <input type="email" name="email" placeholder="请填写警告接受邮箱地址" autocomplete="off"
                       class="layui-input">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">AccessToken</label>
            <div class="layui-input-block" style="width:700px">
                <input type="text" name="accessToken" lay-verify="required" autocomplete="off"
                       placeholder="请输入钉钉机器人的access token" class="layui-input" value="${accessToken!''}">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">Secret</label>
            <div class="layui-input-block" style="width:700px">
                <input type="text" name="secret" lay-verify="required" placeholder="请输入钉钉机器人的加签secret"
                       autocomplete="off" class="layui-input" value="${secret!''}">
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
            form.on('select(type)', function (data) {
                $("select[name=server]").html("<option value=\"\">请选择主机地址</option>");
                form.render('select');
                if ($.trim(data.value) === '') {
                    return;
                }
                admin.post('listServers', {'type': data.value, 'opt': 'insert'}, function (res) {
                    $.each(res.data, function (key, val) {
                        const option = $("<option>").val(val).text(val);
                        $("select[name=server]").append(option);
                        form.render('select');
                    });
                    $("select[name=server]").get(0).selectedIndex = 0;
                });
            });
        });
    </script>
    </body>
    </html>
</@compress>