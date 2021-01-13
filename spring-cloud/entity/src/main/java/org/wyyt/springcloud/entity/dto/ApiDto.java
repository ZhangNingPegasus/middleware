package org.wyyt.springcloud.entity.dto;

import lombok.Data;

/**
 * The DTO of entity API
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Data
public class ApiDto {
    private String name;
    private String method;
    private String description;
    private String serviceId;
    private String path;
    private String className;
    private String methodName;
    private String parameterTypes;
}