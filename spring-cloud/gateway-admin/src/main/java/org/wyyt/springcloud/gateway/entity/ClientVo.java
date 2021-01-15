package org.wyyt.springcloud.gateway.entity;

import lombok.Data;

/**
 * The View Object of Client
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021        Initialize  *
 * *****************************************************************
 */
@Data
public class ClientVo {
    private String clientId;
    private String clientSecret;
}
