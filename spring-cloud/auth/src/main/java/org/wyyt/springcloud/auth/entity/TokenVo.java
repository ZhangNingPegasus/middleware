package org.wyyt.springcloud.auth.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * The entity model of Login
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
@ApiModel(description = "AccessToken参数")
public class TokenVo implements Serializable {
    @ApiModelProperty("应用ID, 同Client Id")
    private String apiKey;

    @ApiModelProperty("应用secret, 同Client Secret")
    private String secretKey;

    @ApiModelProperty("应用ID, 同Client Id")
    private String appId;
}
