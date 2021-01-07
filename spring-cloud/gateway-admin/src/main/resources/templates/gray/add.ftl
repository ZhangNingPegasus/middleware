<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
        <style type="text/css">
            .layui-table-cell {
                overflow: visible !important;
            }

            td .layui-form-select {
                margin-top: -10px;
                margin-left: -15px;
                margin-right: -15px;
            }

            .layui-form-select dl {
                z-index: 9999;
            }

            .layui-table-cell {
                overflow: visible;
            }

            .layui-table-box {
                overflow: visible;
            }

            .layui-table-body {
                overflow: visible;
            }

            .div-inline {
                display: inline
            }
        </style>
    </head>
    <body>

    <div class="layui-form" lay-filter="layuiadmin-form-admin" id="layuiadmin-form-admin"
         style="padding: 5px 5px 5px 5px;">

        <div class="layui-form-item">
            <div class="layui-inline">&nbsp;&nbsp;&nbsp;&nbsp;分支标识</div>
            <div class="layui-inline" style="width:150px">
                <input type="text" name="id" class="layui-input" placeholder="请填写分支标识" autocomplete="off"
                       lay-verify="required">
            </div>

            <div class="layui-inline" style="margin-left: 50px">流量比例</div>
            <div class="layui-inline" style="width:130px">
                <input type="number" name="weight" class="layui-input" placeholder="请填写流量比例" autocomplete="off"
                       lay-verify="required" value="0">
            </div>
            <div class="layui-inline" style="margin-left: -10px"><b>%</b></div>

            <div class="layui-inline" style="margin-left: 50px">描述信息</div>
            <div class="layui-inline" style="width:300px">
                <input type="text" name="description" class="layui-input" placeholder="请填写描述信息" autocomplete="off"
                       lay-verify="required">
            </div>
        </div>

        <hr/>

        <table id="grid" lay-filter="grid"></table>

        <script type="text/html" id="grid-bar">
            <@delete>
                <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="del"><i
                            class="layui-icon layui-icon-delete"></i>删除</a>
            </@delete>
        </script>

        <script type="text/html" id="grid-toolbar">
            <div class="layui-btn-container">
                <@update>
                    <button class="layui-btn layui-btn-primary layui-btn-sm layuiadmin-btn-admin" lay-event="inspect">
                        <i class="layui-icon layui-icon-rss"></i>&nbsp;&nbsp;测试调用链路
                    </button>
                </@update>
                <@select>
                    <button class="layui-btn layui-btn-primary layui-btn-sm layuiadmin-btn-admin" lay-event="refresh">
                        <i class="layui-icon layui-icon-refresh-3"></i>&nbsp;&nbsp;刷新服务列表
                    </button>
                </@select>
            </div>
        </script>

        <script type="text/html" id="tName">
            {{#  if(d.versionList.length > 0){ }}
            <span class="layui-badge layui-bg-green">{{ d.name }}</span>
            {{#  } else { }}
            <span class="layui-badge layui-bg-orange">{{ d.name }}</span>
            {{#  } }}
        </script>

        <script type="text/html" id="tVersion">
            {{#  if(d.versionList.length > 0){ }}
            <select name='version' lay-filter='version' lay-search>
                <option value="">请选择版本号</option>
                {{# layui.each(d.versionList, function(index, item){ }}
                <option value="{{item}}" {{d.version==item?'selected="selected"':''}}>
                    {{item}}
                </option>
                {{# }); }}
            </select>
            {{#  } else { }}
            <span class="layui-badge layui-bg-orange">此服务缺少运行实例或没有配置版本号[spring.application.version]</span>
            {{#  } }}
        </script>

        <script type="text/html" id="tVersionSize">
            {{#  if(d.versionList.length > 0){ }}
            <span class="layui-badge layui-bg-green">{{ d.versionList.length }}</span>
            {{#  } else { }}
            <span class="layui-badge layui-bg-orange">{{ d.versionList.length }}</span>
            {{#  } }}
        </script>

        <div class="layui-form-item layui-hide">
            <input type="button" lay-submit lay-filter="btn_confirm" id="btn_confirm" value="确认">
        </div>
    </div>
    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table', 'form'], function () {
            const table = layui.table, form = layui.form, $ = layui.$, admin = layui.admin;

            function reload(data) {
                table.reload('grid', {url: null, 'data': data});
            }

            tableErrorHandler();
            table.render({
                elem: '#grid',
                url: 'listServices',
                toolbar: '#grid-toolbar',
                method: 'post',
                cellMinWidth: 80,
                page: false,
                limit: 99999999,
                limits: [99999999],
                even: true,
                text: {
                    none: '暂无相关数据'
                },
                cols: [[
                    {type: 'numbers', title: '序号', width: 100},
                    {title: '服务ID', templet: '#tName'},
                    {title: '版本号', unresize: true, align: "center", templet: '#tVersion', width: 400},
                    {title: '版本数量', align: "center", templet: '#tVersionSize', width: 150},
                    {fixed: 'right', title: '操作', align: "center", toolbar: '#grid-bar', width: 80}
                ]],
                done: function (res) {

                }
            });

            table.on('tool(grid)', function (obj) {
                const data = obj.data;
                if (obj.event === 'del') {
                    const d = table.cache['grid'];
                    $.each(d, function (index, item) {
                        if (item) {
                            if (item.name === data.name) {
                                d.remove(item);
                                return;
                            }
                        }
                    });
                    reload(d);
                }
            });

            table.on('toolbar(grid)', function (obj) {
                if (obj.event === 'refresh') {
                    table.reload('grid', {url: "listServices"});
                } else if (obj.event === 'inspect') {
                    const data = table.cache['grid'];
                    const service = [];
                    $.each(data, function (index, item) {
                        if (item.version) {
                            service.push({
                                "service": item.name,
                                "version": item.version
                            });
                        }
                    });

                    if (service.length < 1) {
                        admin.error("系统提示", "请先选择服务对应的版本");
                        return;
                    }

                    layer.open({
                        type: 2,
                        title: '<i class="layui-icon layui-icon-rss" style="color: #1E9FFF;"></i>&nbsp;测试调用链路',
                        content: 'toinspect?data=' + escape(JSON.stringify(service)),
                        area: ['960px', '700px'],
                        btn: ['取消'],
                        closeBtn: 0,
                        resize: false
                    });
                }
            });

            form.on('select(version)', function (obj) {
                const dataIndex = $(obj.elem).parent().parent().parent().attr('data-index');
                const data = table.cache['grid'];
                data[dataIndex]['version'] = obj.value;
                reload(data);
            });
        });
    </script>
    </body>
    </html>
</@compress>