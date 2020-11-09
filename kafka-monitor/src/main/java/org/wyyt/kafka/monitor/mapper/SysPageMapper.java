package org.wyyt.kafka.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.wyyt.kafka.monitor.entity.dto.SysPage;
import org.wyyt.kafka.monitor.entity.vo.PageVo;

import java.util.List;

/**
 * The mapper for database's schema. Using for administrator's information.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Mapper
public interface SysPageMapper extends BaseMapper<SysPage> {
    List<PageVo> list(IPage page, @Param("name") String name);

    Long getMaxOrderNum(@Param("parentId") Long parentId);
}