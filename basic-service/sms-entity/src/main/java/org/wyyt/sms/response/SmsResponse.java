package org.wyyt.sms.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;
import org.wyyt.sms.enums.ProviderType;

/**
 * 发送短信返回值实体类
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@ToString
@Data
@ApiModel(description = "短信发送响应实体")
public class SmsResponse {
    /**
     * 短信发送后的唯一标识
     */
    @ApiModelProperty("短信发送后的唯一标识")
    private String id;

    /**
     * 短信提供商返回的响应编码
     */
    @ApiModelProperty("短信提供商返回的响应编码")
    private String code;

    /**
     * 发送方法是否调用成功(调用成功不一定代表发送成功)
     */
    @ApiModelProperty("发送方法是否调用成功(调用成功不一定代表发送成功)")
    private Boolean ok;

    /**
     * 异常错误信息
     */
    @ApiModelProperty("异常错误信息")
    private String errorMsg;

    /**
     * 短信服务商类型
     */
    @ApiModelProperty("短信服务商类型")
    private ProviderType providerType;
}