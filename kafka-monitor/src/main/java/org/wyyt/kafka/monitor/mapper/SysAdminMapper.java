package org.wyyt.kafka.monitor.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.wyyt.kafka.monitor.entity.dto.SysAdmin;
import org.wyyt.kafka.monitor.entity.vo.AdminVo;

/**
 * The mapper for database's schema. Using for administrator's information.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Mapper
public interface SysAdminMapper extends BaseMapper<SysAdmin> {

    IPage<AdminVo> list(Page page, @Param("name") String name);

    AdminVo getById(@Param("sysAdminId") Long sysAdminId);

    AdminVo getByUsernameAndPassword(@Param("username") String username,
                                     @Param("password") String password);
}
