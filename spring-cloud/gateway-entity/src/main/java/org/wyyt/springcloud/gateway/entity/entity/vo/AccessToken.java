package org.wyyt.springcloud.gateway.entity.entity.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
public class AccessToken implements Serializable {
    private String accessToken;
    private Long expiresTime;
}