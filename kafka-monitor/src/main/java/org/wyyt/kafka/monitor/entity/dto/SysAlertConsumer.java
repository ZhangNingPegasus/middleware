package org.wyyt.kafka.monitor.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.wyyt.kafka.monitor.entity.base.BaseDto;

/**
 * The entity for table `sys_alert_consumer`. Using for throw an alerm when there's something wrong in consumer.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "`sys_alert_consumer`")
public class SysAlertConsumer extends BaseDto {
    /**
     * 消费组名称
     */
    @TableField(value = "`group_id`")
    private String groupId;

    /**
     * 消费组对应的主题名称
     */
    @TableField(value = "`topic_name`")
    private String topicName;

    /**
     * 消息积压的数量阀值，超过这个阀值则会触发报警
     */
    @TableField(value = "`lag_threshold`")
    private Long lagThreshold;

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
}