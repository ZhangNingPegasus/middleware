package org.wyyt.db2es.core.entity.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * the domain entity of index name
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@AllArgsConstructor
@Data
public final class IndexName {
    /**
     * 索引别名
     */
    private String alias;
    /**
     * 索引年份
     */
    private Integer year;
    /**
     * 索引后缀
     */
    private Integer suffix;

    public IndexName(final String indexName) {
        try {
            int lastFirst = indexName.lastIndexOf("_");
            final String strSuffix = indexName.substring(lastFirst + 1);
            this.suffix = Integer.parseInt(strSuffix);

            final String remaining = indexName.substring(0, lastFirst);

            lastFirst = remaining.lastIndexOf("_");
            String strYear = remaining.substring(lastFirst + 1);
            this.year = Integer.parseInt(strYear);

            this.alias = indexName.substring(0, lastFirst);
        } catch (final Exception exception) {
            throw new RuntimeException(String.format("IndexName: the format of [%s] is incorrect", indexName), exception);
        }
    }

    @Override
    public final String toString() {
        return String.format("%s_%s_%s", this.alias, this.year, this.suffix).trim();
    }
}