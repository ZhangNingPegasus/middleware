package org.wyyt.kafka.monitor.entity.po;

import lombok.Data;

/**
 * the entity class for offset of alert information
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
public class Alert {
    private String email;
    private String emailTitle;
    private String emailContent;
    private String dingAccessToken;
    private String dingSecret;
    private String dingContent;
}