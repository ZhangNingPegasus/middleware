package org.wyyt.sharding.db2es.admin.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.wyyt.sharding.db2es.admin.mapper.TableMapper;
import org.wyyt.sharding.db2es.core.entity.domain.TableMap;
import org.wyyt.sharding.db2es.core.entity.persistent.Table;
import org.wyyt.tool.anno.TranRead;
import org.wyyt.tool.anno.TranSave;

import java.util.List;

/**
 * The service for table 't_table'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class TableService extends ServiceImpl<TableMapper, Table> {
    @TranSave
    public void save(final TableMap tableMap) {
        this.baseMapper.clear();
        final Table table = new Table();
        table.setInfo(JSON.toJSONString(tableMap));
        this.save(table);
    }

    @TranRead
    public TableMap get() {
        final List<Table> tableList = this.list();
        for (final Table table : tableList) {
            return JSON.parseObject(table.getInfo(), TableMap.class);
        }
        return null;
    }
}