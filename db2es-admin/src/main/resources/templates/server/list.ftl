<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
    </head>
    <body>

    <div class="layui-fluid">
        <div class="layui-card">
            <div class="layui-form layui-card-header layuiadmin-card-header-auto">
                <div class="layui-form-item">
                    <div class="layui-inline">主题名称</div>
                    <div class="layui-inline" style="width:500px">
                        <input type="text" name="topicName" placeholder="请输入主题名称" autocomplete="off"
                               class="layui-input">
                    </div>
                    <div class="layui-inline">
                        <button id="btnSearch" class="layui-btn layuiadmin-btn-admin" lay-submit lay-filter="search">
                            <i class="layui-icon layui-icon-search layuiadmin-button-btn"></i>
                        </button>
                    </div>
                </div>
            </div>

            <div class="layui-card-body">
                <table id="grid" lay-filter="grid"></table>

                <script type="text/html" id="colHost">
                    <a href="javascript:void(0)" class="host layui-table-link" tag="{{ d.topicName }}">{{ d.host }}</a>
                </script>

                <script type="text/html" id="colTopicName">
                    <a href="javascript:void(0)" class="topicName layui-table-link" tag="{{ d.topicName }}">
                        {{ d.topicName }}
                    </a>
                    {{# if(d.isOptimize){ }}
                    &nbsp;&nbsp;&nbsp;&nbsp;<span class="layui-badge layui-bg-blue"><small><i>加速中</i></small></span>
                    {{# } }}
                </script>

                <script type="text/html" id="colIsActive">
                    {{#  if(d.isActive){ }}

                    {{#  if(d.errorMsg == ''){ }}
                    <span class="layui-badge layui-bg-green">
                        <i class="layui-icon layui-icon-ok"></i>运行中</span>
                    {{#  } else { }}
                    <span class="layui-badge layui-bg-orange">
                        <a href="javascript:void(0)" class="errorMsg" tag="{{d.errorMsg}}">
                        <i class="layui-icon layui-icon-close-fill"></i>有异常
                        </a>
                    </span>
                    {{#  } }}

                    {{#  } else { }}
                    <span class="layui-badge">
                        <i class="layui-icon layui-icon-logout"></i>
                        已暂停</span>
                    {{#  } }}
                </script>

                <script type="text/html" id="grid-toolbar">
                    <div class="layui-btn-container">
                        <@insert>
                            <button class="layui-btn layui-btn-sm layuiadmin-btn-admin" lay-event="dispatch">
                                <i class="layui-icon layui-icon-transfer"></i>&nbsp;分派主题
                            </button>
                        </@insert>

                        <@select>
                            <button class="layui-btn layui-btn-sm layuiadmin-btn-admin" lay-event="refresh">
                                <i class="layui-icon layui-icon-refresh"></i>&nbsp;刷新列表
                            </button>
                        </@select>
                    </div>
                </script>

                <script type="text/html" id="grid-bar">
                    <@update>
                        {{#  if(d.isActive){ }}
                        <a class="layui-btn layui-btn-xs" lay-event="startOrStop">
                            <i class="layui-icon layui-icon-logout"></i>暂停</a>
                        {{#  } else { }}
                        <a class="layui-btn layui-btn-normal layui-btn-xs" lay-event="startOrStop">
                            <i class="layui-icon layui-icon-ok"></i>开始</a>
                        {{#  } }}

                        <a class="layui-btn layui-btn-xs" lay-event="resetOffset"><i
                                    class="layui-icon layui-icon-slider"></i>重置</a>

                        {{#  if(d.isActive){ }}

                        {{#  if(d.isOptimize){ }}
                        <a class="layui-btn layui-btn-xs layui-btn-normal" lay-event="restore">
                            <i class="layui-icon layui-icon-read"></i>还原</a>
                        {{#  } else { }}
                        <a class="layui-btn layui-btn-xs" lay-event="optimize">
                            <i class="layui-icon layui-icon-top"></i>加速</a>
                        {{#  } }}

                        {{#  } else { }}
                        <a class="layui-btn layui-btn-xs layui-btn-danger" lay-event="uninstallTopic"><i
                                    class="layui-icon layui-icon-delete"></i>卸载</a>
                        {{#  } }}
                    </@update>
                </script>
            </div>
        </div>
    </div>

    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table'], function () {
            const admin = layui.admin, $ = layui.$, form = layui.form, table = layui.table;
            tableErrorHandler();
            form.on('submit(search)', function (data) {
                const field = data.field;
                table.reload('grid', {where: field});
            });

            table.render({
                elem: '#grid',
                url: 'list',
                toolbar: '#grid-toolbar',
                method: 'post',
                cellMinWidth: 80,
                page: false,
                even: true,
                text: {
                    none: '暂无相关数据'
                },
                totalRow: true,
                cols: [[
                    {field: 'host', title: 'DB2ES服务地址', templet: '#colHost', width: 180},
                    {field: 'db2esId', title: 'DB2ES ID', width: 100},
                    {field: 'version', title: 'DB2ES 版本', width: 110},
                    {field: 'num', title: '序号', width: 60, totalRowText: '<b><i>合计</i></b>'},
                    {field: 'topicName', title: '主题(ES索引)名称', templet: '#colTopicName', totalRow: true, width: 350},
                    {field: 'size', title: '消息总数', width: 120, totalRow: true},
                    {
                        field: 'offset',
                        title: '已消费数(<i style="cursor: pointer" title="近实时"><small>NRT</small></i>)',
                        width: 120,
                        totalRow: true
                    },
                    {
                        field: 'lag',
                        title: '堆积数量(<i style="cursor: pointer" title="近实时"><small>NRT</small></i>)',
                        width: 120,
                        totalRow: true
                    },
                    {
                        field: 'docsCount',
                        title: 'ES记录数(<i style="cursor: pointer" title="近实时"><small>NRT</small></i>)',
                        width: 125,
                        totalRow: true
                    },
                    {field: 'tps', title: 'TPS', width: 70, totalRow: true},
                    {field: 'isActive', title: '状态', templet: '#colIsActive', width: 100},
                    {
                        field: 'offsetDateTime',
                        title: '当前ACK时间(<i style="cursor: pointer" title="近实时"><small>NRT</small></i>)',
                        width: 160
                    },
                    {title: '操作', toolbar: '#grid-bar', width: 220}
                ]],
                done: function (r) {
                    if (!r.success) {
                        return;
                    }
                    let run = 0, notRun = 0, rows = r.data.length;
                    $.each(r.data, function (index, data) {
                        if (data.isActive && data.errorMsg == '') {
                            run++;
                        } else {
                            notRun++;
                        }
                    });
                    if (notRun > 0) {
                        notRun = "<span class=\"layui-badge layui-bg-orange\">" + notRun + "</span>";
                    } else {
                        notRun = "<span class=\"layui-badge layui-bg-green\">" + notRun + "</span>";
                    }
                    $(".layui-table-total .layui-table-cell.laytable-cell-1-0-4").html("<i><span class=\"layui-badge layui-bg-blue\">" + rows + "</span>个主题,<span class=\"layui-badge layui-bg-green\">" + run + "</span>个在运行," + notRun + "个处于暂停异常状态</i>");
                    const size = $(".layui-table-total .layui-table-cell.laytable-cell-1-0-5");
                    const offset = $(".layui-table-total .layui-table-cell.laytable-cell-1-0-6");
                    const lag = $(".layui-table-total .layui-table-cell.laytable-cell-1-0-7");
                    const doc = $(".layui-table-total .layui-table-cell.laytable-cell-1-0-8");
                    const tps = $(".layui-table-total .layui-table-cell.laytable-cell-1-0-9");

                    size.html("<span class=\"layui-badge layui-bg-blue\"><i>" + Math.round(size.html()) + "</i></span>");
                    offset.html("<span class=\"layui-badge layui-bg-blue\"><i>" + Math.round(offset.html()) + "</i></span>");
                    lag.html("<span class=\"layui-badge layui-bg-blue\"><i>" + Math.round(lag.html()) + "</i></span>");
                    doc.html("<span class=\"layui-badge layui-bg-blue\"><i>" + Math.round(doc.html()) + "</i></span>");
                    tps.html("<span class=\"layui-badge layui-bg-blue\"><i>" + Math.round(tps.html()) + "</i></span>");
                    layuiRowspan('host', 1);

                    $("a[class='topicName layui-table-link']").click(function () {
                        showDetail($(this).attr("tag"));
                    });

                    $("a[class='host layui-table-link']").click(function () {
                        showSetting($(this).text(), $(this).attr("tag"));
                    });

                    $("a[class=errorMsg]").click(function () {
                        admin.error("异常信息", $(this).attr("tag"));
                    });
                }
            });

            function showDetail(topicName) {
                layer.open({
                    type: 2,
                    title: '索引名称 <b>' + topicName + '</b>',
                    shadeClose: true,
                    shade: 0.8,
                    area: ['95%', '95%'],
                    content: 'todetail?indexName=' + topicName
                });
            }

            function showSetting(ip, topicName) {
                layer.open({
                    type: 2,
                    title: '主机地址 <b>' + ip + '</b>',
                    shadeClose: true,
                    shade: 0.8,
                    area: ['90%', '90%'],
                    content: 'tosetting?topicName=' + topicName
                });
            }

            table.on('toolbar(grid)', function (obj) {
                if (obj.event === 'dispatch') {
                    layer.open({
                        type: 2,
                        title: '<i class="layui-icon layui-icon-transfer"></i>&nbsp;分派主题',
                        content: 'toadd',
                        area: ['880px', '650px'],
                        btn: admin.BUTTONS,
                        resize: false,
                        yes: function (index, layero) {
                            const iframeWindow = window['layui-layer-iframe' + index], submitID = 'btn_confirm',
                                submit = layero.find('iframe').contents().find('#' + submitID);
                            iframeWindow.layui.form.on('submit(' + submitID + ')', function (data) {
                                const field = data.field;
                                admin.post('installTopic', field, function () {
                                    table.reload('grid');
                                    layer.close(index);
                                }, function (result) {
                                    admin.error(admin.OPT_FAILURE, result.error);
                                    layer.close(index);
                                });
                            });
                            submit.trigger('click');
                        }
                    });
                } else if (obj.event === 'refresh') {
                    $("#btnSearch").click();
                }
            });

            table.on('tool(grid)', function (obj) {
                const row = obj.data;
                if (obj.event === 'startOrStop') {
                    let question;
                    if (row.isActive) {
                        question = "确定要暂停主题[" + row.topicName + "]的运行吗?";
                    } else {
                        question = "确定要开启主题[" + row.topicName + "]吗?";
                    }

                    layer.confirm(question, function (index) {
                        if (row.isActive) {
                            admin.post("stop", row, function () {
                                table.reload('grid');
                                layer.close(index);
                            });
                        } else {
                            admin.post("start", row, function () {
                                table.reload('grid');
                                layer.close(index);
                            });
                        }
                    });
                } else if (obj.event === 'resetOffset') {
                    layer.open({
                        type: 2,
                        title: '<i class="layui-icon layui-icon-slider" style="color: #1E9FFF;"></i>&nbsp;重置位点, 主题: ' + row.topicName,
                        content: 'toreset?topicName=' + row.topicName + '&offset=' + row.offset + '&timestamp=' + row.offsetDateTime,
                        area: ['880px', '500px'],
                        btn: admin.BUTTONS,
                        resize: false,
                        yes: function (index, layero) {
                            const iframeWindow = window['layui-layer-iframe' + index], submitID = 'btn_confirm',
                                submit = layero.find('iframe').contents().find('#' + submitID);
                            iframeWindow.layui.form.on('submit(' + submitID + ')', function (data) {
                                const field = data.field;
                                if (field.timestamp !== '' || field.offset !== '') {
                                    field.topicName = row.topicName;
                                    admin.post('restart', field, function () {
                                        table.reload('grid');
                                        layer.close(index);
                                    }, function (result) {
                                        admin.error(admin.OPT_FAILURE, result.error);
                                    });
                                } else {
                                    admin.error("系统错误", "[位点时间]和[偏移量]至少需要填写一个")
                                }
                            });
                            submit.trigger('click');
                        }
                    });
                } else if (obj.event === 'uninstallTopic') {
                    if (row.isActive) {
                        admin.error("操作错误", "请先暂停要卸载的主题");
                        return;
                    }

                    layer.confirm("确定要在[" + row.host + "]机器上卸载主题[" + row.topicName + "]吗?", function (index) {
                        admin.post("uninstallTopic", row, function () {
                            table.reload('grid');
                            layer.close(index);
                        });
                    });
                } else if (obj.event === 'optimize') {

                    layer.confirm("提高速度会降低ElasticSearch的实时性, 确定要提高索引[" + row.topicName + "]的消费速度吗?", function (index) {
                        admin.post("optimize", row, function () {
                            table.reload('grid');
                            layer.close(index);
                        });
                    });

                } else if (obj.event === 'restore') {
                    layer.confirm("还原提速会恢复ElasticSearch的实时性, 确定要停止索引[" + row.topicName + "]的提速效果吗?", function (index) {
                        admin.post("restore", row, function () {
                            table.reload('grid');
                            layer.close(index);
                        });
                    });
                }
            });
        });
    </script>
    </body>
    </html>
</@compress>