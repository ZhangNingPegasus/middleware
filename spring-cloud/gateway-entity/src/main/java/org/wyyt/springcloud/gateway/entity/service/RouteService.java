package org.wyyt.springcloud.gateway.entity.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wyyt.springcloud.gateway.entity.anno.TranRead;
import org.wyyt.springcloud.gateway.entity.anno.TranSave;
import org.wyyt.springcloud.gateway.entity.entity.Route;
import org.wyyt.springcloud.gateway.entity.exception.GatewayException;
import org.wyyt.springcloud.gateway.entity.mapper.RouteMapper;

import java.util.List;
import java.util.Set;

/**
 * The service of `t_route` table
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class RouteService extends ServiceImpl<RouteMapper, Route> {
    @TranRead
    public IPage<Route> page(final String description,
                             final Integer pageNum,
                             final Integer pageSize) {
        final QueryWrapper<Route> queryWrapper = new QueryWrapper<>();
        if (!ObjectUtils.isEmpty(description)) {
            queryWrapper.lambda().like(Route::getDescription, description);
        }
        return this.page(new Page<>(pageNum, pageSize), queryWrapper);
    }

    @TranRead
    public List<Route> listEnableRoutes() {
        final QueryWrapper<Route> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Route::getEnabled, true).orderByAsc(Route::getRowCreateTime);
        return this.list(queryWrapper);
    }

    @TranRead
    public Route getByRouteId(final String routeId) {
        final QueryWrapper<Route> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Route::getRouteId, routeId);
        return this.getOne(queryWrapper);
    }

    @TranSave
    public void add(final String description,
                    final String uri,
                    final String predicate,
                    final String filter,
                    final Integer orderNum,
                    final String serviceName,
                    final Boolean enabled) {
        final String routeId = RandomStringUtils.randomAlphanumeric(15);
        Route route = this.getByRouteId(routeId);
        if (null != route) {
            throw new GatewayException(String.format("RouteId=%s的路由已存在", routeId));
        }
        route = new Route();
        route.setRouteId(routeId);
        route.setDescription(description);
        route.setUri(uri);
        route.setPredicates(predicate);
        route.setFilters(filter);
        route.setOrderNum(orderNum);
        route.setServiceName(serviceName);
        route.setEnabled(enabled);
        this.save(route);
    }

    @TranSave
    public void edit(final long id,
                     final String description,
                     final String uri,
                     final String predicate,
                     final String filter,
                     final Integer orderNum,
                     final String serviceName,
                     final Boolean enabled) {
        final Route route = this.getById(id);
        if (null == route) {
            throw new GatewayException(String.format("路由(id=%s)不存在", id));
        }
        route.setDescription(description);
        route.setUri(uri);
        route.setPredicates(predicate);
        route.setFilters(filter);
        route.setOrderNum(orderNum);
        route.setServiceName(serviceName);
        route.setEnabled(enabled);
        this.updateById(route);
    }

    @TranSave
    public void delete(final Set<Long> ids) {
        if (null == ids || ids.isEmpty()) {
            return;
        }
        this.removeByIds(ids);
    }

    @TranSave
    public void enable(final long id,
                       final boolean enable) {
        final Route route = this.getById(id);
        if (null == route) {
            throw new GatewayException(String.format("路由(id=%s)不存在", id));
        }
        route.setEnabled(enable);
        this.updateById(route);
    }
}