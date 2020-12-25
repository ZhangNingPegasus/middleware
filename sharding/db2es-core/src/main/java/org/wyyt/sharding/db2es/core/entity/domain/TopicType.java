package org.wyyt.sharding.db2es.core.entity.domain;

import org.wyyt.sharding.db2es.core.exception.Db2EsException;

/**
 * the topic type
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
public enum TopicType {
    /**
     * 正在使用中的索引
     */
    IN_USE(0),
    /**
     * 正在重建中的索引
     */
    REBUILD(1);

    private final int type;

    TopicType(final int type) {
        this.type = type;
    }

    public static TopicType get(final int type) {
        for (TopicType item : TopicType.values()) {
            if (item.getType() == type) {
                return item;
            }
        }
        throw new Db2EsException(String.format("不存在值为[%s]的主题类型", type));
    }

    public final TopicType getAnotherType() {
        if (this == IN_USE) {
            return REBUILD;
        } else if (this == REBUILD) {
            return IN_USE;
        }
        throw new Db2EsException(String.format("未知的主题类型, %s", type));
    }

    public static TopicType getDefaultType(final TopicType type) {
        if (type == IN_USE) {
            return IN_USE;
        } else if (type == REBUILD) {
            return REBUILD;
        }
        throw new Db2EsException(String.format("未知的主题类型, %s", type));
    }

    public final int getType() {
        return type;
    }
}