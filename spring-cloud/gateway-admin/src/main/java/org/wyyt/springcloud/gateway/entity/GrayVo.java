package org.wyyt.springcloud.gateway.entity;

import lombok.Data;

/**
 * The View Object of Gray
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
public class GrayVo {
    private String grayId;
    private String value;
    private Integer weight;
    private String description;
}
