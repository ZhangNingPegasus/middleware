package org.wyyt.sharding.db2es.admin.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * The view object of sharding data source
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class DataSourceVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String host;
    private Integer port;
    private String uid;
    private String pwd;
    private String databaseName;
    private String tableNames;

    @Override
    public final boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataSourceVo that = (DataSourceVo) o;
        return host.equals(that.host) &&
                port.equals(that.port) &&
                uid.equals(that.uid) &&
                pwd.equals(that.pwd) &&
                databaseName.equals(that.databaseName);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(host, port, uid, pwd, databaseName);
    }
}