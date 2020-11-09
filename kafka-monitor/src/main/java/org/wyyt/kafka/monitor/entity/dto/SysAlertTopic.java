package org.wyyt.kafka.monitor.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wyyt.kafka.monitor.entity.base.BaseDto;

/**
 * The entity for table `sys_alert_topic`. Using for throw a alerm when there's something wrong in topic.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "`sys_alert_topic`")
public class SysAlertTopic extends BaseDto {
    @TableField(value = "`topic_name`")
    private String topicName;

    @TableField(value = "`from_time`")
    private String fromTime;

    @TableField(value = "`to_time`")
    private String toTime;

    @TableField(value = "`from_tps`")
    private Integer fromTps;

    @TableField(value = "`to_tps`")
    private Integer toTps;

    @TableField(value = "`from_mom_tps`")
    private Integer fromMomTps;

    @TableField(value = "`to_mom_tps`")
    private Integer toMomTps;

    @TableField(value = "`email`")
    private String email;

    @TableField(value = "`access_token`")
    private String accessToken;

    @TableField(value = "`secret`")
    private String secret;

    public String toInfo() {
        return String.format("TPS范围: [%s, %s]; 变化范围: [%s, %s]",
                (null == this.fromTps) ? "-∞" : this.fromTps,
                (null == this.toTps) ? "+∞" : this.toTps,
                (null == this.fromMomTps) ? "-∞" : this.fromMomTps,
                (null == this.toMomTps) ? "+∞" : this.toMomTps
        );
    }

    public String toTpsInfo() {
        return String.format("[%s, %s]",
                (null == this.fromTps) ? "-∞" : this.fromTps,
                (null == this.toTps) ? "+∞" : this.toTps);
    }

    public String toMomTpsInfo() {
        return String.format("[%s, %s]",
                (null == this.fromMomTps) ? "-∞" : this.fromMomTps,
                (null == this.toMomTps) ? "+∞" : this.toMomTps);
    }
}