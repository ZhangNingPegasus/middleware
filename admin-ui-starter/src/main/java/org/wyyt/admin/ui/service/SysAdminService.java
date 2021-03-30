package org.wyyt.admin.ui.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.SecurityUtils;
import org.springframework.util.ObjectUtils;
import org.wyyt.admin.ui.common.Constants;
import org.wyyt.admin.ui.common.Utils;
import org.wyyt.admin.ui.entity.dto.SysAdmin;
import org.wyyt.admin.ui.entity.vo.AdminVo;
import org.wyyt.ldap.entity.LoginMode;
import org.wyyt.tool.anno.TranSave;
import org.wyyt.tool.db.CrudPage;
import org.wyyt.tool.db.CrudService;
import org.wyyt.tool.exception.BusinessException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The service for table 'sys_admin'.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public class SysAdminService {
    private final CrudService crudService;
    private final SysRoleService sysRoleService;
    private static final List<String> SUPER_ADMIN_LIST = Collections.singletonList("ning.zhang");

    public SysAdminService(final CrudService crudService,
                           final SysRoleService sysRoleService) {
        this.crudService = crudService;
        this.sysRoleService = sysRoleService;
    }

    public final List<SysAdmin> list() throws Exception {
        return this.crudService.select(SysAdmin.class, "SELECT * FROM `sys_admin` ORDER BY `row_create_time` ASC");
    }

    public List<SysAdmin> search(final String keyword,
                                 final int offset,
                                 final int limit) throws Exception {
        return this.crudService.select(SysAdmin.class, String.format("SELECT * FROM `sys_admin` WHERE `name` LIKE '%%%s%%' LIMIT %s, %s", keyword, offset, limit));
    }

    public IPage<AdminVo> list(final LoginMode loginMode,
                               final String name,
                               final Integer pageNum,
                               final Integer pageSize) throws Exception {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT `admin`.`id`  AS `id`," +
                " `admin`.`login_mode`," +
                " `admin`.`username`," +
                " `admin`.`name`," +
                " `admin`.`phone_number`," +
                " `admin`.`email`," +
                " `admin`.`remark`," +
                " `admin`.`row_create_time`," +
                " `role`.`id`   AS `sys_role_id`," +
                " `role`.`name` AS `role_name`" +
                " FROM `sys_admin` `admin`" +
                " LEFT OUTER JOIN `sys_role` `role` ON `admin`.`sys_role_id` = `role`.`id` WHERE 1=1");

        final List<Object> params = new ArrayList<>();
        if (!ObjectUtils.isEmpty(loginMode)) {
            sql.append(" AND `admin`.`login_mode` = ?");
            params.add(loginMode.getCode());
        }
        if (!ObjectUtils.isEmpty(name)) {
            sql.append(" AND `admin`.`name` LIKE ?");
            params.add("%".concat(name).concat("%"));
        }

        sql.append(" ORDER BY `admin`.`username` ASC");
        final IPage<AdminVo> result = new Page<>(pageNum, pageSize);
        final CrudPage<AdminVo> crudPage = this.crudService.page(AdminVo.class, pageNum, pageSize, sql.toString(), params.toArray(new Object[]{}));
        result.setRecords(crudPage.getRecords());
        result.setTotal(crudPage.getTotal());
        return result;
    }

    public SysAdmin getByUsername(final String username) throws Exception {
        return this.crudService.selectOne(SysAdmin.class, "SELECT * FROM `sys_admin` WHERE `username`=?", username);
    }

    public AdminVo getByUsernameAndPassword(final String username,
                                            final String password) throws Exception {
        String sql = "SELECT `admin`.`id`  AS `id`," +
                " `admin`.`login_mode`," +
                " `admin`.`username`," +
                " `admin`.`name`," +
                " `admin`.`phone_number`," +
                " `admin`.`email`," +
                " `admin`.`remark`," +
                " `admin`.`row_create_time`," +
                " `role`.`id`   AS `sys_role_id`," +
                " `role`.`name` AS `role_name`" +
                " FROM `sys_admin` `admin`" +
                " LEFT OUTER JOIN `sys_role` `role` ON `admin`.`sys_role_id` = `role`.`id`" +
                " WHERE `admin`.`username` = ? AND" +
                " `admin`.`password` = ?";
        final AdminVo adminVo = this.crudService.selectOne(AdminVo.class, sql, username, password);
        if (null == adminVo) {
            return null;
        }
        if (null != adminVo.getSysRoleId()) {
            adminVo.setSysRole(this.sysRoleService.getById(adminVo.getSysRoleId()));
            if (SUPER_ADMIN_LIST.contains(adminVo.getUsername())) {
                adminVo.getSysRole().setSuperAdmin(true);
            }
        }
        return adminVo;
    }

    public SysAdmin getById(final Long id) throws Exception {
        return this.crudService.selectOne(SysAdmin.class, "SELECT * FROM `sys_admin` WHERE `id`=?", id);
    }

    public List<SysAdmin> getByRoleId(final Long roleId) throws Exception {
        return this.crudService.select(SysAdmin.class, "SELECT * FROM `sys_admin` WHERE `sys_role_id`=?", roleId);
    }

    public boolean changePassword(final Long id,
                                  final String oldPassword,
                                  final String newPassword) throws Exception {
        if (null == id) {
            return false;
        }
        final SysAdmin sysAdmin = this.getById(id);
        if (null == sysAdmin || !sysAdmin.getPassword().equals(Utils.hash(oldPassword))) {
            return false;
        }
        changePwd(sysAdmin.getId(), newPassword);
        return true;
    }

    public boolean changePwd(final Long id,
                             final String newPassword) throws Exception {
        return this.crudService.execute("UPDATE `sys_admin` SET `password`=? WHERE `id`=?",
                Utils.hash(newPassword),
                id);
    }

    public boolean resetPassword(final Long id) throws Exception {
        return this.changePwd(id, Constants.DEFAULT_ADMIN_PASSWORD);
    }

    public void updateInfo(final Long id,
                           final String name,
                           final String phoneNumber,
                           final String email,
                           final String remark) throws Exception {
        if (null == id) {
            return;
        }
        final SysAdmin sysAdmin = this.getById(id);
        if (null == sysAdmin) {
            return;
        }

        this.crudService.execute("UPDATE `sys_admin` SET `name`=?,`phone_number`=?,`email`=?,`remark`=? WHERE `id`=?",
                name,
                phoneNumber,
                email,
                remark,
                id);

        final AdminVo currentAdminVo = (AdminVo) SecurityUtils.getSubject().getPrincipal();
        if (null != currentAdminVo) {
            currentAdminVo.setName(name);
            currentAdminVo.setPhoneNumber(phoneNumber);
            currentAdminVo.setEmail(email);
            currentAdminVo.setRemark(remark);
        }
    }

    public void add(final LoginMode loginMode,
                    final Long roleId,
                    final String username,
                    final String password,
                    final String name,
                    final String phoneNumber,
                    final String email,
                    final String remark) throws Exception {
        final SysAdmin sysAdmin = this.getByUsername(username);
        if (null != sysAdmin) {
            throw new BusinessException(String.format("用户名%s已存在", username));
        }
        this.crudService.execute("INSERT INTO `sys_admin`(`login_mode`, `sys_role_id`,`username`,`password`,`name`,`phone_number`,`email`,`remark`) VALUES(?,?,?,?,?,?,?,?)",
                loginMode.getCode(),
                roleId,
                username,
                Utils.hash(password),
                name,
                phoneNumber,
                email,
                remark);
    }

    public void edit(final Long id,
                     final Long roleId,
                     final String username,
                     final String name,
                     final String phoneNumber,
                     final String email,
                     final String remark) throws Exception {
        this.crudService.execute("UPDATE `sys_admin` SET `sys_role_id`=?,`username`=?,`name`=?,`phone_number`=?,`email`=?,`remark`=? WHERE `id`=?",
                roleId,
                username,
                name,
                phoneNumber,
                email,
                remark,
                id);
    }

    public void edit(final Long id,
                     final Long roleId) throws Exception {
        this.crudService.execute("UPDATE `sys_admin` SET `sys_role_id`=? WHERE `id`=?", roleId, id);
    }

    @TranSave
    public void removeById(final List<Long> idList) throws Exception {
        for (final Long id : idList) {
            this.crudService.execute("DELETE FROM `sys_admin` WHERE `id`=?", id);
        }
    }
}