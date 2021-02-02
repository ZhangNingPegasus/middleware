package org.wyyt.sharding.db2es.admin.entity.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wyyt.sharding.db2es.core.entity.persistent.BaseDto;

/**
 * The View Object for K-V
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
public final class TopicDb2EsVo extends BaseDto {
    private static final long serialVersionUID = 1L;

    @TableField(value = "`db2es_id`")
    private Long db2esId;

    @TableField(value = "`name`")
    private String name;

    @TableField(exist = false)
    private Boolean isActive;
}