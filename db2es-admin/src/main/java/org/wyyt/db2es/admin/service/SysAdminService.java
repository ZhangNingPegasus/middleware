package org.wyyt.db2es.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wyyt.db2es.admin.common.Constants;
import org.wyyt.db2es.admin.common.Utils;
import org.wyyt.db2es.admin.entity.dto.SysAdmin;
import org.wyyt.db2es.admin.entity.vo.AdminVo;
import org.wyyt.db2es.admin.mapper.SysAdminMapper;
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
public class SysAdminService extends ServiceImpl<SysAdminMapper, SysAdmin> {

    private final SysRoleService sysRoleService;

    public SysAdminService(final SysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    @TranRead
    public IPage<AdminVo> list(final Integer pageNum,
                               final Integer pageSize,
                               String name) {
        if (!ObjectUtils.isEmpty(name)) {
            name = name.trim();
        }
        return this.baseMapper.list(new Page<>(pageNum, pageSize), name);
    }

    @TranRead
    public SysAdmin getByUsername(final String username) {
        final QueryWrapper<SysAdmin> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysAdmin::getUsername, username);
        return this.baseMapper.selectOne(queryWrapper);
    }

    @TranRead
    public AdminVo getByUsernameAndPassword(final String username,
                                            final String password) {
        final AdminVo adminVo = this.baseMapper.getByUidAndPwd(username, password);
        if (null == adminVo) {
            return null;
        }
        adminVo.setSysRole(sysRoleService.getById(adminVo.getSysRoleId()));
        return adminVo;
    }


    public boolean changePassword(final Long id,
                                  final String oldPassword,
                                  final String newPassword) {
        if (null == id) {
            return false;
        }
        final SysAdmin sysAdmin = this.getById(id);
        if (null == sysAdmin || !sysAdmin.getPassword().equals(Utils.hash(oldPassword))) {
            return false;
        }
        changePwd(sysAdmin, newPassword);
        return true;
    }

    @TranSave
    void changePwd(final SysAdmin sysAdmin,
                   final String newPassword) {
        sysAdmin.setPassword(Utils.hash(newPassword));
        this.baseMapper.updateById(sysAdmin);
    }

    @TranSave
    public boolean resetPassword(final Long id) {
        final UpdateWrapper<SysAdmin> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(SysAdmin::getId, id)
                .set(SysAdmin::getPassword, Constants.DEFAULT_ADMIN_PASSWORD);
        return this.update(updateWrapper);
    }

    @TranSave
    public boolean updateInfo(final Long id,
                              final String name,
                              final String phoneNumber,
                              final String email,
                              final String remark) {
        if (null == id) {
            return false;
        }
        final SysAdmin sysAdmin = getById(id);
        if (null == sysAdmin) {
            return false;
        }
        sysAdmin.setName(name);
        sysAdmin.setPhoneNumber(phoneNumber);
        sysAdmin.setEmail(email);
        sysAdmin.setRemark(remark);
        this.baseMapper.updateById(sysAdmin);

        final AdminVo currentAdminVo = (AdminVo) SecurityUtils.getSubject().getPrincipal();
        currentAdminVo.setName(name);
        currentAdminVo.setPhoneNumber(phoneNumber);
        currentAdminVo.setEmail(email);
        currentAdminVo.setRemark(remark);
        return true;
    }

    @TranSave
    public int add(final Long roleId,
                   final String username,
                   final String password,
                   final String name,
                   final String phoneNumber,
                   final String email,
                   final String remark) {
        SysAdmin sysAdmin = this.getByUsername(username);
        if (null == sysAdmin) {
            sysAdmin = new SysAdmin();
            sysAdmin.setSysRoleId(roleId);
            sysAdmin.setUsername(username);
            sysAdmin.setPassword(Utils.hash(password));
            sysAdmin.setName(name);
            sysAdmin.setPhoneNumber(phoneNumber);
            sysAdmin.setEmail(email);
            sysAdmin.setRemark(remark);
            return this.baseMapper.insert(sysAdmin);
        }
        throw new Db2EsException(String.format("用户名%s已存在", username));
    }

    @TranSave
    public boolean edit(final Long id,
                        final Long roleId,
                        final String username,
                        final String name,
                        final String phoneNumber,
                        final String email,
                        final String remark) {
        final UpdateWrapper<SysAdmin> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(SysAdmin::getId, id)
                .set(SysAdmin::getSysRoleId, roleId)
                .set(SysAdmin::getName, name)
                .set(SysAdmin::getPhoneNumber, phoneNumber)
                .set(SysAdmin::getEmail, email)
                .set(SysAdmin::getRemark, remark);
        return this.update(updateWrapper);
    }

    @TranRead
    public List<SysAdmin> getByRoleId(final Long roleId) {
        final QueryWrapper<SysAdmin> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysAdmin::getSysRoleId, roleId);
        return this.list(queryWrapper);
    }
}