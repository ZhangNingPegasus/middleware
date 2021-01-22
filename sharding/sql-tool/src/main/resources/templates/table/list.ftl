<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <#include "../common/layui.ftl">
    <script src="${ctx}/js/jquery-3.4.1.min.js"></script>
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

<div class="layui-fluid">
    <div class="layui-card">
        <div class="layui-form layui-card-header layuiadmin-card-header-auto">
            <div class="layui-form-item">

                <div class="layui-inline">逻辑表:</div>
                <div class="layui-inline" style="width: 300px">
                    <select name="name" lay-filter="name" lay-search>
                        <option value="">请选择要修改的逻辑表名称</option>
                        <#list tables as table>
                            <option dimension="${table.dimension}" datasource="${table.datasource}"
                                    table="${table.table}" value="${table.table}">${table.table}</option>
                        </#list>
                    </select>
                </div>

                <div class="layui-inline">新表名称:</div>
                <div class="layui-inline">
                    <input type="text" id="tableName" name="tableName" lay-verify="required" class="layui-input"
                           placeholder="请填写逻辑表的名称" autocomplete="off">
                </div>

                <div class="layui-inline">数据库总个数:</div>
                <div class="layui-inline">
                    <input type="number" id="dbCount" name="dbCount" lay-verify="required" class="layui-input"
                           placeholder="请填写表名" value="2" autocomplete="off">
                </div>

                <div class="layui-inline">每个库的物理表个数:</div>
                <div class="layui-inline">
                    <input type="number" id="tableCount" name="tableCount" lay-verify="required"
                           class="layui-input"
                           placeholder="请填写表名" value="32" autocomplete="off">
                </div>

                <div class="layui-inline">
                    <button id="btnCreate" class="layui-btn layuiadmin-btn-admin">
                        <i class="layui-icon layui-icon-form layuiadmin-button-btn"></i>&nbsp;生&nbsp;成&nbsp;建&nbsp;表&nbsp;语&nbsp;句&nbsp;
                    </button>

                    <button id="btnExport" class="layui-btn layuiadmin-btn-admin">
                        <i class="layui-icon layui-icon-export layuiadmin-button-btn"></i>&nbsp;导&nbsp;出&nbsp;建&nbsp;表&nbsp;语&nbsp;句&nbsp;
                    </button>
                </div>
            </div>
        </div>

        <div class="layui-card-body">
            <table class="layui-hide" id="grid" lay-filter="grid"></table>
            <script type="text/html" id="grid-toolbar">
                <div class="layui-btn-container">
                    <button class="layui-btn layui-btn-sm layuiadmin-btn-admin" lay-event="add">
                        <i class="layui-icon layui-icon-search layui-icon-edit"></i>添加
                    </button>
                    <button class="layui-btn layui-btn-sm" lay-event="insert">
                        <i class="layui-icon layui-icon-search layui-icon-shrink-right"></i>插入
                    </button>
                    <button class="layui-btn layui-btn-sm" lay-event="up">
                        <i class="layui-icon layui-icon-search layui-icon-up"></i>上移
                    </button>
                    <button class="layui-btn layui-btn-sm" lay-event="down">
                        <i class="layui-icon layui-icon-search layui-icon-down"></i>下移
                    </button>
                    <button class="layui-btn layui-btn-sm" lay-event="del">
                        <i class="layui-icon layui-icon-search layui-icon-delete"></i>删除
                    </button>
                    <button class="layui-btn layui-btn-sm" lay-event="index">
                        <i class="layui-icon layui-icon-search layui-icon-list"></i>索引
                    </button>
                </div>
            </script>

            <script type="text/html" id="tType">
                <select name='type' lay-filter='type' lay-search>
                    <option value=""></option>
                    <#list types as type>
                        <option value="${type}" {{ d.type=='${type}'
                                ?'selected="selected"' : '' }}>${type}</option>
                    </#list>
                </select>
            </script>

            <script type="text/html" id="tNotNull">
                <input type="checkbox" style="cursor: pointer" lay-skin="primary" lay-filter='notNull'
                       {{ d.notNull ? 'checked' : '' }}>
            </script>

            <script type="text/html" id="tIsPrimary">
                <input type="checkbox" style="cursor: pointer" lay-skin="primary" lay-filter='isPrimary'
                       {{ d.isPrimary ? 'checked' : '' }}>
            </script>

            <script type="text/html" id="tUnsigned">
                <input type="checkbox" style="cursor: pointer" lay-skin="primary" lay-filter='unsigned'
                       {{ d.unsigned ? 'checked' : '' }}>
            </script>

            <script type="text/html" id="tAutoIncrement">
                <input type="checkbox" style="cursor: pointer" lay-skin="primary" lay-filter='autoIncrement'
                       {{ d.autoIncrement ? 'checked' : '' }}>
            </script>

            <script type="text/html" id="tAutoUpdateByTimestampt">
                <input type="checkbox" style="cursor: pointer" lay-skin="primary"
                       lay-filter='autoUpdateByTimestampt'
                       {{ d.autoUpdateByTimestampt ? 'checked' : '' }}>
            </script>

        </div>

    </div>
