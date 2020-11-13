package org.wyyt.db2es.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.wyyt.db2es.admin.entity.dto.SysPage;
import org.wyyt.db2es.admin.entity.dto.SysPermission;
import org.wyyt.db2es.admin.entity.vo.PageVo;
import org.wyyt.db2es.admin.entity.vo.PermissionVo;
import org.wyyt.db2es.admin.mapper.SysPermissionMapper;
import org.wyyt.sharding.anno.TranRead;

import java.util.List;
import java.util.Map;

/**
 * The service for table 'sys_permission'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Service
public class SysPermissionService extends ServiceImpl<SysPermissionMapper, SysPermission> {

    @TranRead
    public Map<Long, PageVo> getPermissionPages(final Long sysAdminId) {
        return this.baseMapper.getPermission(sysAdminId);
    }

    @TranRead
    public IPage list(final Integer pageNum,
                      final Integer pageSize,
                      final Long sysRoleId,
                      final Long sysPageId) {
        final IPage<PermissionVo> page = new Page<>(pageNum, pageSize);
        final List<PermissionVo> list = this.baseMapper.list(page, sysRoleId, sysPageId);
        page.setRecords(list);
        return page;
    }

    @TranRead
    public List<SysPage> getPermissionPagesByRoleId(final Long sysRoleId) {
        return this.baseMapper.getPermissionPagesByRoleId(sysRoleId);
    }
}