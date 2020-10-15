package org.wyyt.db2es.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.wyyt.db2es.admin.entity.dto.SysAdmin;
import org.wyyt.db2es.admin.entity.vo.AdminVo;

/**
 * The mapper of table sys_admin
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Mapper
public interface SysAdminMapper extends BaseMapper<SysAdmin> {

    IPage<AdminVo> list(Page page, @Param("name") String name);

    AdminVo getById(@Param("sysAdminId") Long sysAdminId);

    AdminVo getByUidAndPwd(@Param("username") String username,
                           @Param("password") String password);
}