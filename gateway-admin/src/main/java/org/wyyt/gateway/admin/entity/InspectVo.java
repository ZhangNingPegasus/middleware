package org.wyyt.gateway.admin.entity;

import lombok.Data;

/**
 * The View Object of Inspect
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Data
public class InspectVo {
    private String service;
    private String version;
}