package org.wyyt.springcloud.gateway.entity.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.wyyt.redis.service.RedisService;
import org.wyyt.springcloud.gateway.entity.anno.TranRead;
import org.wyyt.springcloud.gateway.entity.entity.Api;
import org.wyyt.springcloud.gateway.entity.entity.Auth;
import org.wyyt.springcloud.gateway.entity.mapper.AuthMapper;

import java.util.List;

/**
 * The service of `t_auth` table
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Service
public class AuthService extends ServiceImpl<AuthMapper, Auth> {

    @Autowired
    protected RedisService redisService;
    @Lazy
    @Autowired
    protected AppService appService;

    @TranRead
    public List<Api> getApiByClientId(String clientId) {
        return this.baseMapper.getApiByClientId(clientId);
    }

    @TranRead
    public IPage<Auth> page(final Integer pageNum,
                            final Integer pageSize,
                            final Long appId) {
        if (null == appId) {
            return null;
        }
        final Page<Auth> page = new Page<>(pageNum, pageSize);
        final QueryWrapper<Auth> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Auth::getAppId, appId);
        return this.page(page, queryWrapper);
    }

    @TranRead
    public Auth getByAppIdAndApiId(final Long appId,
                                   final Long apiId) {
        if (null == appId) {
            return null;
        }

        final QueryWrapper<Auth> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Auth::getAppId, appId)
                .eq(Auth::getApiId, apiId);
        return this.getOne(queryWrapper);
    }
}