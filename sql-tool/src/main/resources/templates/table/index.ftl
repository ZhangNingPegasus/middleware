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

    <div class="layui-form" lay-filter="layuiadmin-form-admin" id="layuiadmin-form-admin">

        <table class="layui-hide" id="grid" lay-filter="grid"></table>
        <script type="text/html" id="grid-toolbar">
            <div class="layui-btn-container">
                <button class="layui-btn layui-btn-sm layuiadmin-btn-admin" lay-event="add">
                    <i class="layui-icon layui-icon-search layui-icon-edit"></i>添加
                </button>
                <button class="layui-btn layui-btn-sm" lay-event="del">
                    <i class="layui-icon layui-icon-search layui-icon-delete"></i>删除
                </button>
            </div>
        </script>

        <script type="text/html" id="tFieldName">
            <div class="clsFieldName">
                {{ d.fieldName }}
            </div>
        </script>

        <script type="text/html" id="tType">
            <select name='indexType' lay-filter='indexType' lay-search>
                <option value="UNIQUE" {{ d.type=='UNIQUE' ?
                'selected="selected"' : '' }}>UNIQUE</option>
                <option value="NORMAL" {{ d.type=='NORMAL' ?
                'selected="selected"' : '' }}>NORMAL</option>
            </select>
        </script>

        <div class="layui-form-item layui-hide">
            <input type="button" lay-submit lay-filter="btn_confirm" id="btn_confirm" value="确认">
        </div>
    </div>

    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'form', 'table'], function () {
            const $ = layui.$, admin = layui.admin, form = layui.form, table = layui.table;
            tableErrorHandler();
            let count = 0;

            function newRow() {
                count++;
                return {
                    '__id': count,
                    'indexName': '',
                    'fieldName': '',
                    'type': 'NORMAL',
                    'comment': ''
                };
            }

            function isVarName(value) {
                try {
                    eval('let ' + value + '="test";');
                    return true;
                } catch (e) {
                    return false;
                }
            }

            function reload(data) {
                table.reload('grid', {'data': data});
            }

            let data = [];
            eval('data=${data}');

            table.render({
                elem: '#grid',
                toolbar: '#grid-toolbar',
                cellMinWidth: 80,
                page: false,
                limit: 9999999,
                limits: [9999999],
                even: false,
                loading: false,
                cols: [[
                    {type: 'checkbox', width: 50},
                    {type: 'numbers', title: '序号', width: 50},
                    {field: 'indexName', title: '索引名称', edit: 'text', width: 300},
                    {field: 'fieldName', title: '字段名称', unresize: true, templet: '#tFieldName', width: 300},
                    {field: 'type', title: '索引类型', templet: '#tType', width: 120},
                    {field: 'comment', title: '注释', edit: 'text'},
                ]],
                data: data,
                done: function () {
                    $("td[data-field='fieldName']").click(function () {
                        const data = table.cache['grid'];
                        const dataIndex = $(this).parent().attr('data-index');
                        const selFieldName = [];
                        $.each(data, function (i, d) {
                            if (parseInt(i) === parseInt(dataIndex)) {
                                selFieldName.push(d.fieldName);
                            }
                        });
                        layer.open({
                            type: 2,
                            title: '<i class="layui-icon layui-icon-cols" style="color: #1E9FFF;"></i>&nbsp;选择字段',
                            content: 'toFieldName?names=' + escape('${names}') + '&enames=' + escape(selFieldName.join(',')),
                            area: ['500px', '300px'],
                            btn: ['确定'],
                            resize: false,
                            yes: function (index, layero) {
                                const iframeWindow = window['layui-layer-iframe' + index], submitID = 'btn_confirm',
                                    submit = layero.find('iframe').contents().find('#' + submitID);
                                iframeWindow.layui.form.on('submit(' + submitID + ')', function () {
                                    const fieldNames = iframeWindow.layui.table.checkStatus('grid').data;
                                    const names = [];
                                    $.each(fieldNames, function (i, d) {
                                        names.push('`' + d.fieldName + '`');
                                    });
                                    data[dataIndex]['fieldName'] = names.join(',');
                                    reload(data);
                                    layer.close(index);
                                });
                                submit.trigger('click');
                            }
                        });
                    });
                }
            });

            form.on('select(indexType)', function (obj) {
                const dataIndex = $(obj.elem).parent().parent().parent().attr('data-index');
                const data = table.cache['grid'];
                data[dataIndex]['indexType'] = obj.value
            });

            table.on('toolbar(grid)', function (obj) {
                const data = table.cache['grid'];
                const checkData = table.checkStatus(obj.config.id).data;
                switch (obj.event) {
                    case 'del':
                        const deleted = [];
                        $.each(checkData, function (index, item) {
                            deleted.push(item.__id);
                        });
                        $.each(deleted, function (index, item) {
                            for (let i = 0; i < data.length; i++) {
                                if (data[i].__id === item) {
                                    data.remove(data[i]);
                                    break;
                                }
                            }
                        });
                        break;
                    case 'add':
                        data.push(newRow());
                        break;
                }
                reload(data);
            });

            $("#btn_confirm").click(function () {
                const data = table.cache['grid'];
                let hasError = false;
                const names = [];
                $.each(data, function (i, d) {
                    if (d.indexName === '') {
                        admin.error("系统提示", "索引名称不允许为空");
                        hasError = true;
                        return false;
                    } else if (d.fieldName === '') {
                        admin.error("系统提示", "字段名称不允许为空");
                        hasError = true;
                        return false;
                    } else if (!isVarName(d.indexName)) {
                        admin.error("系统提示", "索引名称不符合命名规则");
                        hasError = true;
                        return false;
                    } else if (names.indexOf(d.indexName) > -1) {
                        admin.error("系统提示", "索引名称[" + d.indexName + "]已存在");
                        hasError = true;
                        return false;
                    }
                    names.push(d.indexName);
                });

                return !hasError;
            });
        });
    </script>
    </body>
    </html>
</@compress>