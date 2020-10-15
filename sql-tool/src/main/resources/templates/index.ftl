<@compress single_line=true>
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <#include "common/layui.ftl">
    </head>
    <body class="layui-layout-body">
    <div id="LAY_app" class="layadmin-side-shrink">
        <div class="layui-layout layui-layout-admin">
            <div class="layui-header">
                <ul class="layui-nav layui-layout-left">
                    <li class="layui-nav-item layadmin-flexible" lay-unselect>
                        <a href="javascript:" layadmin-event="flexible" title="侧边伸缩">
                            <#--<i class="layui-icon layui-icon-shrink-right" id="LAY_app_flexible"></i>-->
                            <i class="layui-icon layui-icon-spread-left" id="LAY_app_flexible"></i>
                        </a>
                    </li>
                    <li class="layui-nav-item" lay-unselect>
                        <a href="javascript:" layadmin-event="refresh" title="刷新">
                            <i class="layui-icon layui-icon-refresh-3"></i>
                        </a>
                    </li>
                    <li class="layui-nav-item" lay-unselect>
                        <a href="javascript:void(0)" style="cursor: default">
                            <span>当前版本:&nbsp;${version}</span>
                        </a>
                    </li>
                </ul>
                <ul class="layui-nav layui-layout-right" lay-filter="layadmin-layout-right">
                    <li class="layui-nav-item layui-hide-xs" lay-unselect>
                        <a href="javascript:" layadmin-event="fullscreen">
                            <i class="layui-icon layui-icon-screen-full"></i>
                        </a>
                    </li>
                    <li class="layui-nav-item" style="margin-right: 10px" lay-unselect>
                        <a href="javascript:">
                            <cite>${admin.name}</cite>
                        </a>
                        <dl class="layui-nav-child">
                            <dd><a lay-href="${ctx}/toinfo">基本资料</a></dd>
                            <dd><a lay-href="${ctx}/topassword">修改密码</a></dd>
                            <hr>
                            <dd layadmin-event="logout" style="text-align: center;"><a>退出</a></dd>
                        </dl>
                    </li>
                </ul>
            </div>
            <div class="layui-side layui-side-menu">
                <div class="layui-side-scroll">
                    <div class="layui-logo" lay-href="${defaultPage}">
                        <span>${name}</span>
                    </div>
                    <ul class="layui-nav layui-nav-tree" lay-shrink="all" id="LAY-system-side-menu"
                        lay-filter="layadmin-system-side-menu">
                        <li data-name="template" class="layui-nav-item layui-nav-itemed">
                            <a lay-href="${defaultPage}" lay-tips="SQL执行" class="layui-this">
                                <i class="layui-icon layui-icon-template"></i>
                                <cite>SQL执行</cite>
                            </a>
                        </li>

                        <li data-name="admin" class="layui-nav-item layui-nav-itemed">
                            <a lay-href="${ctx}/his/tolist" lay-tips="SQL历史">
                                <i class="layui-icon layui-icon-component"></i>
                                <cite>SQL历史</cite>
                            </a>
                        </li>

                        <li data-name="admin" class="layui-nav-item layui-nav-itemed">
                            <a lay-href="${ctx}/table/tolist" lay-tips="创建逻辑表">
                                <i class="layui-icon layui-icon-table"></i>
                                <cite>创建逻辑表</cite>
                            </a>
                        </li>

                        <#if admin.role.superAdmin>
                            <li data-name="admin" class="layui-nav-item layui-nav-itemed">
                                <a lay-href="${ctx}/admin/tolist" lay-tips="用户管理">
                                    <i class="layui-icon layui-icon-username"></i>
                                    <cite>用户管理</cite>
                                </a>
                            </li>

                            <li data-name="admin" class="layui-nav-item layui-nav-itemed">
                                <a lay-href="${ctx}/role/tolist" lay-tips="角色管理">
                                    <i class="layui-icon layui-icon-group"></i>
                                    <cite>角色管理</cite>
                                </a>
                            </li>

                            <li data-name="admin" class="layui-nav-item layui-nav-itemed">
                                <a lay-href="${ctx}/permission/tolist" lay-tips="权限管理">
                                    <i class="layui-icon layui-icon-auz"></i>
                                    <cite>权限管理</cite>
                                </a>
                            </li>
                        </#if>
                    </ul>
                </div>
            </div>
            <div class="layadmin-pagetabs" id="LAY_app_tabs">
                <div class="layui-icon layadmin-tabs-control layui-icon-prev" layadmin-event="leftPage"></div>
                <div class="layui-icon layadmin-tabs-control layui-icon-next" layadmin-event="rightPage"></div>
                <div class="layui-icon layadmin-tabs-control layui-icon-down">
                    <ul class="layui-nav layadmin-tabs-select" lay-filter="layadmin-pagetabs-nav">
                        <li class="layui-nav-item" lay-unselect>
                            <a href="javascript:"></a>
                            <dl class="layui-nav-child layui-anim-fadein">
                                <dd layadmin-event="closeThisTabs"><a href="javascript:">关闭当前标签页</a></dd>
                                <dd layadmin-event="closeOtherTabs"><a href="javascript:">关闭其它标签页</a></dd>
                                <dd layadmin-event="closeAllTabs"><a href="javascript:">关闭全部标签页</a></dd>
                            </dl>
                        </li>
                    </ul>
                </div>
                <div class="layui-tab" lay-unauto lay-allowClose="true" lay-filter="layadmin-layout-tabs">
                    <ul class="layui-tab-title" id="LAY_app_tabsheader">
                        <li lay-id="${defaultPage}" lay-attr="${defaultPage}"
                            class="layui-this"><i
                                    class="layui-icon layui-icon-home"></i></li>
                    </ul>
                </div>
            </div>
            <div class="layui-body" id="LAY_app_body">
                <div class="layadmin-tabsbody-item layui-show">
                    <iframe src="${defaultPage}" style="border:0" class="layadmin-iframe"></iframe>
                </div>
            </div>
            <div class="layadmin-body-shade" layadmin-event="shade"></div>
        </div>
    </div>
    <script>
        layui.config({base: '..${ctx}/layuiadmin/'}).extend({index: 'lib/index'}).use('index', function () {
        });
    </script>
    </body>
    </html>
</@compress>