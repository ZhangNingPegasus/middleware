package org.wyyt.db2es.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.db2es.admin.common.Constants;
import org.wyyt.db2es.admin.entity.vo.AdminVo;
import org.wyyt.db2es.admin.service.SysAdminService;
import org.wyyt.db2es.admin.service.SysRoleService;
import org.wyyt.tool.web.Result;

import java.util.List;

/**
 * The controller for system administration.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(AdminController.PREFIX)
public class AdminController {
    public static final String PREFIX = "admin";

    private final SysAdminService sysAdminService;
    private final SysRoleService sysRoleService;

    public AdminController(final SysAdminService sysAdminService,
                           final SysRoleService sysRoleService) {
        this.sysAdminService = sysAdminService;
        this.sysRoleService = sysRoleService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("toadd")
    public String toAdd(final Model model) {
        model.addAttribute("roles", this.sysRoleService.listOrderByName());
        return String.format("%s/%s", PREFIX, "add");
    }

    @GetMapping("toedit")
    public String toEdit(final Model model,
                         @RequestParam(name = "id") final Long id) {
        model.addAttribute("admin", this.sysAdminService.getById(id));
        model.addAttribute("roles", this.sysRoleService.listOrderByName());
        return String.format("%s/%s", PREFIX, "edit");
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<AdminVo>> list(@RequestParam(value = "name", required = false) final String name,
                                      @RequestParam(value = "page") final Integer pageNum,
                                      @RequestParam(value = "limit") final Integer pageSize) {
        final IPage<AdminVo> sysAdmins = this.sysAdminService.list(pageNum, pageSize, name);
        return Result.success(sysAdmins.getRecords(), sysAdmins.getTotal());
    }

    @PostMapping("repwd")
    @ResponseBody
    public Result<?> repwd(@RequestParam(value = "id") final Long id) {
        if (this.sysAdminService.resetPassword(id)) {
            return Result.success();
        } else {
            return Result.error();
        }
    }

    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "roleId") final Long roleId,
                         @RequestParam(value = "username") final String username,
                         @RequestParam(value = "password") final String password,
                         @RequestParam(value = "name") final String name,
                         @RequestParam(value = "phoneNumber") final String phoneNumber,
                         @RequestParam(value = "email") final String email,
                         @RequestParam(value = "remark") final String remark) {
        if (this.sysAdminService.add(roleId, username, password, name, phoneNumber, email, remark) > 0) {
            return Result.success();
        } else {
            return Result.error();
        }
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(value = "id") final Long id,
                          @RequestParam(value = "roleId") final Long roleId,
                          @RequestParam(value = "username") final String username,
                          @RequestParam(value = "name") final String name,
                          @RequestParam(value = "phoneNumber") final String phoneNumber,
                          @RequestParam(value = "email") final String email,
                          @RequestParam(value = "remark") final String remark) {
        if (this.sysAdminService.edit(id, roleId, username, name, phoneNumber, email, remark)) {
            return Result.success();
        } else {
            return Result.error();
        }
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "id") final Long id,
                         @RequestParam(value = "username") final String username) {
        if (Constants.DEFAULT_ADMIN_USER_NAME.equals(username)) {
            return Result.error("系统内置账户不能删除");
        }
        if (this.sysAdminService.removeById(id)) {
            return Result.success();
        } else {
            return Result.error();
        }
    }
}