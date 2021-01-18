package org.wyyt.kafka.monitor.alert;

import org.springframework.stereotype.Service;
import org.wyyt.kafka.monitor.entity.po.Alert;
import org.wyyt.tool.cache.CacheService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * The service for providing an alert when a problem is detected.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class AlertService {
    private static final int MAX_SIZE = 1024;
    private final ArrayBlockingQueue<Alert> alertList;
    private final CacheService cacheService;

    public AlertService(final CacheService cacheService) {
        this.cacheService = cacheService;
        this.alertList = new ArrayBlockingQueue<>(MAX_SIZE);
    }

    public void offer(final String id,
                      final Alert alert) {
        final Object val = this.cacheService.get(id);
        if (null == val) {
            this.cacheService.put(id, alert);
            this.alertList.offer(alert);
        }
    }

    public List<Alert> getAll() {
        final List<Alert> result = new ArrayList<>(MAX_SIZE);
        this.alertList.drainTo(result, MAX_SIZE);
        return result;
    }
}