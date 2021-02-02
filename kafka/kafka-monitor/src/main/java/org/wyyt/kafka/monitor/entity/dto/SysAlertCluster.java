package org.wyyt.kafka.monitor.entity.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.wyyt.admin.ui.entity.base.BaseDto;

/**
 * The entity for table `sys_alert_cluster`. Using for throw a alerm when there's something wrong in cluster.
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
@TableName(value = "`sys_alert_cluster`")
public class SysAlertCluster extends BaseDto {
    /**
     * 集群主机类型(1. zookeeper; 2: kafka);参见SysAlertCluster.Type类型
     */
    @TableField(value = "`type`")
    private Integer type;

    /**
     * 服务器地址
     */
    @TableField(value = "`server`")
    private String server;

    /**
     * 警报邮件的发送地址
     */
    @TableField(value = "`email`")
    private String email;

    /**
     * 钉钉机器人的access_token
     */
    @TableField(value = "`access_token`")
    private String accessToken;

    /**
     * 钉钉机器人的加签秘钥
     */
    @TableField(value = "`secret`")
    private String secret;

    @Getter
    public enum Type {
        ZOOKEEPER(1, "ZOOKEEPER"),
        KAFKA(2, "KAFKA");

        private final int code;
        private final String description;

        Type(final int code, final String description) {
            this.code = code;
            this.description = description;
        }

        public static Type get(final Integer code) {
            for (final Type item : Type.values()) {
                if (item.getCode() == code) {
                    return item;
                }
            }
            return null;
        }

        public static Type get(final String description) {
            for (final Type item : Type.values()) {
                if (item.getDescription().equals(description)) {
                    return item;
                }
            }
            return null;
        }
    }
}