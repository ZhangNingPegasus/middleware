package org.wyyt.sharding.sqltool.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * The view object of tree data
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public final class TreeVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String field;
    private Boolean spread;
    private String dimension;
    private String datasource;
    private String table;
    private List<TreeVo> children;
}