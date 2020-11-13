package org.wyyt.kafka.monitor.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wyyt.kafka.monitor.anno.TranRead;
import org.wyyt.kafka.monitor.anno.TranSave;
import org.wyyt.kafka.monitor.common.Constants;
import org.wyyt.kafka.monitor.entity.dto.SysAdmin;
import org.wyyt.kafka.monitor.entity.vo.AdminVo;
import org.wyyt.kafka.monitor.exception.BusinessException;
import org.wyyt.kafka.monitor.mapper.SysAdminMapper;
import org.wyyt.kafka.monitor.util.SecurityUtil;

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
        final AdminVo adminVo = this.baseMapper.getByUsernameAndPassword(username, password);
        if (null == adminVo) {
            return null;
        }
        adminVo.setSysRole(this.sysRoleService.getById(adminVo.getSysRoleId()));
        return adminVo;
    }


    @TranSave
    public boolean changePassword(final Long id,
                                  final String oldPassword,
                                  final String newPassword) {
        if (null == id) {
            return false;
        }
        final SysAdmin sysAdmin = this.getById(id);

        if (null == sysAdmin || !sysAdmin.getPassword().equals(SecurityUtil.hash(oldPassword))) {
            return false;
        }
        changePwd(sysAdmin, newPassword);
        return true;
    }

    @TranSave
    void changePwd(final SysAdmin sysAdmin,
                   final String newPassword) {
        sysAdmin.setPassword(SecurityUtil.hash(newPassword));
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
                              final Boolean gender,
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
        sysAdmin.setGender(gender);
        sysAdmin.setPhoneNumber(phoneNumber);
        sysAdmin.setEmail(email);
        sysAdmin.setRemark(remark);
        this.baseMapper.updateById(sysAdmin);

        final AdminVo currentAdminVo = (AdminVo) SecurityUtils.getSubject().getPrincipal();
        currentAdminVo.setName(name);
        currentAdminVo.setGender(gender);
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
                   final Boolean gender,
                   final String phoneNumber,
                   final String email,
                   final String remark) {
        SysAdmin sysAdmin = this.getByUsername(username);
        if (null == sysAdmin) {
            sysAdmin = new SysAdmin();
            sysAdmin.setSysRoleId(roleId);
            sysAdmin.setUsername(username);
            sysAdmin.setPassword(SecurityUtil.hash(password));
            sysAdmin.setName(name);
            sysAdmin.setGender(gender);
            sysAdmin.setPhoneNumber(phoneNumber);
            sysAdmin.setEmail(email);
            sysAdmin.setRemark(remark);
            return this.baseMapper.insert(sysAdmin);
        }
        throw new BusinessException(String.format("用户名[%s]已存在", username));
    }

    @TranSave
    public boolean edit(final Long id,
                        final Long roleId,
                        final String username,
                        final String name,
                        final Boolean gender,
                        final String phoneNumber,
                        final String email,
                        final String remark) {
        final UpdateWrapper<SysAdmin> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(SysAdmin::getId, id)
                .set(SysAdmin::getSysRoleId, roleId)
                .set(SysAdmin::getName, name)
                .set(SysAdmin::getGender, gender)
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