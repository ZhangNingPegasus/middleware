package org.wyyt.admin.ui.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.wyyt.admin.ui.config.AdminUiProperties;
import org.wyyt.admin.ui.entity.vo.AdminVo;
import org.wyyt.admin.ui.service.SysAdminService;
import org.wyyt.admin.ui.service.SysRoleService;
import org.wyyt.admin.ui.spi.UserService;
import org.wyyt.ldap.entity.LoginMode;
import org.wyyt.ldap.entity.UserInfo;
import org.wyyt.tool.common.CommonTool;
import org.wyyt.tool.exception.BusinessException;
import org.wyyt.tool.rpc.Result;

import java.util.List;

/**
 * The controller for system administration.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(AdminController.PREFIX)
public class AdminController {
    public static final String PREFIX = "admin";

    private final AdminUiProperties adminUiProperties;
    private final SysAdminService sysAdminService;
    private final SysRoleService sysRoleService;
    private final UserService userService;

    public AdminController(final AdminUiProperties adminUiProperties,
                           final SysAdminService sysAdminService,
                           final SysRoleService sysRoleService,
                           final UserService userService) {
        this.adminUiProperties = adminUiProperties;
        this.sysAdminService = sysAdminService;
        this.sysRoleService = sysRoleService;
        this.userService = userService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("toadd")
    public String toAdd(final Model model) throws Exception {
        model.addAttribute("roles", this.sysRoleService.listOrderByName());

        if (this.adminUiProperties.getLoginMode() == LoginMode.DB) {
            return String.format("%s/%s", PREFIX, "add");
        }
        if (this.adminUiProperties.getLoginMode() == LoginMode.LDAP) {
            return String.format("%s/%s", PREFIX, "addldap");
        }
        throw new BusinessException(String.format("暂不支持登录模式[%s]", this.adminUiProperties.getLoginMode()));
    }

    @GetMapping("toedit")
    public String toEdit(final Model model,
                         @RequestParam(name = "id") final Long id) throws Exception {
        model.addAttribute("admin", this.sysAdminService.getById(id));
        model.addAttribute("roles", this.sysRoleService.listOrderByName());
        model.addAttribute("loginMode", this.adminUiProperties.getLoginMode());
        return String.format("%s/%s", PREFIX, "edit");
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<AdminVo>> list(@RequestParam(value = "name", required = false) final String name,
                                      @RequestParam(value = "page") final Integer pageNum,
                                      @RequestParam(value = "limit") final Integer pageSize) throws Exception {
        final IPage<AdminVo> sysAdmins = this.sysAdminService.list(this.adminUiProperties.getLoginMode(), name, pageNum, pageSize);
        return Result.ok(sysAdmins.getRecords(), sysAdmins.getTotal());
    }

    @PostMapping("repwd")
    @ResponseBody
    public Result<?> repwd(@RequestParam(value = "id") final Long id) throws Exception {
        if (this.adminUiProperties.getLoginMode() == LoginMode.DB) {
            if (this.sysAdminService.resetPassword(id)) {
                return Result.ok();
            } else {
                return Result.error();
            }
        } else {
            return Result.error("登陆模式为数据库才能修改密码");
        }
    }

    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "roleId") final Long roleId,
                         @RequestParam(value = "username") final String username,
                         @RequestParam(value = "password", defaultValue = "") final String password,
                         @RequestParam(value = "name") final String name,
                         @RequestParam(value = "phoneNumber") final String phoneNumber,
                         @RequestParam(value = "email") final String email,
                         @RequestParam(value = "remark") final String remark) throws Exception {
        this.sysAdminService.add(this.adminUiProperties.getLoginMode(), roleId, username, password, name, phoneNumber, email, remark);
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
        if (this.adminUiProperties.getLoginMode() == LoginMode.DB) {
            this.sysAdminService.edit(id, roleId, username, name, phoneNumber, email, remark);
        } else {
            this.sysAdminService.edit(id, roleId);
        }
        return Result.ok();
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "ids") final String ids) throws Exception {
        final List<Long> idList = CommonTool.parseList(ids, ",", Long.class);
        this.sysAdminService.removeById(idList);
        return Result.ok();
    }

    @PostMapping("search")
    @ResponseBody
    public Result<List<UserInfo>> search(@RequestParam(value = "keyword", defaultValue = "") final String keyword) throws Exception {
        if (ObjectUtils.isEmpty(keyword.trim())) {
            return Result.ok();
        }
        return Result.ok(this.userService.search(keyword, 0, 10));
    }
}