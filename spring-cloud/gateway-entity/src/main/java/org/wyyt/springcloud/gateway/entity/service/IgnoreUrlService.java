package org.wyyt.springcloud.gateway.entity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
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
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class IgnoreUrlService extends ServiceImpl<IgnoreUrlMapper, IgnoreUrl> {
    @TranRead
    public Set<String> getUrls() {
        final List<IgnoreUrl> ignoreUrls = this.list();
        return ignoreUrls.stream().map(IgnoreUrl::getUrl).collect(Collectors.toSet());
    }

    @TranRead
    public IgnoreUrl getByUrl(final String url) {
        if (ObjectUtils.isEmpty(url)) {
            return null;
        }
        final QueryWrapper<IgnoreUrl> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(IgnoreUrl::getUrl, url);
        return this.getOne(queryWrapper);
    }

    @TranRead
    public IPage<IgnoreUrl> page(final Integer pageNum,
                                 final Integer pageSize,
                                 final String url) {
        final Page<IgnoreUrl> page = new Page<>(pageNum, pageSize);
        final QueryWrapper<IgnoreUrl> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<IgnoreUrl> lambda = queryWrapper.lambda();
        if (!ObjectUtils.isEmpty(url)) {
            lambda.like(IgnoreUrl::getUrl, url);
        }
        return this.page(page, queryWrapper);
    }
}