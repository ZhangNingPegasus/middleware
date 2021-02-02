package org.wyyt.sharding.db2es.core.entity.view;

import lombok.Data;

/**
 * the view entity of database's connection string
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
public final class DbVo {
    private String host;
    private String uid;
    private String pwd;
    private String port;
    private String dbName;
}