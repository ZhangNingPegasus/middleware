package org.wyyt.sharding.algorithm.impl;

import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;
import org.wyyt.sharding.algorithm.AbstractComplextShardingAlgorithm;
import org.wyyt.sharding.algorithm.MathsTool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * algorithm of table splitting
 * <p>
 * Let the total number of databases be M; the total number of tables is N, and the value of the split key is X
 * Then: the number of tables in each database is P = N / M
 * <p>
 * Database splitting algorithm: [murmur3_128(X) / P] % M
 * Table splitting algorithm: murmur3_128(X) % N
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public final class TableComplexShardingAlgorithm extends AbstractComplextShardingAlgorithm implements ComplexKeysShardingAlgorithm<Long> {
    @Override
    public final Collection<String> doSharding(final Collection<String> availableTargetNames,
                                               final ComplexKeysShardingValue<Long> shardingValue) {
        final List<String> result = new ArrayList<>();
        super.sharding(shardingValue, (value, databaseNumCount, tableCount, dimensionInfo, datasourceNames) -> {
            final long remainder = MathsTool.doTableSharding(value, tableCount);
            result.add(String.format(dimensionInfo.getTableNameFormat(), remainder));
        });
        return result;
    }
}