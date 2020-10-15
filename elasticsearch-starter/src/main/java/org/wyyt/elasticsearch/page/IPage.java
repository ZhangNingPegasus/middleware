package org.wyyt.elasticsearch.page;

import org.apache.lucene.search.TotalHits;

import java.io.Serializable;
import java.util.List;

/**
 * the entity of elastic-search's page
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
public interface IPage<T> extends Serializable {

    /**
     * Get sorting information, sorted fields and forward and reverse order
     *
     * @return sorting information
     */
    List<SortItem> sortItemList();

    /**
     * Paginated record list
     *
     * @return Paginated object record list
     */
    List<T> getRecords();

    /**
     * Set paging record list
     */
    IPage<T> setRecords(List<T> records);

    /**
     * The total number of rows currently meeting the condition
     *
     * @return total number
     */
    long getTotal();

    /**
     * Set the current total number of rows that meet the condition
     */
    IPage<T> setTotal(long total);

    /**
     * The relationship between the number of real pages and getTotal()
     *
     * @return
     */
    TotalHits.Relation getRelation();

    /**
     * The relationship between the number of real pages and getTotal()
     *
     * @param relation
     * @return
     */
    IPage<T> setRelation(TotalHits.Relation relation);

    /**
     * Get the number of displays per page
     *
     * @return Number of entries per page
     */
    long getSize();

    /**
     * Set the number of displays per page
     */
    IPage<T> setSize(long size);

    /**
     * Current page, default 1
     *
     * @return current page
     */
    long getCurrent();

    /**
     * set current page
     */
    IPage<T> setCurrent(long current);

    /**
     * 计算当前分页偏移量
     */
    default long offset() {
        return getCurrent() > 0 ? (getCurrent() - 1) * getSize() : 0;
    }

    /**
     * Total number of pages in current page
     */
    default long getPages() {
        if (0 == getSize()) {
            return 0L;
        }
        long pages = getTotal() / getSize();
        if (getTotal() % getSize() != 0) {
            pages++;
        }
        return pages;
    }

    /**
     * In order to not report an error when deserializing json
     */
    default IPage<T> setPages(long pages) {
        return this;
    }
}