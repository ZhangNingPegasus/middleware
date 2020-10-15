package org.wyyt.sql.tool.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.sql.tool.common.Constants;
import org.wyyt.sql.tool.database.Db;
import org.wyyt.sql.tool.entity.dto.SysAdmin;
import org.wyyt.sql.tool.entity.dto.SysRole;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.web.Result;

import java.util.List;

import static org.wyyt.sql.tool.controller.RoleController.PREFIX;


/**
 * The controller for system administration's role.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Slf4j
@Controller
@RequestMapping(PREFIX)
public final class RoleController {
    public static final String PREFIX = "role";

    private final Db db;

    public RoleController(final Db db) {
        this.db = db;
    }

    @GetMapping("tolist")
    public final String toList() {
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("toadd")
    public final String toAdd() {
        return String.format("%s/%s", PREFIX, "add");
    }

    @GetMapping("toedit")
    public final String toEdit(final Model model,
                               @RequestParam(name = "id") final Long id) throws Exception {
        model.addAttribute("role", this.db.getRoleById(id));
        return String.format("%s/%s", PREFIX, "edit");
    }

    @PostMapping("list")
    @ResponseBody
    public final Result<List<SysRole>> list(@RequestParam(value = "name", required = false) final String name,
                                            @RequestParam(value = "page") final Integer pageNum,
                                            @RequestParam(value = "limit") final Integer pageSize) throws Exception {
        final IPage<SysRole> sysAdmins = this.db.listRole(pageNum, pageSize, name);
        return Result.success(sysAdmins.getRecords(), sysAdmins.getTotal());
    }

    @PostMapping("add")
    @ResponseBody
    public final Result<?> add(@RequestParam(value = "name") final String name,
                               @RequestParam(value = "superAdmin") final boolean superAdmin,
                               @RequestParam(value = "remark") final String remark) {
        try {
            this.db.addRole(name, superAdmin, remark);
            return Result.success();
        } catch (final Exception e) {
            return Result.error(ExceptionTool.getRootCauseMessage(e));
        }
    }

    @PostMapping("edit")
    @ResponseBody
    public final Result<?> edit(@RequestParam(value = "id") final Long id,
                                @RequestParam(value = "name") final String name,
                                @RequestParam(value = "superAdmin") final boolean superAdmin,
                                @RequestParam(value = "remark") final String remark) {
        try {
            this.db.editRole(id, name, superAdmin, remark);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ExceptionTool.getRootCauseMessage(e));
        }
    }

    @PostMapping("del")
    @ResponseBody
    public final Result<?> del(@RequestParam(value = "id") final Long id,
                               @RequestParam(value = "name") final String name) throws Exception {
        if (Constants.SYSTEM_ROLE_NAME.equals(name)) {
            return Result.error("系统内置角色不能删除");
        }
        final List<SysAdmin> sysAdminList = this.db.getAdminByRoleId(id);
        if (null != sysAdminList && !sysAdminList.isEmpty()) {
            return Result.error("该角色有管理正在使用,无法删除");
        }
        try {
            this.db.removeRoleById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ExceptionTool.getRootCauseMessage(e));
        }
    }
}