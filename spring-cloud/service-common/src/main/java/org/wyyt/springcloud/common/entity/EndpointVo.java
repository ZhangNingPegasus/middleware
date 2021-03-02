package org.wyyt.springcloud.common.entity;

import lombok.Data;

/**
 * The View Object of Endpoint
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
public class EndpointVo {
    private String id;
    private String serviceName;
    private String address;
    private Integer port;
    private String version;
    private String group;
    private Boolean alive;
}