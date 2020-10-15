package org.wyyt.db2es.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.db2es.admin.common.Constants;
import org.wyyt.db2es.admin.entity.dto.SysAdmin;
import org.wyyt.db2es.admin.entity.dto.SysRole;
import org.wyyt.db2es.admin.service.SysAdminService;
import org.wyyt.db2es.admin.service.SysRoleService;
import org.wyyt.tool.web.Result;

import java.util.List;


/**
 * The controller for system administration's role.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(RoleController.PREFIX)
public class RoleController {
    public static final String PREFIX = "role";

    private final SysAdminService sysAdminService;
    private final SysRoleService sysRoleService;


    public RoleController(final SysAdminService sysAdminService,
                          final SysRoleService sysRoleService) {
        this.sysAdminService = sysAdminService;
        this.sysRoleService = sysRoleService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("toadd")
    public String toAdd() {
        return String.format("%s/%s", PREFIX, "add");
    }

    @GetMapping("toedit")
    public String toEdit(final Model model,
                         @RequestParam(name = "id") final Long id) {
        model.addAttribute("role", this.sysRoleService.getById(id));
        return String.format("%s/%s", PREFIX, "edit");
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<SysRole>> list(@RequestParam(value = "name", required = false) final String name,
                                      @RequestParam(value = "page") final Integer pageNum,
                                      @RequestParam(value = "limit") final Integer pageSize) {
        final IPage<SysRole> sysAdmins = this.sysRoleService.list(pageNum, pageSize, name);
        return Result.success(sysAdmins.getRecords(), sysAdmins.getTotal());
    }

    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "name") final String name,
                         @RequestParam(value = "superAdmin") final boolean superAdmin,
                         @RequestParam(value = "remark") final String remark) {
        if (this.sysRoleService.add(name, superAdmin, remark) > 0) {
            return Result.success();
        } else {
            return Result.error();
        }
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(value = "id") final Long id,
                          @RequestParam(value = "name") final String name,
                          @RequestParam(value = "superAdmin") final boolean superAdmin,
                          @RequestParam(value = "remark") final String remark) {
        if (this.sysRoleService.edit(id, name, superAdmin, remark)) {
            return Result.success();
        } else {
            return Result.error();
        }
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "id") final Long id,
                         @RequestParam(value = "name") final String name) {
        if (Constants.SYSTEM_ROLE_NAME.equals(name)) {
            return Result.error("系统内置角色不能删除");
        }

        final List<SysAdmin> sysAdminList = this.sysAdminService.getByRoleId(id);
        if (null != sysAdminList && !sysAdminList.isEmpty()) {
            return Result.error("该角色有管理正在使用,无法删除");
        }

        if (this.sysRoleService.removeById(id)) {
            return Result.success();
        } else {
            return Result.error();
        }
    }
}