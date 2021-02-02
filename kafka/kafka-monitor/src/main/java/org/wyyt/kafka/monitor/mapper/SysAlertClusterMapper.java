package org.wyyt.kafka.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.wyyt.kafka.monitor.entity.dto.SysAlertCluster;

/**
 * The mapper for table 'sys_alert_cluster'.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Mapper
public interface SysAlertClusterMapper extends BaseMapper<SysAlertCluster> {
}