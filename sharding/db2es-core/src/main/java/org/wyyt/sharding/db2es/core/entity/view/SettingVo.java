package org.wyyt.sharding.db2es.core.entity.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * the view entity of configuration
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public final class SettingVo {
    private String name;
    private String value;
    private String description;
}