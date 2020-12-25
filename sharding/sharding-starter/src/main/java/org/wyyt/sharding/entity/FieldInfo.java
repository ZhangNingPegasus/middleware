package org.wyyt.sharding.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * meta data information of table
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Data
public final class FieldInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String dataType;
    private String typeDesc;
    private String type;
    private Integer size;
    private Integer decimal;
    private Boolean notNull;
    private Boolean isPrimary;
    private Boolean autoUpdateByTimestampt;
    private Boolean unsigned;
    private Boolean autoIncrement;
    private String defaultValue;
    private String key;
    private String comment;
}