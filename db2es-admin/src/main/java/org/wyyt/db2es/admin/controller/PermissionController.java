package org.wyyt.db2es.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.wyyt.db2es.admin.entity.dto.SysPage;
import org.wyyt.db2es.admin.entity.dto.SysPermission;
import org.wyyt.db2es.admin.entity.dto.SysRole;
import org.wyyt.db2es.admin.entity.vo.PermissionVo;
import org.wyyt.db2es.admin.service.SysPageService;
import org.wyyt.db2es.admin.service.SysPermissionService;
import org.wyyt.db2es.admin.service.SysRoleService;
import org.wyyt.tool.web.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * The controller for role's permission.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
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
    public String toList(final Model model) {
        model.addAttribute("roles", this.sysRoleService.list(new QueryWrapper<SysRole>().lambda().eq(SysRole::getSuperAdmin, false).orderByAsc(SysRole::getRowCreateTime)));
        model.addAttribute("pages", this.sysPageService.list().stream().filter(p -> !StringUtils.isEmpty(p.getUrl())).collect(Collectors.toList()));
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("toadd")
    public String toAdd(final Model model) {
        model.addAttribute("roles", this.sysRoleService.list(new QueryWrapper<SysRole>().lambda().eq(SysRole::getSuperAdmin, false).orderByAsc(SysRole::getRowCreateTime)));
        return String.format("%s/%s", PREFIX, "add");
    }

    @PostMapping("getPages")
    @ResponseBody
    public Result<List<SysPage>> getPages(@RequestParam(value = "sysRoleId") final Long sysRoleId) {
        final List<SysPage> allPages = this.sysPageService.list();
        final List<SysPage> pages = this.sysPermissionService.getPermissionPagesByRoleId(sysRoleId);
        allPages.removeAll(pages);
        return Result.success(allPages.stream().filter(p -> !StringUtils.isEmpty(p.getUrl())).collect(Collectors.toList()));
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<PermissionVo>> list(@RequestParam(value = "page") final Integer pageNum,
                                           @RequestParam(value = "limit") final Integer pageSize,
                                           @RequestParam(value = "sysRoleId", required = false) final Long sysRoleId,
                                           @RequestParam(value = "sysPageId", required = false) final Long sysPageId) {
        final IPage list = this.sysPermissionService.list(pageNum, pageSize, sysRoleId, sysPageId);
        return Result.success(list.getRecords(), list.getTotal());
    }


    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "sysRoleId") final Long sysRoleId,
                         @RequestParam(value = "sysPageId") final Long sysPageId,
                         @RequestParam(value = "insert", defaultValue = "false") final Boolean insert,
                         @RequestParam(value = "delete", defaultValue = "false") final Boolean delete,
                         @RequestParam(value = "update", defaultValue = "false") final Boolean update,
                         @RequestParam(value = "select", defaultValue = "false") final Boolean select) {
        final SysPermission authPermission = new SysPermission();
        authPermission.setSysRoleId(sysRoleId);
        authPermission.setSysPageId(sysPageId);
        authPermission.setCanInsert(insert);
        authPermission.setCanDelete(delete);
        authPermission.setCanUpdate(update);
        authPermission.setCanSelect(select);
        this.sysPermissionService.save(authPermission);
        return Result.success();
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(value = "id") final Long id,
                          @RequestParam(value = "type") final String type,
                          @RequestParam(value = "hasPermission") final Boolean hasPermission) {
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
        return Result.success();
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "ids") final String ids) {
        final String[] idsArray = ids.split(",");
        final List<Long> idsList = new ArrayList<>(idsArray.length);
        for (final String id : idsArray) {
            if (null != id && !StringUtils.isEmpty(id.trim())) {
                idsList.add(Long.parseLong(id));
            }
        }
        this.sysPermissionService.removeByIds(idsList);
        return Result.success();
    }
}