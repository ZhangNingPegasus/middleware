package org.wyyt.admin.ui.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.admin.ui.entity.dto.SysAdmin;
import org.wyyt.admin.ui.entity.dto.SysRole;
import org.wyyt.admin.ui.service.SysAdminService;
import org.wyyt.admin.ui.service.SysRoleService;
import org.wyyt.tool.common.CommonTool;
import org.wyyt.tool.rpc.Result;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * The controller for system administration's role.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
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
                         @RequestParam(name = "id") final Long id) throws Exception {
        model.addAttribute("role", this.sysRoleService.getById(id));
        return String.format("%s/%s", PREFIX, "edit");
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<SysRole>> list(@RequestParam(value = "name", required = false) final String name,
                                      @RequestParam(value = "page") final Integer pageNum,
                                      @RequestParam(value = "limit") final Integer pageSize) throws Exception {
        final IPage<SysRole> sysAdmins = this.sysRoleService.list(name, pageNum, pageSize);
        return Result.ok(sysAdmins.getRecords(), sysAdmins.getTotal());
    }

    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "name") final String name,
                         @RequestParam(value = "superAdmin") final boolean superAdmin,
                         @RequestParam(value = "remark") final String remark) throws Exception {
        this.sysRoleService.add(name, superAdmin, remark);
        return Result.ok();
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(value = "id") final Long id,
                          @RequestParam(value = "name") final String name,
                          @RequestParam(value = "superAdmin") final boolean superAdmin,
                          @RequestParam(value = "remark") final String remark) throws Exception {
        this.sysRoleService.edit(id, name, superAdmin, remark);
        return Result.ok();
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "ids") final String ids) throws Exception {
        final List<Long> idList = CommonTool.parseList(ids, ",", Long.class);
        final Set<Long> idSet = new HashSet<>();
        for (final Long id : idList) {
            final List<SysAdmin> sysAdminList = this.sysAdminService.getByRoleId(id);
            if (null != sysAdminList && !sysAdminList.isEmpty()) {
                final SysRole sysRole = this.sysRoleService.getById(id);
                return Result.error(String.format("角色[%s]有管理员正在使用,无法删除", sysRole.getName()));
            }
            idSet.add(id);
        }
        this.sysRoleService.removeById(idSet);
        return Result.ok();
    }
}