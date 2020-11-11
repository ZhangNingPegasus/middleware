<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "../common/layui.ftl">
        <link rel="stylesheet" href="${ctx}/css/codemirror.css" media="all">
        <link rel="stylesheet" href="${ctx}/css/idea.css" media="all">
    </head>
    <body>

    <div class="layui-form" style="padding: 0px 30px 0 30px;">
        <div class="layui-tab layui-tab-brief" lay-filter="form">
            <ul class="layui-tab-title">
                <li class="layui-this">配置数据源</li>
                <li>配置新Mapping</li>
            </ul>
            <div class="layui-tab-content" style="height: 100px;">
                <div class="layui-tab-item layui-show">
                    <div class="layui-form-item" style="text-align: center;">
                        <div class="layui-inline">
                            <input type="radio" name="type" lay-filter="type" value="0" title="默认数据源(采用ACM配置)" checked>
                            <input type="radio" name="type" lay-filter="type" value="1" title="自定义数据源(用户自己设定)">
                            </select>
                        </div>
                    </div>
                    <#list datasources as datasource>
                        <fieldset id="template__${datasource_index}" class="template layui-elem-field">
                            <legend><small>数据源配置</small>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                <div class="layui-btn-group">
                                    <button type="button" class="btnAdd layui-btn layui-btn-sm" title="添加数据源"
                                            tag="__${datasource_index}" style="display: none">
                                        <i class="layui-icon">&#xe654;</i>
                                    </button>
                                    <button type="button" class="btnDelete layui-btn layui-btn-sm layui-btn-warm"
                                            title="删除数据源" tag="__${datasource_index}" style="display: none">
                                        <i class="layui-icon">&#xe640;</i>
                                    </button>
                                    <button type="button" class="btnTest layui-btn layui-btn-sm layui-btn-normal"
                                            title="检验数据源" tag="__${datasource_index}" style="display: none">
                                        <i class="layui-icon">&#xe605;</i>
                                    </button>
                                </div>
                            </legend>
                            <div class="layui-field-box">
                                <div class="layui-form-item">
                                    <label class="layui-form-label">主机:</label>
                                    <div class="layui-input-inline">
                                        <input type="text" name="host__${datasource_index}" lay-verify="required"
                                               class="ds layui-input" style="width: 1000px" autocomplete="off"
                                               value="${datasource.host}" readonly="readonly">
                                    </div>
                                </div>
                                <div class="layui-form-item">
                                    <label class="layui-form-label">端口:</label>
                                    <div class="layui-input-inline">
                                        <input type="number" name="port__${datasource_index}" lay-verify="required"
                                               class="ds layui-input" style="width: 1000px" autocomplete="off"
                                               value="${datasource.port}" readonly="readonly">
                                    </div>
                                </div>
                                <div class="layui-form-item">
                                    <label class="layui-form-label">用户名:</label>
                                    <div class="layui-input-inline">
                                        <input type="text" name="uid__${datasource_index}" lay-verify="required"
                                               class="ds layui-input"
                                               style="width: 1000px" autocomplete="off" value="已隐藏" readonly="readonly">
                                    </div>
                                </div>
                                <div class="layui-form-item">
                                    <label class="layui-form-label">密码:</label>
                                    <div class="layui-input-inline">
                                        <input type="password" name="pwd__${datasource_index}" lay-verify="required"
                                               class="ds layui-input" style="width: 1000px" autocomplete="off"
                                               value="已隐藏"
                                               readonly="readonly">
                                    </div>
                                </div>
                                <div class="layui-form-item">
                                    <label class="layui-form-label">数据库名:</label>
                                    <div class="layui-input-inline">
                                        <input type="text" name="databaseName__${datasource_index}"
                                               lay-verify="required"
                                               class="ds layui-input" style="width: 1000px" autocomplete="off"
                                               value="${datasource.databaseName}" readonly="readonly">
                                    </div>
                                </div>
                                <div class="layui-form-item">
                                    <label class="layui-form-label">数据表(多个逗号分隔):</label>
                                    <div class="layui-input-inline">
                            <textarea name="tableNames__${datasource_index}" class="ds layui-input"
                                      style="resize: none;height: 150px;width: 1000px"
                                      lay-verify="required" readonly="readonly">${datasource.tableNames}</textarea>
                                    </div>
                                </div>
                            </div>
                        </fieldset>
                    </#list>
                </div>
                <div class="layui-tab-item">
                    <div class="layui-card">
                        <div class="layui-form layui-card-header layuiadmin-card-header-auto">
                            <div class="layui-form-item">
                                <div class="layui-inline">主题名称:</div>
                                <div class="layui-inline" style="width: 320px">
                                    <input type="text" name="name" lay-verify="required" class="layui-input"
                                           placeholder="请填写主题名称" value="${topicName}" readonly="readonly">
                                </div>

                                <div class="layui-inline">主分片数:</div>
                                <div class="layui-inline" style="width: 140px">
                                    <input type="number" name="numberOfShards" lay-verify="required" class="layui-input"
                                           placeholder="请填写主分片数" autocomplete="off"
                                           value="${(topic.numberOfShards)!'5'}">
                                </div>

                                <div class="layui-inline">副本分片数:</div>
                                <div class="layui-inline" style="width: 140px">
                                    <input type="number" name="numberOfReplicas" lay-verify="required"
                                           class="layui-input" placeholder="请填写副本分片数"
                                           value="${(topic.numberOfReplicas)!'3'}"
                                           autocomplete="off">
                                </div>

                                <div class="layui-inline">索引别名年份数:</div>
                                <div class="layui-inline" style="width: 140px">
                                    <input type="number" name="aliasOfYears" lay-verify="required" class="layui-input"
                                           placeholder="请填写索引别名年份数" autocomplete="off"
                                           value="${(topic.aliasOfYears)!'3'}">
                                </div>

                                <div class="layui-inline">刷盘间隔:</div>
                                <div class="layui-inline" style="width: 100px">
                                    <input type="text" name="refreshInterval" lay-verify="required" class="layui-input"
                                           placeholder="请填写索引刷盘间隔" autocomplete="off"
                                           value="${(topic.refreshInterval)!'1s'}">
                                </div>
                                <div class="layui-inline">描述信息:</div>
                                <div class="layui-inline" style="width: 872px">
                                    <input type="text" name="description" class="layui-input" placeholder="请填写描述信息"
                                           autocomplete="off" value="${(topic.description)!''}">
                                </div>
                            </div>
                        </div>

                        <div class="layui-card-body">
                            <textarea id="txtJson" name="txtJson" class="layui-input" autocomplete="off"
                                      style="resize: none"></textarea>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="layui-form-item layui-hide">
            <input type="button" lay-submit lay-filter="btn_confirm" id="btn_confirm" value="确认">
            <input type="button" lay-submit lay-filter="btnMapping" id="btnMapping" value="">
            <textarea id="mapping" name="mapping" class="layui-input" autocomplete="off"
                      style="resize: none"></textarea>
        </div>
    </div>

    <script src="${ctx}/js/codemirror.js"></script>
    <script src="${ctx}/js/autorefresh.js"></script>
    <script src="${ctx}/js/active-line.js"></script>
    <script src="${ctx}/js/matchbrackets.js"></script>
    <script src="${ctx}/js/javascript.js"></script>
    <script>
        layui.config({base: '../../..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use(['index', 'form'], function () {
            const admin = layui.admin, $ = layui.$, form = layui.form;
            let index = ${datasources?size-1};
            disable();

            const txtJson = CodeMirror.fromTextArea(document.getElementById("txtJson"), {
                lineNumbers: true,
                indentUnit: 4,
                mode: "application/json",
                matchBrackets: true,
                theme: "idea",
                styleActiveLine: true,
                autoRefresh: true
            });
            txtJson.setSize('auto', '450px');

            const json = ${mapping};
            txtJson.setValue(JSON.stringify(json, null, "\t"));

            $("#btnMapping").click(function () {
                $("#mapping").html(txtJson.getValue());
            });

            form.on('radio(type)', function (data) {
                if (data.value === "0") {
                    disable();
                    window.location.reload();
                } else if (data.value === "1") {
                    enable();
                    const template = $(".template");
                    for (let i = 1; i < template.size(); i++) {
                        template.eq(i).remove();
                    }
                    $(".template").eq(0).find(".ds").val("");
                    index = 1;
                }
            });

            bindEvent();

            function bindEvent() {
                $(".btnAdd").click(function () {
                    const i = $(this).attr("tag");
                    const html = $("#template" + i).prop("outerHTML");
                    $("#template" + i).after(html.replace(RegExp("" + i, "g"), "__" + (index + 1)));
                    $("#template__" + (index + 1)).eq(0).find(".ds").val("");
                    unbindEvent();
                    bindEvent();
                    index++;
                });

                $(".btnDelete").click(function () {
                    if ($(".template").size() > 1) {
                        const i = $(this).attr("tag");
                        $("#template" + i).remove();
                    }
                });

                $(".btnTest").click(function () {
                    const data = {};
                    const tag = $(this).attr("tag");
                    data.host = $("input[name='host" + tag + "']").val();
                    data.port = $("input[name='port" + tag + "']").val();
                    data.uid = $("input[name='uid" + tag + "']").val();
                    data.pwd = $("input[name='pwd" + tag + "']").val();
                    data.databaseName = $("input[name='databaseName" + tag + "']").val();
                    data.tableNames = $("textarea[name='tableNames" + tag + "']").val();

                    admin.post("test", data, function () {
                        admin.success("系统提示", "连接成功");
                    });
                    console.log(data);
                });
            }

            function unbindEvent() {
                $(".btnAdd").unbind("click");
                $(".btnDelete").unbind("click");
                $(".btnTest").unbind("click");
            }

            function disable() {
                $(".btnAdd").hide();
                $(".btnDelete").hide();
                $(".btnTest").hide();
                $(".ds").attr("readonly", "readonly");
            }

            function enable() {
                $(".btnAdd").show();
                $(".btnDelete").show();
                $(".btnTest").show();
                $(".ds").removeAttr("readonly");
            }
        });
    </script>
    </body>
    </html>
</@compress>