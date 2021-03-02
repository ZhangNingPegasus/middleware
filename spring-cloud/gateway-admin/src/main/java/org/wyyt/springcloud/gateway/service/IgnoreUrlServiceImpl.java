package org.wyyt.springcloud.gateway.service;

import org.springframework.stereotype.Service;
import org.wyyt.redis.service.RedisService;
import org.wyyt.springcloud.gateway.entity.anno.TranSave;
import org.wyyt.springcloud.gateway.entity.contants.Constant;
import org.wyyt.springcloud.gateway.entity.entity.IgnoreUrl;
import org.wyyt.springcloud.gateway.entity.service.IgnoreUrlService;

import java.util.Set;

/**
 * The service of `t_ignore_url` table
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class IgnoreUrlServiceImpl extends IgnoreUrlService {
    private final RedisService redisService;
    private final GatewayRpcService gatewayRpcService;

    public IgnoreUrlServiceImpl(final RedisService redisService,
                                final GatewayRpcService gatewayRpcService) {
        this.redisService = redisService;
        this.gatewayRpcService = gatewayRpcService;
    }

    @TranSave
    public void add(final String url,
                    final String description) throws Exception {

        IgnoreUrl ignoreUrl = this.getByUrl(url);
        if (null != ignoreUrl) {
            throw new RuntimeException(String.format("白名单[%s]已存在", url));
        }

        ignoreUrl = new IgnoreUrl();
        ignoreUrl.setUrl(url);
        ignoreUrl.setDescription(description);
        this.save(ignoreUrl);
        this.removeRedis();
    }


    @TranSave
    public void edit(final Long id,
                     final String url,
                     final String description) throws Exception {
        final IgnoreUrl ignoreUrl = this.getById(id);
        if (null == ignoreUrl) {
            throw new RuntimeException(String.format("Url[id=%s]不存在", id));
        }
        ignoreUrl.setUrl(url);
        ignoreUrl.setDescription(description);
        this.updateById(ignoreUrl);
        this.removeRedis();
    }

    @TranSave
    public void del(final Set<Long> idSet) throws Exception {
        this.removeByIds(idSet);
        this.removeRedis();
    }

    private void removeRedis() throws Exception {
        this.redisService.del(Constant.REDIS_IGNORE_URLS_KEY);
        this.gatewayRpcService.removeIgnoreUrlSetLocalCache();
    }
}