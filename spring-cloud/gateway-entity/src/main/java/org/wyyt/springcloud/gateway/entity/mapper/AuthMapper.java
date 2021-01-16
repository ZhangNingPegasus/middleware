package org.wyyt.springcloud.gateway.entity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Mapper
public interface AuthMapper extends BaseMapper<Auth> {
    List<Api> getApiByClientId(@Param("clientId") String clientId);

    IPage<Api> page(IPage<Auth> page,
                    @Param("appId") Long appId);

    IPage<Api> selectNoAuthApis(Page<Auth> page,
                                @Param("appId") Long appId,
                                @Param("serviceId") String serviceId,
                                @Param("name") String name,
                                @Param("path") String path);
}