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
            <label class="layui-form-label">Id</label>
            <div class="layui-input-inline">
                <input type="text" name="clientId" lay-verify="required" class="layui-input layui-disabled"
                       style="width: 740px"
                       autocomplete="off" value="${app.clientId}" readonly="readonly">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">Secret</label>
            <div class="layui-input-inline">
                <input type="text" name="clientSecret" lay-verify="required" class="layui-input layui-disabled"
                       style="width: 740px"
                       autocomplete="off" value="${app.clientSecret}" readonly="readonly">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">应用名称</label>
            <div class="layui-input-inline">
                <input type="text" name="name" lay-verify="required" class="layui-input" style="width: 740px"
                       placeholder="请输入应用名称" autocomplete="off" value="${app.name}">
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">是否管理员</label>
            <div class="layui-input-inline">
                <input type="radio" name="isAdmin" value="true" title="是" ${(app.isAdmin)?string('checked','')}>
                <input type="radio" name="isAdmin" value="false" title="否" ${(app.isAdmin)?string('','checked')}>
            </div>
        </div>

        <div class="layui-form-item">
            <label class="layui-form-label">描述信息</label>
            <div class="layui-input-inline">
                <textarea name="description" placeholder="请输入描述信息" class="layui-textarea"
                          style="resize: none;width: 740px;height:300px">${app.description}</textarea>
            </div>
        </div>

        <div class="layui-form-item layui-hide">
            <input type="button" lay-submit lay-filter="btn_confirm" id="btn_confirm" value="确认">
        </div>
    </div>
    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'form'], function () {

        });
    </script>
    </body>
    </html>
</@compress>