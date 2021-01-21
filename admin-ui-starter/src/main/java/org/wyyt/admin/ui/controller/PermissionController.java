package org.wyyt.admin.ui.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.wyyt.admin.ui.entity.dto.SysPage;
import org.wyyt.admin.ui.entity.dto.SysPermission;
import org.wyyt.admin.ui.entity.vo.PermissionVo;
import org.wyyt.admin.ui.service.SysPageService;
import org.wyyt.admin.ui.service.SysPermissionService;
import org.wyyt.admin.ui.service.SysRoleService;
import org.wyyt.tool.common.CommonTool;
import org.wyyt.tool.rpc.Result;

import java.util.List;
import java.util.stream.Collectors;


/**
 * The controller for role's permission.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PermissionController.PREFIX)
public class PermissionController {
    public static final String PREFIX = "permission";

    private final SysPermissionService sysPermissionService;
    private final SysRoleService sysRoleService;
    private final SysPageService sysPageService;

    public PermissionController(final SysPermissionService sysPermissionService,
                                final SysRoleService sysRoleService,
                                final SysPageService sysPageService) {
        this.sysPermissionService = sysPermissionService;
        this.sysRoleService = sysRoleService;
        this.sysPageService = sysPageService;
    }

    @GetMapping("tolist")
    public String toList(final Model model) throws Exception {
        model.addAttribute("roles", this.sysRoleService.getNotSuperAdmin());
        model.addAttribute("pages", this.sysPageService.listEmptyUrl());
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("toadd")
    public String toAdd(final Model model) throws Exception {
        model.addAttribute("roles", this.sysRoleService.getNotSuperAdmin());
        return String.format("%s/%s", PREFIX, "add");
    }

    @PostMapping("getPages")
    @ResponseBody
    public Result<List<SysPage>> getPages(@RequestParam(value = "sysRoleId") final Long sysRoleId) throws Exception {
        final List<SysPage> allPages = this.sysPageService.list();
        final List<SysPage> pages = this.sysPermissionService.getPermissionPagesByRoleId(sysRoleId);
        allPages.removeAll(pages);
        return Result.ok(allPages.stream().filter(p -> !ObjectUtils.isEmpty(p.getUrl())).collect(Collectors.toList()));
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<PermissionVo>> list(@RequestParam(value = "page") final Integer pageNum,
                                           @RequestParam(value = "limit") final Integer pageSize,
                                           @RequestParam(value = "sysRoleId", required = false) final Long sysRoleId,
                                           @RequestParam(value = "sysPageId", required = false) final Long sysPageId) throws Exception {
        final IPage<PermissionVo> list = this.sysPermissionService.list(pageNum, pageSize, sysRoleId, sysPageId);
        return Result.ok(list.getRecords(), list.getTotal());
    }


    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "sysRoleId") final Long sysRoleId,
                         @RequestParam(value = "sysPageId") final Long sysPageId,
                         @RequestParam(value = "insert", defaultValue = "false") final Boolean insert,
                         @RequestParam(value = "delete", defaultValue = "false") final Boolean delete,
                         @RequestParam(value = "update", defaultValue = "false") final Boolean update,
                         @RequestParam(value = "select", defaultValue = "false") final Boolean select) throws Exception {
        final SysPermission authPermission = new SysPermission();
        authPermission.setSysRoleId(sysRoleId);
        authPermission.setSysPageId(sysPageId);
        authPermission.setCanInsert(insert);
        authPermission.setCanDelete(delete);
        authPermission.setCanUpdate(update);
        authPermission.setCanSelect(select);
        this.sysPermissionService.insert(authPermission);
        return Result.ok();
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(value = "id") final Long id,
                          @RequestParam(value = "type") final String type,
                          @RequestParam(value = "hasPermission") final Boolean hasPermission) throws Exception {
        final SysPermission dbAdminPermission = this.sysPermissionService.getById(id);
        if (null != dbAdminPermission) {
            switch (type.toLowerCase()) {
                case "insert":
                    dbAdminPermission.setCanInsert(hasPermission);
                    break;
                case "delete":
                    dbAdminPermission.setCanDelete(hasPermission);
                    break;
                case "update":
                    dbAdminPermission.setCanUpdate(hasPermission);
                    break;
                case "select":
                    dbAdminPermission.setCanSelect(hasPermission);
                    break;
            }
            this.sysPermissionService.updateById(dbAdminPermission);
        }
        return Result.ok();
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "ids") final String ids) throws Exception {
        final List<Long> idList = CommonTool.parseList(ids, ",", Long.class);
        this.sysPermissionService.removeByIds(idList);
        return Result.ok();
    }
}