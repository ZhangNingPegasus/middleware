package org.wyyt.sharding.auto.config;

import lombok.Data;

/**
 * the property of ACM
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Data
public final class AcmConfig {
    private String nacosLocalSnapshotPath;
    private String nacosLogPath;
    private String acmConfigPath;
    /**
     * ShardingSphere的数据源配置信息
     */
    private Config datasource;
    /**
     * ShardingSphere的维度配置信息
     */
    private Config dimenstion;
    /**
     * ShardingSphere的数据表配置信息
     */
    private Config table;

    @Data
    public static class Config {
        /**
         * ACM的dataId信息
         */
        private String dataId = "";

        /**
         * ACM的group信息
         */
        private String group = "";
    }
}