package org.wyyt.springcloud.gateway.entity;

import lombok.Data;
import lombok.ToString;
import org.springframework.util.ObjectUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The View Object of Service
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
public class ServiceVo {
    private String name;
    private String version;
    private List<EndpointVo> endpointVoList;
    private List<String> versionList;

    public Set<String> getVersionList() {
        final Set<String> result = new HashSet<>();
        for (final EndpointVo endpointVo : endpointVoList) {
            if (ObjectUtils.isEmpty(endpointVo.getVersion())) {
                continue;
            }
            result.add(endpointVo.getVersion());
        }
        return result;
    }
}