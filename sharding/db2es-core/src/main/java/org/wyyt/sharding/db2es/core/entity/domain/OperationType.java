package org.wyyt.sharding.db2es.core.entity.domain;

/**
 * the operation type of SQL script.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public enum OperationType {
    /**
     * 新增
     */
    INSERT("INSERT"),
    /**
     * 修改
     */
    UPDATE("UPDATE"),
    /**
     * 删除
     */
    DELETE("DELETE"),
    /**
     * 清空表数据
     */
    TRUNCATE("TRUNCATE"),
    /**
     * 修改表结构
     */
    ALTER("ALTER"),
    /**
     * 擦除操作, 例如DROP TABLE
     */
    ERASE("ERASE"),
    /**
     * 创建对象,如CREATE TABLE
     */
    CREATE("CREATE"),
    /**
     * 删除索引, 如DROP INDEX
     */
    DINDEX("DINDEX"),
    /**
     * 解析异常(自定义)
     */
    EXCEPTION("EXCEPTION"),
    /**
     * 未知
     */
    UNKOWN("UNKNOWN");

    private final String operationType;

    OperationType(final String operationType) {
        this.operationType = operationType;
    }

    public static OperationType get(final String operationType) {
        for (final OperationType item : OperationType.values()) {
            if (item.getOperationType().equalsIgnoreCase(operationType)) {
                return item;
            }
        }
        return UNKOWN;
    }

    public final String getOperationType() {
        return operationType;
    }
}