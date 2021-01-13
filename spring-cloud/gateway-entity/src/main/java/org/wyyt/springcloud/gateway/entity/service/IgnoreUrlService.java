package org.wyyt.springcloud.gateway.entity.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.wyyt.springcloud.gateway.entity.anno.TranRead;
import org.wyyt.springcloud.gateway.entity.entity.IgnoreUrl;
import org.wyyt.springcloud.gateway.entity.mapper.IgnoreUrlMapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The service of `t_ignore_url` table
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Service
public class IgnoreUrlService extends ServiceImpl<IgnoreUrlMapper, IgnoreUrl> {
    @TranRead
    public Set<String> getUrls() {
        final List<IgnoreUrl> ignoreUrls = this.list();
        return ignoreUrls.stream().map(p -> p.getUrl()).collect(Collectors.toSet());
    }
}