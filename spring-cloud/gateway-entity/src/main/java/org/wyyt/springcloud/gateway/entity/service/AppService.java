package org.wyyt.springcloud.gateway.entity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wyyt.redis.service.RedisService;
import org.wyyt.springcloud.gateway.entity.anno.TranRead;
import org.wyyt.springcloud.gateway.entity.entity.App;
import org.wyyt.springcloud.gateway.entity.mapper.AppMapper;

/**
 * The service of `t_api` table
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class AppService extends ServiceImpl<AppMapper, App> {
    @Autowired
    protected OauthClientDetailsService oauthClientDetailsService;
    @Autowired
    protected AuthService authService;
    @Autowired
    protected RedisService redisService;

    @TranRead
    public App getByClientId(final String clientId) {
        if (ObjectUtils.isEmpty(clientId)) {
            return null;
        }
        final QueryWrapper<App> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(App::getClientId, clientId);
        return this.getOne(queryWrapper);
    }

    @TranRead
    public IPage<App> page(final Integer pageNum,
                           final Integer pageSize,
                           final String clientId,
                           final String name) {
        final Page<App> page = new Page<>(pageNum, pageSize);
        final QueryWrapper<App> queryWrapper = new QueryWrapper<>();
        final LambdaQueryWrapper<App> lambda = queryWrapper.lambda();
        if (!ObjectUtils.isEmpty(name)) {
            lambda.like(App::getName, name);
        }
        if (!ObjectUtils.isEmpty(clientId)) {
            lambda.eq(App::getClientId, clientId);
        }
        return this.page(page, queryWrapper);
    }
}