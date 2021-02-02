package org.wyyt.springcloud.gateway.service;

import org.springframework.stereotype.Service;
import org.wyyt.springcloud.gateway.entity.anno.TranSave;
import org.wyyt.springcloud.gateway.entity.entity.Api;
import org.wyyt.springcloud.gateway.entity.service.ApiService;

import java.util.Set;

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
public class ApiServiceImpl extends ApiService {
    private final AuthServiceImpl authServiceImpl;

    public ApiServiceImpl(final AuthServiceImpl authServiceImpl) {
        super(authServiceImpl);
        this.authServiceImpl = authServiceImpl;
    }

    @TranSave
    public void add(final String name,
                    final String method,
                    final String serviceName,
                    final String path) {
        Api api = this.getByServiceNameAndPath(serviceName, path);
        if (null != api) {
            throw new RuntimeException(String.format("接口[%s]在服务[%s]中已存在", path, serviceName));
        }
        api = new Api();
        api.setName(name);
        api.setMethod(method);
        api.setServiceName(serviceName);
        api.setPath(path);
        this.save(api);
    }

    @TranSave
    public void edit(final Long id,
                     final String name,
                     final String method,
                     final String serviceName,
                     final String path) {
        final Api api = this.getById(id);
        if (null == api) {
            return;
        }
        api.setName(name);
        api.setMethod(method);
        api.setServiceName(serviceName);
        api.setPath(path);
        this.updateById(api);
    }

    @TranSave
    public void del(final Set<Long> idSet) throws Exception {
        this.removeByIds(idSet);
        for (final Long apiId : idSet) {
            this.authServiceImpl.removeByApiId(apiId);
        }
    }

    @TranSave
    public void del(final Long apiId) throws Exception {
        this.removeById(apiId);
        this.authServiceImpl.removeByApiId(apiId);
    }
}