package org.wyyt.springcloud.gateway.entity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.wyyt.springcloud.gateway.entity.entity.Api;

import java.util.List;

/**
 * The mapper of table `t_api`
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Mapper
public interface ApiMapper extends BaseMapper<Api> {

    List<String> listServiceIds();
}