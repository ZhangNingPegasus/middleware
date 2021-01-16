package org.wyyt.elasticsearch.page;

import org.apache.lucene.search.TotalHits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * the entity of elastic-search's page
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
public final class Page<T> implements IPage<T> {
    private static final long serialVersionUID = 8545996863226528798L;
    private List<T> records = Collections.emptyList();
    private long total = 0;
    private TotalHits.Relation relation;
    private long size = 10;
    private long current = 1;
    private List<SortItem> sortItemList = new ArrayList<>();

    public Page() {
    }

    /**
     * Paging constructor
     *
     * @param current current page
     * @param size    Number of entries per page
     */
    public Page(final long current, final long size) {
        this(current, size, 0);
    }

    /**
     * Paging constructor
     *
     * @param current current page
     * @param size    Number of entries per page
     * @param total   The total number of eligible records
     */
    public Page(final long current, final long size, final long total) {
        if (current > 1) {
            this.current = current;
        }
        this.size = size;
        this.total = total;
    }

    /**
     * Whether there is a previous page
     *
     * @return true / false
     */
    public final boolean hasPrevious() {
        return this.current > 1;
    }

    /**
     * Whether there is a next page
     *
     * @return true / false
     */
    public final boolean hasNext() {
        return this.current < this.getPages();
    }

    @Override
    public final List<T> getRecords() {
        return this.records;
    }

    @Override
    public final Page<T> setRecords(final List<T> records) {
        this.records = records;
        return this;
    }

    @Override
    public final long getTotal() {
        return this.total;
    }

    @Override
    public final Page<T> setTotal(final long total) {
        this.total = total;
        return this;
    }

    @Override
    public final TotalHits.Relation getRelation() {
        return this.relation;
    }

    @Override
    public final IPage<T> setRelation(final TotalHits.Relation relation) {
        this.relation = relation;
        return this;
    }

    @Override
    public final long getSize() {
        return this.size;
    }

    @Override
    public final Page<T> setSize(final long size) {
        this.size = size;
        return this;
    }

    @Override
    public final long getCurrent() {
        return this.current;
    }

    @Override
    public final Page<T> setCurrent(final long current) {
        this.current = current;
        return this;
    }

    /**
     * Add new sort criteria
     *
     * @param items condition
     * @return Returns the paging parameter itself
     */
    public final Page<T> addOrder(final SortItem... items) {
        this.sortItemList.addAll(Arrays.asList(items));
        return this;
    }

    /**
     * Add new sort criteria
     *
     * @param items condition
     * @return Returns the paging parameter itself
     */
    public final Page<T> addOrder(final List<SortItem> items) {
        this.sortItemList.addAll(items);
        return this;
    }

    @Override
    public final List<SortItem> sortItemList() {
        return getOrders();
    }

    public final List<SortItem> getOrders() {
        return this.sortItemList;
    }

    public final void setOrders(final List<SortItem> orders) {
        this.sortItemList = orders;
    }
}