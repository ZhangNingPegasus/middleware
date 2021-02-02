package org.wyyt.springcloud.gateway.entity;

import lombok.Data;

/**
 * The View Object of Inspect
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
public class InspectVo {
    private String service;
    private String version;
}