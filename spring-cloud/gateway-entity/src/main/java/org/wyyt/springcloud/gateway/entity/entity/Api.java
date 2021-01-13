package org.wyyt.springcloud.gateway.entity.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.wyyt.springcloud.gateway.entity.entity.base.BaseDto;

/**
 * The entity of table `t_gray`
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "`t_api`")
public class Api extends BaseDto {
    @TableField(value = "`name`")
    private String name;

    @TableField(value = "`method`")
    private String method;

    @TableField(value = "`description`")
    private String description;

    @TableField(value = "`service_id`")
    private String serviceId;

    @TableField(value = "`path`")
    private String path;

    @TableField(value = "`status`")
    private Integer status;

    @TableField(value = "`class_name`")
    private String className;

    @TableField(value = "`method_name`")
    private String methodName;

    @Getter
    public enum Status {
        DISABLE(0, "DISABLE"),
        ENABLE(1, "ENABLE");

        private final int code;
        private final String description;

        Status(final int code, final String description) {
            this.code = code;
            this.description = description;
        }

        public static Status get(final Integer code) {
            for (final Status item : Status.values()) {
                if (item.getCode() == code) {
                    return item;
                }
            }
            return null;
        }

        public static Status get(final String description) {
            for (final Status item : Status.values()) {
                if (item.getDescription().equals(description)) {
                    return item;
                }
            }
            return null;
        }
    }
}