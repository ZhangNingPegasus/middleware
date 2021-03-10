package org.wyyt.springcloud.gateway.entity.entity.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * The entity of access token
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
@JsonSerialize
@ApiModel(description = "授权Token")
public class AccessToken implements Serializable {
    @ApiModelProperty("token内容")
    private String accessToken;
    @ApiModelProperty("过期时间,单位:秒")
    private Long expiresTime;
}