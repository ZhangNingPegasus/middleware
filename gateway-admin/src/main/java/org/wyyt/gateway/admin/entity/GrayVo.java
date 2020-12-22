package org.wyyt.gateway.admin.entity;

import lombok.Data;

/**
 * The View Object of Gray
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Data
public class GrayVo {
    private String id;
    private String value;
    private Integer weight;
    private String description;
}
