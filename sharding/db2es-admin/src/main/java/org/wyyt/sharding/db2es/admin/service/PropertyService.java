package org.wyyt.sharding.db2es.admin.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.wyyt.sharding.db2es.admin.mapper.PropertyMapper;
import org.wyyt.sharding.db2es.admin.service.common.Db2EsHttpService;
import org.wyyt.sharding.db2es.core.entity.domain.Config;
import org.wyyt.sharding.db2es.core.entity.persistent.Property;
import org.wyyt.sharding.db2es.core.util.CommonUtils;
import org.wyyt.tool.anno.TranRead;
import org.wyyt.tool.anno.TranSave;

/**
 * The service for table 't_property'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class PropertyService extends ServiceImpl<PropertyMapper, Property> {
    private final Db2EsHttpService db2EsHttpService;
    private final TableService tableService;

    public PropertyService(final Db2EsHttpService db2EsHttpService,
                           TableService tableService) {
        this.db2EsHttpService = db2EsHttpService;
        this.tableService = tableService;
    }

    @TranRead
    public Config getConfig() {
        final Config result = new Config();
        CommonUtils.fillConfig(this.list(), this.tableService.get(), result);
        return result;
    }

    public boolean editValue(final Long id,
                             final String value) throws Exception {
        this.edit(id, null, value, null);
        return this.db2EsHttpService.refreshDbConfig();
    }

    @TranSave
    public void edit(final Long id,
                     final String name,
                     final String value,
                     final String description) {
        final UpdateWrapper<Property> updateWrapper = new UpdateWrapper<>();
        final LambdaUpdateWrapper<Property> lambda = updateWrapper.lambda()
                .eq(Property::getId, id);

        if (null != name) {
            lambda.set(Property::getName, name);
        }
        if (null != value) {
            lambda.set(Property::getValue, value);
        }
        if (null != description) {
            lambda.set(Property::getDescription, description);
        }
        this.update(updateWrapper);
    }
}