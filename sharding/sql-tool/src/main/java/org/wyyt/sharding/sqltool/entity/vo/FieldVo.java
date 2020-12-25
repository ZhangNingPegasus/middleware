package org.wyyt.sharding.sqltool.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;

/**
 * The entity of table field
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Data
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public final class FieldVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String type;
    private Integer length;
    private Integer decimal;
    private Boolean notNull;
    private Boolean isPrimary;
    private Boolean autoUpdateByTimestampt;
    private Boolean unsigned;
    private Boolean autoIncrement;
    private String defaultValue;
    private String comment;
}