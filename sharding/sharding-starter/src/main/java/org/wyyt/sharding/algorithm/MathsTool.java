package org.wyyt.sharding.algorithm;

import com.google.common.hash.Hashing;
import org.springframework.util.Assert;

import java.util.*;

/**
 * Providing various ShardingSphere algorithms
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public final class MathsTool {

    /**
     * 分库算法
     * <p>
     * 设库的总个数为M; 表的总个数为N， 拆分键的值为X
     * 则：每个库的表数量是 P = N / M
     * <p>
     * 分库算法 :   (X / P) % M
     * 分表算法 :   X % N
     *
     * @param shardingValue    拆分键的值
     * @param tableCount       表的总个数
     * @param databaseNumCount 数据库的总个数
     * @return 返回数据库下标
     */
    public static int doDatabaseSharding(final String shardingValue,
                                         final int databaseNumCount,
                                         final int tableCount) {
        Assert.notNull(shardingValue, "拆分键的值不允许为空");
        final int tableCountPerDatabase = tableCount / databaseNumCount;
        final long key = MathsTool.hash(shardingValue);
        return (int) ((key / tableCountPerDatabase) % databaseNumCount);
    }

    /**
     * 分表算法
     * <p>
     * 设库的总个数为M; 表的总个数为N， 拆分键的值为X
     * 则：每个库的表数量是 P = N / M
     * <p>
     * 分库算法 :   (X / P) % M
     * 分表算法 ：X % N
     *
     * @param shardingValue 拆分键的值
     * @param tableCount    表的总个数
     * @return 返回数据表下标
     */
    public static int doTableSharding(final String shardingValue,
                                      final int tableCount) {
        Assert.notNull(shardingValue, "拆分键的值不允许为空");
        final long key = MathsTool.hash(shardingValue);
        return (int) (key % tableCount);
    }


    public static Map<Integer, Set<Object>> doDatabaseSharding(final List<Object> valueList,
                                                               final int databaseNumCount,
                                                               final int tableCount) {
        Assert.notEmpty(valueList, "值列表不允许为空");
        final Map<Integer, Set<Object>> result = new HashMap<>();
        if (valueList == null || valueList.isEmpty()) {
            return result;
        }
        for (final Object value : valueList) {
            final int index = MathsTool.doDatabaseSharding(value.toString(), databaseNumCount, tableCount);
            if (result.containsKey(index)) {
                result.get(index).add(value);
            } else {
                final Set<Object> objectList = new HashSet<>();
                objectList.add(value);
                result.put(index, objectList);
            }
        }
        return result;
    }

    public static long hash(final String value) {
        Assert.notNull(value, "值不允许为空");
        return Math.abs(Hashing.murmur3_128().hashBytes(value.getBytes()).asLong());
    }
}