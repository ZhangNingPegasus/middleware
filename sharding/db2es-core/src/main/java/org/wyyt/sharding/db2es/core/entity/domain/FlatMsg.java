package org.wyyt.sharding.db2es.core.entity.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * the domain entity of Canal records which send to MQ
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@ToString
@EqualsAndHashCode(callSuper = false)
@Data
public class FlatMsg implements Serializable {
    private static final long serialVersionUID = 1L;
    protected long id;
    /**
     * 数据库或schema
     */
    protected String database;
    /**
     * 物理表名
     */
    protected String table;
    /**
     * 主键
     */
    protected HashSet<String> pkNames;
    /**
     * 是否DDL语句
     */
    protected Boolean isDdl;
    /**
     * SQL类型: INSERT UPDATE DELETE
     */
    protected String type;
    /**
     * SQL类型
     */
    protected OperationType operationType;
    /**
     * binlog中SQL语句的执行时间
     */
    protected Long es;
    /**
     * canal监听到该条binlog的时间
     */
    protected Long ts;
    /**
     * 单位毫秒内的递增序列号
     */
    protected Long sequence;
    /**
     * 执行的sql, dml sql为空
     */
    protected String sql;
    /**
     * 各字段类型
     */
    protected CaseInsensitiveMap<String, Integer> sqlType;
    /**
     * 各字段在数据库中的类型
     */
    protected CaseInsensitiveMap<String, String> mysqlType;
    /**
     * 数据列表
     */
    protected ArrayList<CaseInsensitiveMap<String, String>> data;
    /**
     * 旧数据列表, 用于update, size和data的size一一对应
     */
    protected ArrayList<CaseInsensitiveMap<String, String>> old;
    /**
     * 全局递增ID
     */
    protected String gtid;
    /**
     * kafka的原始消息
     */
    protected ConsumerRecord<String, String> consumerRecord;
    /**
     * 伴随FlatMessage转换时可能发生的异常, 通常情况下为null
     */
    protected Exception exception;
}