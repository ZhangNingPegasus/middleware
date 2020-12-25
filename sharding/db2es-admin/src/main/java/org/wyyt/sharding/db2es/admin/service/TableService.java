package org.wyyt.sharding.db2es.admin.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.wyyt.sharding.db2es.admin.mapper.TableMapper;
import org.wyyt.sharding.db2es.core.entity.domain.TableMap;
import org.wyyt.sharding.db2es.core.entity.persistent.Table;
import org.wyyt.sharding.anno.TranRead;
import org.wyyt.sharding.anno.TranSave;

import java.util.List;

/**
 * The service for table 't_table'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Service
public class TableService extends ServiceImpl<TableMapper, Table> {
    @TranSave
    public void save(TableMap tableMap) {
        this.baseMapper.clear();
        Table table = new Table();
        table.setInfo(JSON.toJSONString(tableMap));
        this.save(table);
    }

    @TranRead
    public TableMap get() {
        List<Table> tableList = this.list();
        for (Table table : tableList) {
            return JSON.parseObject(table.getInfo(), TableMap.class);
        }
        return null;
    }
}