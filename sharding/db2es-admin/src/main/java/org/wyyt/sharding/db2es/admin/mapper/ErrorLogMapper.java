package org.wyyt.sharding.db2es.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.wyyt.sharding.db2es.admin.entity.dto.ErrorLog;

/**
 * The mapper of table error_log
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Mapper
public interface ErrorLogMapper extends BaseMapper<ErrorLog> {
}