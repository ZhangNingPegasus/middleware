package org.wyyt.kafka.monitor.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.wyyt.kafka.monitor.anno.TranRead;
import org.wyyt.kafka.monitor.anno.TranSave;
import org.wyyt.kafka.monitor.entity.dto.SysAlertCluster;
import org.wyyt.kafka.monitor.mapper.SysAlertClusterMapper;

import java.util.List;

/**
 * The service for table 'sys_alert_cluster'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Service
public class SysAlertClusterService extends ServiceImpl<SysAlertClusterMapper, SysAlertCluster> {

    @TranRead
    public List<SysAlertCluster> getByType(SysAlertCluster.Type type) {
        QueryWrapper<SysAlertCluster> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysAlertCluster::getType, type.getCode()).orderByAsc(SysAlertCluster::getServer);
        return this.list(queryWrapper);
    }

    @TranSave
    public boolean save(final Integer type,
                        final String server,
                        final String email,
                        final String accessToken,
                        final String secret) {
        final SysAlertCluster sysAlertCluster = new SysAlertCluster();
        sysAlertCluster.setType(type);
        sysAlertCluster.setServer(server);
        sysAlertCluster.setEmail(email);
        sysAlertCluster.setAccessToken(accessToken);
        sysAlertCluster.setSecret(secret);
        return this.save(sysAlertCluster);
    }

    @TranSave
    public boolean update(final Long id,
                          final Integer type,
                          final String server,
                          final String email,
                          final String accessToken,
                          final String secret) {
        final UpdateWrapper<SysAlertCluster> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().eq(SysAlertCluster::getId, id)
                .set(SysAlertCluster::getType, type)
                .set(SysAlertCluster::getServer, server)
                .set(SysAlertCluster::getEmail, email)
                .set(SysAlertCluster::getAccessToken, accessToken)
                .set(SysAlertCluster::getSecret, secret);
        return this.update(updateWrapper);
    }
}