package org.wyyt.springcloud.gateway.entity.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;
import org.wyyt.springcloud.gateway.entity.entity.base.BaseDto;

/**
 * The entity of table `t_route`
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "`t_route`")
public class Route extends BaseDto {
    @TableField(value = "`route_id`")
    private String routeId;

    @TableField(value = "`uri`")
    private String uri;

    @TableField(value = "`predicates`")
    private String predicates;

    @TableField(value = "`filters`")
    private String filters;

    @TableField(value = "`order_num`")
    private Integer orderNum;

    @TableField(value = "`enabled`")
    private Boolean enabled;

    @TableField(value = "`description`")
    private String description;

    @TableField(exist = false)
    private String serviceName;

    public String getPathPredicate() {
        if (StringUtils.isEmpty(this.predicates)) {
            return "";
        }

        return predicates.replaceAll("Path=/", "").replace("/**", "");
    }
}