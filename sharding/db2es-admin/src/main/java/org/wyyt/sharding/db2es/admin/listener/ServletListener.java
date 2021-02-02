package org.wyyt.sharding.db2es.admin.listener;

import org.springframework.stereotype.Component;
import org.wyyt.sharding.auto.property.TableProperty;
import org.wyyt.sharding.db2es.admin.service.TableService;
import org.wyyt.sharding.db2es.core.entity.domain.TableInfo;
import org.wyyt.sharding.db2es.core.entity.domain.TableMap;
import org.wyyt.sharding.db2es.core.entity.domain.TableNameInfo;
import org.wyyt.sharding.service.ShardingService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The servlet context listener
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Component
public class ServletListener implements ServletContextListener {
    private final ShardingService shardingService;
    private final TableService tableService;

    public ServletListener(final ShardingService shardingService,
                           final TableService tableService) {
        this.shardingService = shardingService;
        this.tableService = tableService;
    }

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        TableMap tableMap = new TableMap();

        for (final TableProperty tableProperty : this.shardingService.listTableProperties()) {
            String logicTableName = tableProperty.getName();
            String pkName = tableProperty.getPkName();
            String rowCreateTime = tableProperty.getRowCreateTime();
            String rowUpdateTime = tableProperty.getRowUpdateTime();
            Set<String> factTableNameSet = new HashSet<>();

            for (final Map.Entry<String, TableProperty.DimensionInfo> pair : tableProperty.getDimensionInfos().entrySet()) {
                int tableCountNum = pair.getValue().getTableCountNum();
                for (int i = 0; i < tableCountNum; i++) {
                    factTableNameSet.add(String.format(pair.getValue().getTableNameFormat(), i));
                }
            }
            tableMap.put(new TableNameInfo(logicTableName, factTableNameSet), new TableInfo(pkName, rowCreateTime, rowUpdateTime));
        }
        this.tableService.save(tableMap);
    }
}