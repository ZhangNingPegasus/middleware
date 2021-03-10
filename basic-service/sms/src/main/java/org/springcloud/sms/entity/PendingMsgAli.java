package org.springcloud.sms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springcloud.sms.entity.base.BaseDto;
import org.springcloud.sms.entity.enums.SendStatus;

import java.util.Date;

/**
 * The entity of table `t_pending_msg_ali`
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
@TableName(value = "`t_pending_msg_ali`")
public class PendingMsgAli extends BaseDto {
    /**
     * 短信发送后的唯一标识
     */
    @TableField(value = "`msg_id`")
    private String msgId;

    /**
     * 短信发送后的唯一标识
     */
    @TableField(value = "`phone_number`")
    private String phoneNumber;
}