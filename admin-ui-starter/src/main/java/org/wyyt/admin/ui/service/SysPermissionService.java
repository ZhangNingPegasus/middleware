package org.wyyt.admin.ui.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.wyyt.admin.ui.entity.dto.SysPage;
import org.wyyt.admin.ui.entity.dto.SysPermission;
import org.wyyt.admin.ui.entity.vo.PermissionVo;
import org.wyyt.tool.db.CrudPage;
import org.wyyt.tool.db.CrudService;

import java.util.ArrayList;
import java.util.List;

/**
 * The service for table 'sys_permission'.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class SysPermissionService {
    private final CrudService crudService;

    public SysPermissionService(final @Qualifier("adminUiCrudService") CrudService crudService) {
        this.crudService = crudService;
    }

    public SysPermission getById(final Long id) throws Exception {
        return this.crudService.selectOne(SysPermission.class, "SELECT * FROM `sys_permission` WHERE `id`=?", id);
    }

    public IPage<PermissionVo> list(final Integer pageNum,
                                    final Integer pageSize,
                                    final Long sysRoleId,
                                    final Long sysPageId) throws Exception {
        final StringBuilder sql = new StringBuilder();
        final List<Object> objectList = new ArrayList<>();
        sql.append("SELECT" +
                " `p`.`id`," +
                " `page`.`id` AS `page_id`," +
                " `page`.`name` AS `page_name`," +
                " `r`.`id` AS `role_id`," +
                " `r`.`name` AS `role_name`," +
                " `p`.`can_insert`," +
                " `p`.`can_delete`," +
                " `p`.`can_update`," +
                " `p`.`can_select`" +
                " FROM `sys_permission` `p`" +
                " LEFT OUTER JOIN `sys_page` `page` ON `page`.`id` = `p`.`sys_page_id`" +
                " LEFT OUTER JOIN `sys_role` `r` ON `p`.`sys_role_id` = `r`.`id`" +
                " WHERE 1=1");
        if (null != sysRoleId) {
            sql.append("AND `r`.`id`=?");
            objectList.add(sysRoleId);
        }
        if (null != sysPageId) {
            sql.append("AND `page`.`id`=?");
            objectList.add(sysPageId);
        }

        final CrudPage<PermissionVo> crudPage = this.crudService.page(PermissionVo.class, pageNum, pageSize, sql.toString(), objectList.toArray());
        final IPage<PermissionVo> result = new Page<>(pageNum, pageSize);
        result.setRecords(crudPage.getRecords());
        result.setTotal(crudPage.getTotal());
        return result;
    }


    public List<SysPage> getPermissionPagesByRoleId(final Long sysRoleId) throws Exception {
        return this.crudService.select(SysPage.class, "SELECT" +
                " `page`.*" +
                " FROM `sys_page` `page`" +
                " LEFT OUTER JOIN `sys_permission` `p` ON `page`.`id`=`p`.`sys_page_id`" +
                " WHERE `p`.`sys_role_id`=?", sysRoleId);
    }

    public void removeByIds(final List<Long> idsList) throws Exception {
        if (null == idsList || idsList.isEmpty()) {
            return;
        }
        final StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM `sys_permission` WHERE `id` IN (");
        for (final Long id : idsList) {
            sql.append(id.toString().concat(","));
        }
        sql.delete(sql.length() - 1, sql.length());
        sql.append(")");

        this.crudService.execute(sql.toString());
    }

    public void updateById(final SysPermission sysPermission) throws Exception {
        this.crudService.execute("UPDATE `sys_permission` SET `sys_role_id`=?,`sys_page_id`=?,`can_insert`=?,`can_delete`=?,`can_update`=?,`can_select`=? WHERE `id`=?",
                sysPermission.getSysRoleId(),
                sysPermission.getSysPageId(),
                sysPermission.getCanInsert(),
                sysPermission.getCanDelete(),
                sysPermission.getCanUpdate(),
                sysPermission.getCanSelect(),
                sysPermission.getId());
    }


    public void insert(final SysPermission sysPermission) throws Exception {
        this.crudService.execute("INSERT INTO `sys_permission`(`sys_role_id`,`sys_page_id`,`can_insert`,`can_delete`,`can_update`,`can_select`) VALUES(?,?,?,?,?,?)",
                sysPermission.getSysRoleId(),
                sysPermission.getSysPageId(),
                sysPermission.getCanInsert(),
                sysPermission.getCanDelete(),
                sysPermission.getCanUpdate(),
                sysPermission.getCanSelect());
    }
}