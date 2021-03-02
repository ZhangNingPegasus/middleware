package org.wyyt.springcloud.entity;

import lombok.Data;
import lombok.ToString;

/**
 * The View Object of App
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
@ToString
public class AppVo {
    private String clientId;
    private String name;
    private Boolean isAdmin;
    private String description;
}