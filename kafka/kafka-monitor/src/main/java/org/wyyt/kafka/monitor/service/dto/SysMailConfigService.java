package org.wyyt.kafka.monitor.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.wyyt.kafka.monitor.anno.TranRead;
import org.wyyt.kafka.monitor.anno.TranSave;
import org.wyyt.kafka.monitor.entity.dto.SysMailConfig;
import org.wyyt.kafka.monitor.mapper.SysMailConfigMapper;

import java.util.List;

/**
 * The service for table 'sys_mail_config'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Service
public class SysMailConfigService extends ServiceImpl<SysMailConfigMapper, SysMailConfig> {
    @TranSave
    public int save(final String host,
                    final String port,
                    final String username,
                    final String password) {
        final QueryWrapper<SysMailConfig> queryWrapper = new QueryWrapper<>();
        this.baseMapper.delete(queryWrapper);
        final SysMailConfig sysMailConfig = new SysMailConfig();
        sysMailConfig.setHost(host);
        sysMailConfig.setPort(port);
        sysMailConfig.setUsername(username);
        sysMailConfig.setPassword(password);
        return this.baseMapper.insert(sysMailConfig);
    }

    @TranRead
    public SysMailConfig get() {
        final List<SysMailConfig> list = this.list();
        if (null != list && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }
}