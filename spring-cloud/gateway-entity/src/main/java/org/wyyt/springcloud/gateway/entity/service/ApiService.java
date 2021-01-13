package org.wyyt.springcloud.gateway.entity.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.wyyt.springcloud.gateway.entity.entity.Api;
import org.wyyt.springcloud.gateway.entity.mapper.ApiMapper;

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
public class ApiService extends ServiceImpl<ApiMapper, Api> {
}