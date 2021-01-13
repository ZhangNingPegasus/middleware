package org.wyyt.springcloud.gateway.entity.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Service
public class AppService extends ServiceImpl<AppMapper, App> {
    @TranRead
    public App getByClientId(final String clientId) {
        if (StringUtils.isEmpty(clientId)) {
            return null;
        }
        final QueryWrapper<App> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(App::getClientId, clientId);
        return this.getOne(queryWrapper);
    }
}