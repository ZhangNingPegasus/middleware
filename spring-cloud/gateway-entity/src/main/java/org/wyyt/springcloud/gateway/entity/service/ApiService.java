package org.wyyt.springcloud.gateway.entity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wyyt.springcloud.gateway.entity.anno.TranRead;
import org.wyyt.springcloud.gateway.entity.anno.TranSave;
import org.wyyt.springcloud.gateway.entity.entity.Api;
import org.wyyt.springcloud.gateway.entity.mapper.ApiMapper;

import java.util.ArrayList;
import java.util.List;

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
public class ApiService extends ServiceImpl<ApiMapper, Api> {
    protected final AuthService authService;

    public ApiService(final AuthService authService) {
        this.authService = authService;
    }

    @TranRead
    public IPage<Api> page(final Integer pageNum,
                           final Integer pageSize,
                           final String name,
                           final String serviceName,
                           final String path) {
        final Page<Api> page = new Page<>(pageNum, pageSize);
        final QueryWrapper<Api> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<Api> lambda = queryWrapper.lambda();
        if (!ObjectUtils.isEmpty(name)) {
            lambda.like(Api::getName, name);
        }
        if (!ObjectUtils.isEmpty(serviceName)) {
            lambda.eq(Api::getServiceName, serviceName);
        }
        if (!ObjectUtils.isEmpty(path)) {
            lambda.like(Api::getPath, path);
        }
        return this.page(page, queryWrapper);
    }

    @TranRead
    public Api getByServiceNameAndPath(final String serviceName,
                                       final String path) {
        if (ObjectUtils.isEmpty(serviceName) || ObjectUtils.isEmpty(path)) {
            return null;
        }

        final QueryWrapper<Api> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Api::getServiceName, serviceName)
                .eq(Api::getPath, path);
        return this.getOne(queryWrapper);
    }

    @TranSave
    public Api.Result save(final List<Api> apiList) {
        final Api.Result result = new Api.Result();
        if (null == apiList || apiList.isEmpty()) {
            return result;
        }

        final List<Api> insertList = new ArrayList<>();
        final List<Api> updateList = new ArrayList<>();

        for (final Api api : apiList) {
            final Api dbApi = this.getByServiceNameAndPath(api.getServiceName(), api.getPath());
            if (null == dbApi) {
                insertList.add(api);
            } else if (!dbApi.equals(api)) {
                api.setId(dbApi.getId());
                updateList.add(api);
            }
        }

        if (!insertList.isEmpty()) {
            this.saveBatch(insertList, insertList.size());
            result.setInsertNum(insertList.size());
        }
        if (!updateList.isEmpty()) {
            this.updateBatchById(updateList, updateList.size());
            result.setUpdateNum(updateList.size());
        }
        return result;
    }

    @TranRead
    public List<String> listServiceNames() {
        return this.baseMapper.listServiceNames();
    }
}