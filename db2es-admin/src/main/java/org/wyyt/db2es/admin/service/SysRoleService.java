package org.wyyt.db2es.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.wyyt.db2es.admin.entity.dto.SysRole;
import org.wyyt.db2es.admin.mapper.SysRoleMapper;
import org.wyyt.db2es.core.exception.Db2EsException;
import org.wyyt.sharding.anno.TranRead;
import org.wyyt.sharding.anno.TranSave;

import java.util.List;

/**
 * The service for table 'sys_admin'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Service
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {
    @TranRead
    public SysRole getById(final Long sysRoleId) {
        final QueryWrapper<SysRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysRole::getId, sysRoleId);
        return this.getOne(queryWrapper);
    }

    @TranRead
    public IPage<SysRole> list(final Integer pageNum,
                               final Integer pageSize,
                               String name) {
        if (!StringUtils.isEmpty(name)) {
            name = name.trim();
        }
        final QueryWrapper<SysRole> queryWrapper = new QueryWrapper<>();

        final LambdaQueryWrapper<SysRole> lambda = queryWrapper.lambda();
        if (!StringUtils.isEmpty(name)) {
            lambda.like(SysRole::getName, name);
        }
        lambda.orderByAsc(SysRole::getName);
        return this.page(new Page<>(pageNum, pageSize), queryWrapper);
    }

    @TranRead
    public SysRole getByUsername(final String name) {
        final QueryWrapper<SysRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysRole::getName, name);
        return this.baseMapper.selectOne(queryWrapper);
    }

    @TranSave
    public int add(final String name,
                   final Boolean superAdmin,
                   final String remark) {
        SysRole sysRole = this.getByUsername(name);
        if (sysRole == null) {
            sysRole = new SysRole();
            sysRole.setName(name);
            sysRole.setSuperAdmin(superAdmin);
            sysRole.setRemark(remark);
            return this.baseMapper.insert(sysRole);
        }
        throw new Db2EsException(String.format("角色名%s已存在", name));
    }

    @TranSave
    public boolean edit(final Long id,
                        final String name,
                        final Boolean superAdmin,
                        final String remark) {
        final UpdateWrapper<SysRole> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(SysRole::getId, id)
                .set(SysRole::getName, name)
                .set(SysRole::getSuperAdmin, superAdmin)
                .set(SysRole::getRemark, remark);
        return this.update(updateWrapper);
    }

    @TranRead
    public List<SysRole> listOrderByName() {
        final QueryWrapper<SysRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(SysRole::getName);
        return this.list(queryWrapper);
    }
}