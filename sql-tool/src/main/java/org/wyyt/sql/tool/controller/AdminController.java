package org.wyyt.sql.tool.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.wyyt.sql.tool.common.Constants;
import org.wyyt.sql.tool.database.Db;
import org.wyyt.sql.tool.entity.vo.AdminVo;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.web.Result;

import java.util.List;

/**
 * The controller of admin
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Slf4j
@Controller
@RequestMapping("admin")
public final class AdminController {
    public static final String PREFIX = "admin";
    private final Db db;

    public AdminController(final Db db) {
        this.db = db;
    }

    @GetMapping("tolist")
    public final String toList() {
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("toadd")
    public final String toAdd(final Model model) throws Exception {
        model.addAttribute("roles", this.db.listRole());
        return String.format("%s/%s", PREFIX, "add");
    }

    @GetMapping("toedit")
    public final String toEdit(final Model model,
                               @RequestParam(name = "id") final Long id) throws Exception {
        model.addAttribute("admin", this.db.getAdminById(id));
        model.addAttribute("roles", this.db.listRole());
        return String.format("%s/%s", PREFIX, "edit");
    }

    @PostMapping("list")
    @ResponseBody
    public final Result<List<AdminVo>> list(@RequestParam(value = "name", required = false) final String name,
                                            @RequestParam(value = "page") final Integer pageNum,
                                            @RequestParam(value = "limit") final Integer pageSize) throws Exception {
        final IPage<AdminVo> sysAdmins = this.db.listAdmin(pageNum, pageSize, name);
        return Result.success(sysAdmins.getRecords(), sysAdmins.getTotal());
    }

    @PostMapping("repwd")
    @ResponseBody
    public final Result<?> repwd(@RequestParam(value = "id") final Long id) {
        try {
            this.db.resetPassword(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error();
        }
    }

    @PostMapping("add")
    @ResponseBody
    public final Result<?> add(@RequestParam(value = "roleId") final Long roleId,
                               @RequestParam(value = "username") final String username,
                               @RequestParam(value = "password") final String password,
                               @RequestParam(value = "name") final String name,
                               @RequestParam(value = "phoneNumber") final String phoneNumber,
                               @RequestParam(value = "email") final String email,
                               @RequestParam(value = "remark") final String remark) {
        try {
            this.db.addAdmin(roleId, username, password, name, phoneNumber, email, remark);
            return Result.success();
        } catch (final Exception e) {
            return Result.error(ExceptionTool.getRootCauseMessage(e));
        }
    }

    @PostMapping("edit")
    @ResponseBody
    public final Result<?> edit(@RequestParam(value = "id") final Long id,
                                @RequestParam(value = "roleId") final Long roleId,
                                @RequestParam(value = "username") final String username,
                                @RequestParam(value = "name") final String name,
                                @RequestParam(value = "phoneNumber") final String phoneNumber,
                                @RequestParam(value = "email") final String email,
                                @RequestParam(value = "remark") final String remark) {
        try {
            this.db.editAdmin(id, roleId, username, name, phoneNumber, email, remark);
            return Result.success();
        } catch (final Exception e) {
            return Result.error(ExceptionTool.getRootCauseMessage(e));
        }
    }

    @PostMapping("del")
    @ResponseBody
    public final Result<?> del(@RequestParam(value = "id") final Long id,
                               @RequestParam(value = "username") final String username) {
        if (Constants.DEFAULT_ADMIN_USER_NAME.equals(username)) {
            return Result.error("系统内置账户不能删除");
        }
        try {
            this.db.removeAdminById(id);
            return Result.success();
        } catch (final Exception e) {
            return Result.error(ExceptionTool.getRootCauseMessage(e));
        }
    }
}