package org.wyyt.gateway.admin.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.wyyt.gateway.admin.business.entity.Route;

/**
 * The mapper of table `t_route`
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Mapper
public interface RouteMapper extends BaseMapper<Route> {

}