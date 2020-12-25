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
            <label class="layui-form-label">名称</label>
            <div class="layui-input-inline">
                <input type="text" name="username" lay-verify="required" class="layui-input" autocomplete="off"
                       value="${property.name}" readonly>
            </div>
            <div class="layui-form-mid layui-word-aux">不可修改</div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">值</label>
            <div class="layui-input-inline">
                <input type="text" id="value" name="value" autofocus="autofocus" autocomplete="off" style="width: 740px"
                       placeholder="请输入配置项值" class="layui-input" value="${property.value}">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">描述信息</label>
            <div class="layui-input-inline">
                <textarea class="layui-textarea" readonly="readonly" style="width: 740px;resize: none">
                    ${property.description}
                </textarea>
            </div>
        </div>

        <div class="layui-form-item layui-hide">
            <input type="button" lay-submit lay-filter="btn_confirm" id="btn_confirm" value="确认">
        </div>
    </div>

    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'form'], function () {
            const $ = layui.$;
            parent.layer.iframeAuto(parent.layer.getFrameIndex(window.name));
            setTimeout(function () {
                const t = $('#value').val();
                $('#value').val("").focus().val(t);
            }, 500);
        });
    </script>
    </body>
    </html>
</@compress>