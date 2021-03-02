package org.wyyt.springcloud.gateway.service;

import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.wyyt.springcloud.gateway.entity.contants.Constant;
import org.wyyt.springcloud.gateway.entity.entity.Route;
import org.wyyt.springcloud.gateway.entity.service.RouteService;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The service of dynamic route service
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class DynamicRouteService implements ApplicationEventPublisherAware {
    private ApplicationEventPublisher publisher;
    private final RouteService routeService;
    private final RouteDefinitionWriter routeDefinitionWriter;
    private final RouteDefinitionLocator routeDefinitionLocator;

    public DynamicRouteService(final @Lazy RouteService routeService,
                               final @Lazy RouteDefinitionWriter routeDefinitionWriter,
                               final @Lazy RouteDefinitionLocator routeDefinitionLocator) {
        this.routeService = routeService;
        this.routeDefinitionWriter = routeDefinitionWriter;
        this.routeDefinitionLocator = routeDefinitionLocator;
    }

    public Map<String, RouteDefinition> listRouteDefinition() {
        final Map<String, RouteDefinition> result = new HashMap<>();
        final Flux<RouteDefinition> routeDefinitions = this.routeDefinitionLocator.getRouteDefinitions();
        routeDefinitions.subscribe(routeDefinition -> result.put(routeDefinition.getId(), routeDefinition));
        return result;
    }

    public RouteDefinition toRouteDefinition(final Route route) {
        if (null == route) {
            return null;
        }

        final RouteDefinition result = new RouteDefinition();
        result.setId(route.getRouteId());
        result.setOrder(route.getOrderNum());
        result.setMetadata(new HashMap<>());
        result.getMetadata().put(Constant.SERVICE_NAME, route.getServiceName());

        final String strUri = route.getUri();
        URI uri;
        if (strUri.startsWith("http")) {
            uri = UriComponentsBuilder.fromHttpUrl(strUri).build().toUri();
        } else {
            uri = URI.create(strUri);
        }
        result.setUri(uri);

        final List<PredicateDefinition> predicateDefinitionList = new ArrayList<>();
        for (final String text : route.getPredicates().split(";")) {
            predicateDefinitionList.add(new PredicateDefinition(text));
        }
        result.setPredicates(predicateDefinitionList);

        final List<FilterDefinition> filterDefinitionList = new ArrayList<>();
        for (final String text : route.getFilters().split(";")) {
            filterDefinitionList.add(new FilterDefinition(text));
        }
        result.setFilters(filterDefinitionList);
        return result;
    }

    public void add(final RouteDefinition routeDefinition) {
        Disposable disposable = null;
        try {
            disposable = this.routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
        } finally {
            if (null != disposable) {
                disposable.dispose();
            }
        }
    }

    public void update(final RouteDefinition routeDefinition) {
        this.delete(routeDefinition.getId());
        this.add(routeDefinition);
    }

    public void delete(final String routeId) {
        Disposable disposable = null;
        try {
            disposable = this.routeDefinitionWriter.delete(Mono.just(routeId)).subscribe();
        } finally {
            if (null != disposable) {
                disposable.dispose();
            }
        }
    }

    public synchronized void publish() {
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
    }

    public synchronized void refresh() {
        final List<Route> newRouteList = this.routeService.listEnableRoutes();

        final Map<String, RouteDefinition> newRouteMap = newRouteList.stream().collect(Collectors.toMap(Route::getRouteId, this::toRouteDefinition));
        final Map<String, RouteDefinition> currentRouteMap = this.listRouteDefinition();
        final List<RouteDefinition> insertRouteDefinition = new ArrayList<>(newRouteMap.size());
        final List<RouteDefinition> updateRouteDefinition = new ArrayList<>(newRouteMap.size());
        final List<RouteDefinition> deleteRouteDefinition = new ArrayList<>(newRouteMap.size());

        for (final Map.Entry<String, RouteDefinition> pair : newRouteMap.entrySet()) {
            if (!currentRouteMap.containsKey(pair.getKey())) {
                insertRouteDefinition.add(pair.getValue());
            }
        }

        for (final Map.Entry<String, RouteDefinition> pair : newRouteMap.entrySet()) {
            if (currentRouteMap.containsKey(pair.getKey())) {
                final RouteDefinition currentRouteDefinition = currentRouteMap.get(pair.getKey());
                final RouteDefinition newRouteDefinition = pair.getValue();
                if (!currentRouteDefinition.equals(newRouteDefinition)) {
                    updateRouteDefinition.add(newRouteDefinition);
                }
            }
        }

        for (final Map.Entry<String, RouteDefinition> pair : currentRouteMap.entrySet()) {
            if (!newRouteMap.containsKey(pair.getKey())) {
                deleteRouteDefinition.add(pair.getValue());
            }
        }

        for (final RouteDefinition routeDefinition : insertRouteDefinition) {
            this.add(routeDefinition);
        }
        for (final RouteDefinition routeDefinition : updateRouteDefinition) {
            this.update(routeDefinition);
        }
        for (final RouteDefinition routeDefinition : deleteRouteDefinition) {
            this.delete(routeDefinition.getId());
        }

        if (!insertRouteDefinition.isEmpty() || !updateRouteDefinition.isEmpty() || !deleteRouteDefinition.isEmpty()) {
            this.publish();
        }
    }

    @Override
    public void setApplicationEventPublisher(final ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
}