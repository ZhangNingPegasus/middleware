package org.wyyt.admin.ui.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wyyt.admin.ui.entity.dto.SysRole;
import org.wyyt.admin.ui.exception.BusinessException;
import org.wyyt.tool.db.CrudPage;
import org.wyyt.tool.db.CrudService;

import java.util.List;

/**
 * The service for table 'sys_admin'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class SysRoleService {

    private final CrudService crudService;

    public SysRoleService(final @Qualifier("adminUiCrudService") CrudService crudService) {
        this.crudService = crudService;
    }

    public SysRole getById(final Long sysRoleId) throws Exception {
        return crudService.selectOne(SysRole.class, "SELECT * FROM `sys_role` WHERE `id`=?", sysRoleId);
    }

    public SysRole getByUsername(final String name) throws Exception {
        return crudService.selectOne(SysRole.class, "SELECT * FROM `sys_role` WHERE `name`=?", name);
    }

    public List<SysRole> listOrderByName() throws Exception {
        return crudService.select(SysRole.class, "SELECT * FROM `sys_role` ORDER BY `name` ASC");
    }

    public IPage<SysRole> list(final String name,
                               final Integer pageNum,
                               final Integer pageSize) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM `sys_role` WHERE 1=1");
        if (!ObjectUtils.isEmpty(name)) {
            sql.append(" AND `name` LIKE ?");
        }
        sql.append(" ORDER BY `name` ASC");
        final IPage<SysRole> result = new Page<>(pageNum, pageSize);
        final CrudPage<SysRole> crudPage = crudService.page(SysRole.class, pageNum, pageSize, sql.toString(), ObjectUtils.isEmpty(name) ? null : "%".concat(name).concat("%"));
        result.setRecords(crudPage.getRecrods());
        result.setTotal(crudPage.getTotal());
        return result;
    }

    public void add(final String name,
                    final Boolean superAdmin,
                    final String remark) throws Exception {
        final SysRole sysRole = this.getByUsername(name);
        if (null != sysRole) {
            throw new BusinessException(String.format("角色名[%s]已存在", name));
        }
        this.crudService.execute("INSERT INTO `sys_role`(`name`, `super_admin`, `remark`) VALUES (?,?,?)",
                name,
                superAdmin,
                remark);
    }

    public void edit(final Long id,
                     final String name,
                     final Boolean superAdmin,
                     final String remark) throws Exception {

        final SysRole sysRole = this.getById(id);
        if (null == sysRole) {
            throw new BusinessException(String.format("角色[%s]不存在", name));
        }
        this.crudService.execute("UPDATE `sys_role` SET `name`=?, `super_admin`=?, `remark`=? WHERE `id`=?",
                name,
                superAdmin,
                remark,
                id);
    }

    public void removeById(final Long id) throws Exception {
        this.crudService.execute("DELETE FROM `sys_role` WHERE `id`=?", id);
    }

    public List<SysRole> getNotSuperAdmin() throws Exception {
        return this.crudService.select(SysRole.class, "SELECT * FROM `sys_role` WHERE `super_admin`=false ORDER BY `row_create_time` ASC");
    }
}