</div>
<form action="" id="fileForm" method="post" style="display: none;"></form>
<script type="text/javascript">
    layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'table'], function () {
        const $ = layui.$, admin = layui.admin, form = layui.form, table = layui.table;
        tableErrorHandler();
        let inserts = [], updates = [], deletes = [], orgiData = null;
        let count = 0, indexData = null, isNew = true;

        function newRow() {
            count++;
            return {
                'index': count,
                'id': count,
                'name': '',
                'type': '',
                'length': '',
                'decimal': '',
                'notNull': false,
                'isPrimary': false,
                'autoUpdateByTimestampt': false,
                'unsigned': false,
                'autoIncrement': false,
                'defaultValue': '',
                'comment': '',
                'position': ''
            };
        }

        function swap(array, i, j) {
            const temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }

        function isInt(value) {
            return /^\d+$/.test(value);
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
            $.each(data, function (i, d) {
                d.index = i;
            });
            table.reload('grid', {'data': data});
        }

        function pushInserts(row) {
            let existed = false;
            $.each(inserts, function (i, d) {
                if (d.id === row.id) {
                    inserts[i] = row;
                    existed = true;
                    return true;
                }
            });
            if (!existed) {
                inserts.push(row);
            }
        }

        function pushUpdates(row) {
            let existed = false;
            $.each(updates, function (i, d) {
                if (d.id === row.id) {
                    updates[i] = row;
                    existed = true;
                    return true;
                }
            });
            if (!existed) {
                updates.push(row);
            }
        }

        function pushDeletes(row) {
            let existed = false;
            $.each(deletes, function (i, d) {
                if (d.id === row.id) {
                    deletes[i] = row;
                    existed = true;
                    return true;
                }
            });
            if (!existed) {
                deletes.push(row);
            }
        }

        function getData() {
            const result = {};
            const data = table.cache['grid'];
            let hasError = false;
            const names = [];

            const tableName = $.trim($('#tableName').val());
            const dbCount = $.trim($('#dbCount').val());
            const tableCount = $.trim($('#tableCount').val());

            $.each(data, function (index, item) {
                if (isNew && tableName === '') {
                    admin.error("系统提示", "逻辑表名称不允许为空");
                    hasError = true;
                    $('#tableName').focus();
                    return true;
                } else if (dbCount === '') {
                    admin.error("系统提示", "数据库总个数不允许为空");
                    hasError = true;
                    $('#dbCount').focus();
                    return true;
                } else if (tableCount === '') {
                    admin.error("系统提示", "每个库的物理表个数不允许为空");
                    hasError = true;
                    $('#tableCount').focus();
                    return true;
                } else if ($.trim(item.name) === '') {
                    admin.error("系统提示", "字段名称不能允许为空");
                    hasError = true;
                    return true;
                } else if ($.trim(item.type) === '') {
                    admin.error("系统提示", "字段类型不允许为空");
                    hasError = true;
                    return true;
                } else if (item.length !== '' && !isInt(item.length)) {
                    admin.error("系统提示", "字段长度必须是整数");
                    hasError = true;
                    return true;
                } else if (item.decimal !== '' && !isInt(item.decimal)) {
                    admin.error("系统提示", "小数点必须是整数");
                    hasError = true;
                    return true;
                } else if (!isVarName(item.name)) {
                    admin.error("系统提示", "字段名称不符合命名规则");
                    hasError = true;
                    return true;
                } else if (names.indexOf(item.name) > -1) {
                    admin.error("系统提示", "字段名称[" + item.name + "]已存在");
                    hasError = true;
                    return true;
                }
                names.push(item.name);

                if (item.isPrimary) {
                    item.notNull = true;
                }
            });

            if (hasError) {
                return null;
            }

            reload(data);

            result.tableName = tableName;
            result.dbCount = dbCount;
            result.tableCount = tableCount;
            result.data = JSON.stringify(data);
            result.indexData = JSON.stringify(indexData);
            return result;
        }

        table.render({
            elem: '#grid',
            toolbar: '#grid-toolbar',
            cellMinWidth: 80,
            page: false,
            limit: 99999999,
            limits: [99999999],
            even: false,
            loading: false,
            cols: [[
                {type: 'checkbox', width: 50},
                {type: 'numbers', title: '序号', width: 50},
                {field: 'name', title: '字段名称', edit: 'text', width: 150},
                {field: 'type', title: '字段类型', unresize: true, templet: '#tType', width: 150},
                {field: 'size', title: '字段长度', edit: 'text', width: 120},
                {field: 'decimal', title: '小数点', edit: 'text', width: 120},
                {field: 'notNull', title: '不是null', unresize: true, templet: '#tNotNull', align: 'center', width: 80},
                {
                    field: 'isPrimary',
                    title: '是否主键',
                    unresize: true,
                    templet: '#tIsPrimary',
                    align: 'center',
                    width: 80
                },
                {field: 'unsigned', title: '无符号', unresize: true, templet: '#tUnsigned', align: 'center', width: 80},
                {
                    field: 'autoIncrement',
                    title: '自动递增',
                    unresize: true,
                    templet: '#tAutoIncrement',
                    align: 'center',
                    width: 80
                },
                {
                    field: 'autoUpdateByTimestampt',
                    title: '自动更新时间戳',
                    unresize: true,
                    templet: '#tAutoUpdateByTimestampt',
                    align: 'center',
                    width: 120
                },
                {field: 'defaultValue', title: '默认值', unresize: true, edit: 'text', width: 200},
                {field: 'comment', title: '注释', edit: 'text'},
            ]],
            data: [newRow()]
        });

        form.on('select(type)', function (obj) {
            const dataIndex = $(obj.elem).parent().parent().parent().attr('data-index');
            const data = table.cache['grid'];
            data[dataIndex]['type'] = obj.value;
        });

        form.on('checkbox(notNull)', function (obj) {
            const dataIndex = $(obj.elem).parent().parent().parent().attr('data-index');
            const data = table.cache['grid'];
            const strClass = $(obj.elem).next().attr("class");
            data[dataIndex]['notNull'] = strClass.indexOf('layui-form-checked') >= 0;
        });

        form.on('checkbox(isPrimary)', function (obj) {
            const dataIndex = $(obj.elem).parent().parent().parent().attr('data-index');
            const data = table.cache['grid'];
            const strClass = $(obj.elem).next().attr("class");
            data[dataIndex]['isPrimary'] = strClass.indexOf('layui-form-checked') >= 0;
        });

        form.on('checkbox(autoUpdateByTimestampt)', function (obj) {
            const dataIndex = $(obj.elem).parent().parent().parent().attr('data-index');
            const data = table.cache['grid'];
            const strClass = $(obj.elem).next().attr("class");
            data[dataIndex]['autoUpdateByTimestampt'] = strClass.indexOf('layui-form-checked') >= 0;
        });

        form.on('checkbox(unsigned)', function (obj) {
            const dataIndex = $(obj.elem).parent().parent().parent().attr('data-index');
            const data = table.cache['grid'];
            const strClass = $(obj.elem).next().attr("class");
            data[dataIndex]['unsigned'] = strClass.indexOf('layui-form-checked') >= 0;
        });

        form.on('checkbox(autoIncrement)', function (obj) {
            const dataIndex = $(obj.elem).parent().parent().parent().attr('data-index');
            const data = table.cache['grid'];
            const strClass = $(obj.elem).next().attr("class");
            data[dataIndex]['autoIncrement'] = strClass.indexOf('layui-form-checked') >= 0;
        });

        form.on('select(name)', function (obj) {
            inserts = [];
            updates = [];
            deletes = [];
            if (obj.value === '') {
                count = 0;
                indexData = null;
                orgiData = null;
                isNew = true;

                reload([newRow()]);
                return;
            }
            const option = $("select[name='name']").find("option[value='" + obj.value + "']");
            admin.post("getTableDetail",
                {
                    'dimension': option.attr("dimension"),
                    'datasource': option.attr("datasource"),
                    'table': option.attr("table")
                },
                function (result) {
                    $.each(result.data.field, function (i, d) {
                        d.id = i + 1;
                    });
                    orgiData = cloneJson(result.data.field);
                    reload(result.data.field);
                    indexData = result.data.index;
                    count = result.data.field.length;
                    $("#dbCount ").val(result.data.databaseCount);
                    $("#tableCount").val(result.data.tableCount);
                    isNew = false;
                });
        });

        table.on('edit(grid)', function (obj) {
            if (!isNew) {
                pushUpdates(obj.data);
            }
        });

        table.on('toolbar(grid)', function (obj) {
            const data = table.cache['grid'];
            const checkData = table.checkStatus(obj.config.id).data;
            switch (obj.event) {
                case 'del':
                    const deleted = [];
                    $.each(checkData, function (index, item) {
                        deleted.push(item.id);
                    });
                    $.each(deleted, function (index, item) {
                        for (let i = 0; i < data.length; i++) {
                            if (data[i].id === item) {
                                data.remove(data[i]);
                                return;
                            }
                        }
                    });
                    break;
                case 'add':
                    data.push(newRow());
                    break;
                case 'insert':
                    if (checkData.length > 0) {
                        for (let i = 0; i < data.length; i++) {
                            if (data[i].id === checkData[0].id) {
                                data.splice(i, 0, newRow());
                                break;
                            }
                        }
                    }
                    break;
                case 'up':
                    if (checkData.length > 0) {
                        for (let i = 0; i < data.length; i++) {
                            if (data[i].id === checkData[0].id) {
                                const j = i - 1;
                                if (j >= 0) {
                                    if (i - 1 > 0) {
                                        checkData[0].position = "AFTER " + data[i - 1].name;
                                    } else {
                                        checkData[0].position = "FIRST";
                                    }
                                    swap(data, i, j);
                                    pushUpdates(checkData[0]);
                                }
                                break;
                            }
                        }
                    }
                    break;
                case 'down':
                    if (checkData.length > 0) {
                        for (let i = 0; i < data.length; i++) {
                            if (data[i].id === checkData[0].id) {
                                const j = i + 1;
                                if (j < data.length) {
                                    checkData[0].position = "AFTER " + data[j].name;
                                    swap(data, i, j);
                                    pushUpdates(checkData[0]);
                                }
                                break;
                            }
                        }
                    }
                    break;
                case 'index':
                    const names = [];
                    $.each(data, function (i, d) {
                        names.push(d.name);
                    });

                    admin.post("session1", {
                        'names': names.join(','),
                        'data': JSON.stringify(indexData)
                    }, function () {
                        layer.open({
                            type: 2,
                            title: '<i class="layui-icon layui-icon-list" style="color: #1E9FFF;"></i>&nbsp;索引',
                            content: 'toIndex',
                            area: ['1200px', '768px'],
                            btn: ['确定'],
                            resize: false,
                            yes: function (index, layero) {
                                const iframeWindow = window['layui-layer-iframe' + index], submitID = 'btn_confirm',
                                    submit = layero.find('iframe').contents().find('#' + submitID);
                                iframeWindow.layui.form.on('submit(' + submitID + ')', function () {
                                    indexData = iframeWindow.layui.table.cache['grid'];
                                    layer.close(index);
                                });
                                submit.trigger('click');
                            }
                        });
                    });
                    break;
            }
            reload(data);
        });

        $("#btnCreate").click(function () {
            const buffer = getData();
            if (buffer == null) {
                return true;
            }
            if (isNew) {
                admin.post("session", {
                    'tableName': buffer.tableName,
                    'dbCount': buffer.dbCount,
                    'tableCount': buffer.tableCount,
                    'data': buffer.data,
                    'index': buffer.indexData
                }, function () {
                    layer.open({
                        type: 2,
                        title: '<i class="layui-icon layui-icon-form" style="color: #1E9FFF;"></i>&nbsp;建表语句',
                        content: 'toresullt',
                        area: ['1200px', '780px'],
                        btn: ['确定'],
                        resize: false
                    });
                });
            }
        });

        $("#btnExport").click(function () {
            const buffer = getData();
            if (buffer == null) {
                return true;
            }
            if (isNew) {
                admin.post("session", {
                    'tableName': buffer.tableName,
                    'dbCount': buffer.dbCount,
                    'tableCount': buffer.tableCount,
                    'data': buffer.data,
                    'index': buffer.indexData
                }, function () {
                    $("#fileForm").attr('action', 'download').submit();
                });
            }
        });
    });
</script>
</body>
</html>