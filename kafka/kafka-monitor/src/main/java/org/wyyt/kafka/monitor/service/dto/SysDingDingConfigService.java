package org.wyyt.kafka.monitor.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.wyyt.kafka.monitor.anno.TranRead;
import org.wyyt.kafka.monitor.anno.TranSave;
import org.wyyt.kafka.monitor.entity.dto.SysDingDingConfig;
import org.wyyt.kafka.monitor.mapper.SysDingDingConfigMapper;

import java.util.List;

/**
 * The service for table 'sys_dingding_config'.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class SysDingDingConfigService extends ServiceImpl<SysDingDingConfigMapper, SysDingDingConfig> {

    @TranSave
    public int save(final String accessToken,
                    final String secret) {
        final QueryWrapper<SysDingDingConfig> queryWrapper = new QueryWrapper<>();
        this.baseMapper.delete(queryWrapper);
        final SysDingDingConfig sysDingDingConfig = new SysDingDingConfig();
        sysDingDingConfig.setAccessToken(accessToken);
        sysDingDingConfig.setSecret(secret);
        return this.baseMapper.insert(sysDingDingConfig);
    }

    @TranRead
    public SysDingDingConfig get() {
        final QueryWrapper<SysDingDingConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.last("LIMIT 1");
        final List<SysDingDingConfig> list = this.list(queryWrapper);
        if (null != list && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }
}