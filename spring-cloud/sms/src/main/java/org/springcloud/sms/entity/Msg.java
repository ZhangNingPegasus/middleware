package org.springcloud.sms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springcloud.sms.entity.base.BaseDto;
import org.springcloud.sms.entity.enums.SendStatus;

import java.util.Date;

/**
 * The entity of table `t_msg`
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
@TableName(value = "`t_msg`")
public class Msg extends BaseDto {

    /**
     * 短信发送后的唯一标识
     */
    @TableField(value = "`msg_id`")
    private String msgId;

    /**
     * 发送方手机号
     */
    @TableField(value = "phone_number")
    private String phoneNumber;

    /**
     * 短信内容
     */
    @TableField(value = "`content`")
    private String content;

    /**
     * 短信签名编码
     */
    @TableField(value = "`sign_code`")
    private String signCode;

    /**
     * 短信模板编码
     */
    @TableField(value = "`template_code`")
    private String templateCode;

    /**
     * 短信模板参数, 必须是json格式
     */
    @TableField(value = "`template_param`")
    private String templateParam;

    /**
     * 短信提供商, 请参考{@link org.wyyt.sms.enums.ProviderType}
     */
    @TableField(value = "`provider`")
    private Integer provider;

    /**
     * 发送短信的主机ip
     */
    @TableField(value = "`send_ip`")
    private String sendIp;

    /**
     * 短信发送时间
     */
    @TableField(value = "`send_time`")
    private Date sendTime;

    /**
     * 短信接受时间
     */
    @TableField(value = "`receive_time`")
    private Date receiveTime;

    /**
     * 发送状态, 请参考{@link SendStatus}
     */
    @TableField(value = "`send_status`")
    private Integer sendStatus;

    /**
     * 扩展信息
     */
    @TableField(value = "`extra`")
    private String extra;
}