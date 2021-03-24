package org.wyyt.sharding.db2es.core.entity.persistent;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.apache.http.util.Asserts;
import org.json.JSONObject;
import org.wyyt.sharding.db2es.core.entity.domain.Names;

import java.util.Map;
import java.util.Objects;

/**
 * The entity for table t_topic
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@TableName(value = "`t_topic`")
public final class Topic extends BaseDto {
    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
    private JSONObject sourceJson = null;

    /**
     * 主题名称
     */
    @TableField(value = "`name`")
    private String name;

    /**
     * 将多少年的索引归为同一个索引别名
     */
    @TableField(value = "`alias_of_years`")
    private Integer aliasOfYears;

    /**
     * 主题对应的ES索引的source (必须包括settings和mappings两部分)
     */
    @TableField(value = "`source`")
    private String source;

    /**
     * 主题描述信息
     */
    @TableField(value = "`description`")
    private String description;

    /**
     * 正在使用的索引后缀. KEY: 年份; VALUE: 后缀
     */
    @TableField(exist = false)
    private Map<Integer, Integer> inUseSuffixMap;

    /**
     * 用于索引重建的索引后缀. KEY: 年份; VALUE: 后缀
     */
    @TableField(exist = false)
    private Map<Integer, Integer> rebuildSuffixMap;


    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Integer getAliasOfYears() {
        return aliasOfYears;
    }

    public void setAliasOfYears(final Integer aliasOfYears) {
        this.aliasOfYears = aliasOfYears;
    }

    public String getSource() {
        return source;
    }

    public void setSource(final String source) {
        this.source = source;
        if (null != this.sourceJson) {
            this.sourceJson.clear();
        }
        this.sourceJson = new JSONObject(this.source);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Map<Integer, Integer> getInUseSuffixMap() {
        return inUseSuffixMap;
    }

    public void setInUseSuffixMap(final Map<Integer, Integer> inUseSuffixMap) {
        this.inUseSuffixMap = inUseSuffixMap;
    }

    public Map<Integer, Integer> getRebuildSuffixMap() {
        return rebuildSuffixMap;
    }

    public void setRebuildSuffixMap(final Map<Integer, Integer> rebuildSuffixMap) {
        this.rebuildSuffixMap = rebuildSuffixMap;
    }

    public Integer getNumberOfShards() {
        Asserts.notNull(this.sourceJson, "The source information");
        final JSONObject jsonObject = (JSONObject) this.sourceJson.get(Names.SETTINGS);
        return jsonObject.getInt(Names.NUMBER_OF_SHARDS);
    }

    public Integer getNumberOfReplicas() {
        Asserts.notNull(this.sourceJson, "The source information");
        final JSONObject jsonObject = (JSONObject) this.sourceJson.get(Names.SETTINGS);
        return jsonObject.getInt(Names.NUMBER_OF_REPLICAS);
    }

    public String getRefreshInterval() {
        Asserts.notNull(this.sourceJson, "The source information");
        final JSONObject jsonObject = (JSONObject) this.sourceJson.get(Names.SETTINGS);
        return jsonObject.getString(Names.REFRESH_INTERVAL);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final Topic topic = (Topic) o;
        return name.equals(topic.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.name);
    }
}