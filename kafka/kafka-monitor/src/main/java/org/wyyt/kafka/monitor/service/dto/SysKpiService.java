package org.wyyt.kafka.monitor.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.wyyt.kafka.monitor.anno.TranRead;
import org.wyyt.kafka.monitor.entity.dto.SysKpi;
import org.wyyt.kafka.monitor.mapper.SysKpiMapper;

import java.util.Date;
import java.util.List;

/**
 * The service for table 'sys_kpi'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class SysKpiService extends ServiceImpl<SysKpiMapper, SysKpi> {

    @TranRead
    public List<SysKpi> listKpi(final List<Integer> kpis,
                                final Date from,
                                final Date to) {
        final QueryWrapper<SysKpi> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .in(SysKpi::getKpi, kpis)
                .ge(SysKpi::getRowCreateTime, from)
                .le(SysKpi::getRowCreateTime, to)
                .orderByAsc(SysKpi::getRowCreateTime);
        return this.list(queryWrapper);
    }
}