package org.wyyt.sharding.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * Index information of the table
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Data
public final class IndexInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String indexName;
    private String fieldName;
    private String type;
    private String method;
    private String comment;
}