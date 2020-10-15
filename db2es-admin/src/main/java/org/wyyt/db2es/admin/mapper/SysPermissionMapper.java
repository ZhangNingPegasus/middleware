package org.wyyt.db2es.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.wyyt.db2es.admin.entity.dto.SysPage;
import org.wyyt.db2es.admin.entity.dto.SysPermission;
import org.wyyt.db2es.admin.entity.vo.PageVo;
import org.wyyt.db2es.admin.entity.vo.PermissionVo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * The mapper for role's permission.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    @MapKey("id")
    Map<Long, PageVo> getPermission(@Nullable @Param("sysAdminId") Long sysAdminId);

    List<PermissionVo> list(IPage<PermissionVo> page,
                            @Nullable @Param("sysRoleId") Long sysRoleId,
                            @Nullable @Param("sysPageId") Long sysPageId);

    List<SysPage> getPermissionPagesByRoleId(@Param("sysRoleId") Long sysRoleId);
}