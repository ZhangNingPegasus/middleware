package org.wyyt.db2es.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.wyyt.db2es.admin.entity.dto.SysPage;
import org.wyyt.db2es.admin.entity.vo.PageVo;

import java.util.List;

/**
 * The mapper of table sys_page
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Mapper
public interface SysPageMapper extends BaseMapper<SysPage> {
    List<PageVo> list(IPage page, @Param("name") String name);

    Long getMaxOrderNum(@Param("parentId") Long parentId);

    List<PageVo> listPermissionPages(@Param("sysAdminId") Long sysAdminId);
}