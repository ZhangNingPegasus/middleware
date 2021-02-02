package org.wyyt.springcloud.gateway.entity.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Getter;
import org.wyyt.springcloud.gateway.entity.entity.base.BaseDto;

import java.util.Objects;

/**
 * The entity of table `t_gray`
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
@TableName(value = "`t_api`")
public class Api extends BaseDto {
    @TableField(value = "`name`")
    private String name;

    @TableField(value = "`method`")
    private String method;

    @TableField(value = "`service_name`")
    private String serviceName;

    @TableField(value = "`path`")
    private String path;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Api api = (Api) o;
        return name.equals(api.name) && method.equals(api.method) && serviceName.equals(api.serviceName) && path.equals(api.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, method, serviceName, path);
    }

    @Data
    public static class Result {
        private Integer insertNum = 0;
        private Integer updateNum = 0;
    }

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