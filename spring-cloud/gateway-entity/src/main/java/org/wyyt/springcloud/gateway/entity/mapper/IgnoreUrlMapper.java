package org.wyyt.springcloud.gateway.entity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.wyyt.springcloud.gateway.entity.entity.IgnoreUrl;

/**
 * The mapper of table `t_ignore_url`
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Mapper
public interface IgnoreUrlMapper extends BaseMapper<IgnoreUrl> {

}