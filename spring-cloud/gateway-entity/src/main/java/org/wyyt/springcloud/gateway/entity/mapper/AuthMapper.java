package org.wyyt.springcloud.gateway.entity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.wyyt.springcloud.gateway.entity.entity.Api;
import org.wyyt.springcloud.gateway.entity.entity.Auth;

import java.util.List;

/**
 * The mapper of table `t_auth`
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Mapper
public interface AuthMapper extends BaseMapper<Auth> {
    List<Api> getApiByClientId(@Param("clientId") String clientId);
}