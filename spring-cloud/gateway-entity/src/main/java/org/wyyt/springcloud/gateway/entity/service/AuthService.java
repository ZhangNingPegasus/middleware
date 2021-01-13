package org.wyyt.springcloud.gateway.entity.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
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
    @TranRead
    public List<Api> getApiByClientId(String clientId) {
        return this.baseMapper.getApiByClientId(clientId);
    }
}