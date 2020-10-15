package org.wyyt.db2es.core.entity.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

/**
 * the view entity for leader of db2es-client
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public final class NodeVo {
    private Integer id;
    private String name;
    private String ip;
    private Integer port;
    private List<NodeVo> slaveList;

    public NodeVo(final String name,
                  final String ip,
                  final Integer port) {
        this(null, name, ip, port, null);
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeVo nodeVo = (NodeVo) o;
        return Objects.equals(id, nodeVo.id) &&
                Objects.equals(ip, nodeVo.ip) &&
                Objects.equals(port, nodeVo.port);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id, ip, port);
    }
}
