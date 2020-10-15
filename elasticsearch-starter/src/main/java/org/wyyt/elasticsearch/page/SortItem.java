package org.wyyt.elasticsearch.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * the entity of sort
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString
public final class SortItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private String column;
    private boolean asc = true;

    public static SortItem asc(final String column) {
        return build(column, true);
    }

    public static SortItem desc(final String column) {
        return build(column, false);
    }

    public static List<SortItem> ascs(final String... columns) {
        return Arrays.stream(columns).map(SortItem::asc).collect(Collectors.toList());
    }

    public static List<SortItem> descs(final String... columns) {
        return Arrays.stream(columns).map(SortItem::desc).collect(Collectors.toList());
    }

    private static SortItem build(final String column,
                                  final boolean asc) {
        final SortItem item = new SortItem();
        item.setColumn(column);
        item.setAsc(asc);
        return item;
    }
}