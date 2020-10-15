package org.wyyt.db2es.client.ding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * the entity of DingDing's message
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Data
public final class Message {
    private String msgtype;
    private Text text;
    private At at;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public final static class Text {
        private String content;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public final static class At {
        private List<String> atMobiles;
        private Boolean isAtAll;
    }
}