package org.wyyt.springcloud.gateway.entity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.wyyt.springcloud.gateway.entity.entity.Route;

/**
 * The mapper of table `t_route`
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Mapper
public interface RouteMapper extends BaseMapper<Route> {

}