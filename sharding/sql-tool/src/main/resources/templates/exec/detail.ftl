<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
        <script src="${ctx}/js/jquery-3.4.1.min.js"></script>
        <script src="${ctx}/js/jquery.json-editor.min.js"></script>

        <style type="text/css">
            .key {
                color: #009688;
                font-family: "Times New Roman";
                font-weight: bold;
                font-style: italic
            }
        </style>
    </head>
    <body>

    <div class="layui-fluid">
        <div class="layui-tab layui-tab-card">
            <ul class="layui-tab-title">
                <li class="layui-this"><i class="layui-icon layui-icon-cols"></i>&nbsp;&nbsp;字段</li>
                <li><i class="layui-icon layui-icon-share"></i>&nbsp;&nbsp;索引</li>
                <li><i class="layui-icon layui-icon-vercode"></i>&nbsp;&nbsp;检查结构一致性</li>
                <li><i class="layui-icon layui-icon-search"></i>&nbsp;&nbsp;分库分表算法</li>
                <li><i class="layui-icon layui-icon-list"></i>&nbsp;&nbsp;物理表细节</li>
            </ul>
            <div class="layui-tab-content">
                <div class="layui-tab-item layui-show">
                    <table id="grid" lay-filter="grid"></table>
                    <script type="text/html" id="colKey">
                        {{#  if(d.key == 'PRI'){ }}
                        <i class="layui-icon layui-icon-key" style="color: #B8860B;"></i>
                        {{#  } else { }}
                        <span></span>
                        {{#  } }}
                    </script>

                    <script type="text/html" id="colNotNull">
                        {{#  if(d.notNull){ }}
                        <input type="checkbox" style="cursor: pointer" disabled="disabled" lay-skin="primary">
                        {{#  } else { }}
                        <input type="checkbox" style="cursor: pointer" disabled="disabled" lay-skin="primary"
                               checked="checked">
                        {{#  } }}
                    </script>
                </div>

                <div class="layui-tab-item">
                    <table id="gridIndex" lay-filter="gridIndex"></table>
                </div>

                <div class="layui-tab-item">
                    <div class="layui-form layui-row" lay-filter="layuiadmin-form-admin" id="layuiadmin-form-admin">
                        <div class="layui-col-md12">
                            <button id="btnCheck" class="layui-btn">&nbsp;&nbsp;检&nbsp;&nbsp;查&nbsp;&nbsp;
                            </button>
                        </div>
                        <div class="layui-col-md6">
                            <pre id="diffTables" class="layui-code"></pre>
                        </div>

                        <div class="layui-col-md6">
                            <pre id="diffIndexs" class="layui-code"></pre>
                        </div>
                    </div>
                </div>

                <div class="layui-tab-item">
                    <div class="layui-form layui-row" lay-filter="layuiadmin-form-admin" id="layuiadmin-form-admin"
                         style="padding: 20px 30px 0 0;">
                        <div class="layui-col-md7">

                            <div class="layui-form-item">
                                <label class="layui-form-label">字段值</label>
                                <div class="layui-input-inline" style="width:700px">
                                    <input type="text" id="value" lay-verify="required" placeholder="请填写要验证的值"
                                           autocomplete="off" class="layui-input"/>
                                    <br/>
                                    <button id="btnAlgorithm" class="layui-btn layui-btn-normal">&nbsp;&nbsp;计&nbsp;&nbsp;算&nbsp;&nbsp;
                                    </button>
                                </div>
                            </div>

                            <br/><br/><br/>
                            <hr class="layui-bg-blue" style="width: 95%;margin-left: 20px;">
                            <br/><br/><br/>

                            <div class="layui-form-item">
                                <label class="layui-form-label">HASH值</label>
                                <div class="layui-input-inline" style="width:700px">
                                    <input type="text" id="hash" readonly="readonly" autocomplete="off"
                                           class="layui-input"/>
                                </div>
                            </div>

                            <div class="layui-form-item">
                                <label class="layui-form-label">数据库</label>
                                <div class="layui-input-inline" style="width:700px">
                                    <input type="text" id="database" readonly="readonly" autocomplete="off"
                                           class="layui-input"/>
                                </div>
                            </div>

                            <div class="layui-form-item">
                                <label class="layui-form-label">数据表</label>
                                <div class="layui-input-inline" style="width:700px">
                                    <input type="text" id="table" readonly="readonly" autocomplete="off"
                                           class="layui-input"/>
                                </div>
                            </div>

                        </div>

                        <div class="layui-col-md5">
                            <div class="layui-form-item">
                                <fieldset class="layui-elem-field">
                                    <legend>分库分表算法简介</legend>
                                    <div class="layui-field-box">
                                        <span style="color:#1E9FFF!important">假设</span> :
                                        <p/>
                                        &nbsp;&nbsp;&nbsp;&nbsp;该表所在的数据库库总个数为<span class="key">M</span>,
                                        该表的总个数为<span class="key">N</span>, 拆分键的值为<span class="key">X</span>
                                        <p/>
                                        <br/>
                                        <span style="color:#1E9FFF!important">则有</span> :
                                        <p/>
                                        &nbsp;&nbsp;&nbsp;&nbsp;1. 每个库的表数量是 <span class="key">P = N / M</span>
                                        <p/>
                                        &nbsp;&nbsp;&nbsp;&nbsp;2. 对拆分键值进行Hash后的值是 <span
                                                class="key">H = murmur3_128(X)</span>
                                        <p/>
                                        <br/>
                                        <span style="color:#1E9FFF!important">那么</span> :
                                        <p/>
                                        &nbsp;&nbsp;&nbsp;&nbsp;1. 分库算法 : <span class="key">(H / P) % M</span>
                                        <p/>
                                        &nbsp;&nbsp;&nbsp;&nbsp;2. 分表算法 : <span class="key">(H % N)</span>
                                        <p/>
                                        <br/>
                                        <span style="color:#1E9FFF!important">解释</span> :
                                        <blockquote class="layui-elem-quote layui-quote-nm">
                                            &nbsp;&nbsp;&nbsp;&nbsp;murmur3算法诞生于2018年, 对于murmur2算法而言, murmur3速度更快,
                                            尤其对大块的数据,
                                            具有较高的平衡性与低碰撞率。
                                            <br/>
                                            &nbsp;&nbsp;&nbsp;&nbsp;murmur3算法实现了32位和128位HashKey, 由于128位算法针对各自的平台进行了优化,
                                            所以当使用128位时,
                                            x86和x64版本将产生不同的值。
                                        </blockquote>
                                    </div>
                                </fieldset>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="layui-tab-item">
                    <pre id="factTableDetails" class="layui-code">${tableList}</pre>
                </div>

            </div>
        </div>
    </div>

    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table', 'carousel', 'echarts'], function () {
            const admin = layui.admin, table = layui.table, $ = layui.$;

            tableErrorHandler();

            layui.use('code', function () {
                layui.code({
                    elem: '#diffTables',
                    title: '<i class="layui-icon layui-icon-table"></i>&nbsp;&nbsp;表结构检查结果',
                    about: false
                });
            });

            layui.use('code', function () {
                layui.code({
                    elem: '#diffIndexs',
                    title: '<i class="layui-icon layui-icon-link"></i>&nbsp;&nbsp;表索引检查结果',
                    about: false
                });
            });

            layui.use('code', function () {
                layui.code({
                    elem: '#factTableDetails',
                    title: '表清单',
                    about: false
                });
            });

            $("#btnCheck").click(function () {
                admin.post("check", {'table': '${table}'}, function (data) {
                    $("#diffTables li").html(data.data[0]).show();
                    $("#diffIndexs li").html(data.data[1]).show();
                });
            });

            table.render({
                elem: '#grid',
                cellMinWidth: 80,
                page: false,
                limit: 99999999,
                limits: [99999999],
                even: true,
                height: '600',
                text: {none: '暂无相关字段信息'},
                cols: [[
                    {type: 'numbers', title: '序号', width: 50},
                    {field: "name", title: '字段名', width: 200},
                    {field: "typeDesc", title: '类型', width: 200},
                    {field: "defaultValue", title: '默认值', width: 200},
                    {field: "notNull", title: '不是 null', templet: '#colNotNull', align: 'center', width: 100},
                    {field: "key", title: '键', templet: '#colKey', align: 'center', width: 50},
                    {field: "comment", title: '注释'}
                ]],
                data: ${fields}
            });

            table.render({
                elem: '#gridIndex',
                cellMinWidth: 80,
                page: false,
                limit: 99999999,
                limits: [99999999],
                even: true,
                height: '600',
                text: {none: '暂无相关索引信息'},
                cols: [[
                    {type: 'numbers', title: '序号', width: 50},
                    {field: "indexName", title: '索引名', width: 200},
                    {field: "fieldName", title: '字段', width: 300},
                    {field: "type", title: '索引类型', width: 200},
                    {field: "method", title: '索引方法', width: 200},
                    {field: "comment", title: '注释'}
                ]],
                data: ${indexs}
            });

            $("#btnAlgorithm").click(function () {
                const value = $.trim($("#value").val());
                if (value === "") {
                    admin.error("系统提示", "字段值不允许为空", function () {
                        $("#value").focus();
                    });
                    return;
                }
                admin.post('doalgorithm', {
                    'value': value,
                    'dimension': ' ${dimension}',
                    'datasource': '${datasource}',
                    'table': '${table}'
                }, function (res) {
                    $("#hash").val(res.data.hash);
                    $("#database").val(res.data.database);
                    $("#table").val(res.data.table);
                });
            });

        });
    </script>
    </body>
    </html>
</@compress>