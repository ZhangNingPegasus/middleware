package org.wyyt.admin.ui.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.admin.ui.entity.vo.AdminVo;
import org.wyyt.admin.ui.service.SysAdminService;
import org.wyyt.admin.ui.service.SysRoleService;
import org.wyyt.tool.common.CommonTool;
import org.wyyt.tool.rpc.Result;

import java.util.List;

/**
 * The controller for system administration.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
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
    public String toAdd(final Model model) throws Exception {
        model.addAttribute("roles", this.sysRoleService.listOrderByName());
        return String.format("%s/%s", PREFIX, "add");
    }

    @GetMapping("toedit")
    public String toEdit(final Model model,
                         @RequestParam(name = "id") final Long id) throws Exception {
        model.addAttribute("admin", this.sysAdminService.getById(id));
        model.addAttribute("roles", this.sysRoleService.listOrderByName());
        return String.format("%s/%s", PREFIX, "edit");
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<AdminVo>> list(@RequestParam(value = "name", required = false) final String name,
                                      @RequestParam(value = "page") final Integer pageNum,
                                      @RequestParam(value = "limit") final Integer pageSize) throws Exception {
        final IPage<AdminVo> sysAdmins = this.sysAdminService.list(name, pageNum, pageSize);
        return Result.ok(sysAdmins.getRecords(), sysAdmins.getTotal());
    }

    @PostMapping("repwd")
    @ResponseBody
    public Result<?> repwd(@RequestParam(value = "id") final Long id) throws Exception {
        if (this.sysAdminService.resetPassword(id)) {
            return Result.ok();
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
                         @RequestParam(value = "remark") final String remark) throws Exception {
        this.sysAdminService.add(roleId, username, password, name, phoneNumber, email, remark);
        return Result.ok();
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(value = "id") final Long id,
                          @RequestParam(value = "roleId") final Long roleId,
                          @RequestParam(value = "username") final String username,
                          @RequestParam(value = "name") final String name,
                          @RequestParam(value = "phoneNumber") final String phoneNumber,
                          @RequestParam(value = "email") final String email,
                          @RequestParam(value = "remark") final String remark) throws Exception {
        this.sysAdminService.edit(id, roleId, username, name, phoneNumber, email, remark);
        return Result.ok();
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "ids") final String ids) throws Exception {
        final List<Long> idList = CommonTool.parseList(ids, ",", Long.class);
        this.sysAdminService.removeById(idList);
        return Result.ok();
    }
}