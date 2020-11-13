package org.wyyt.test.elasticsearch;

import lombok.Data;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.wyyt.elasticsearch.page.IPage;
import org.wyyt.elasticsearch.page.Page;
import org.wyyt.elasticsearch.service.ElasticSearchService;
import org.wyyt.test.TestApplication;
import org.wyyt.tool.common.CommonTool;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * the test of Elastic-Search
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Rollback(false)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ElasticSearchTester {
    private static final String INDEX_NAME = "middleware_elastic_search_for_test";
    private static final String PRIMARY_KEY_VALUE = "1";
    @Autowired
    private ElasticSearchService elasticSearchService;

    @Test
    public void test00_deleteIndex() throws IOException {
        if (this.elasticSearchService.exists(INDEX_NAME)) {
            boolean result = this.elasticSearchService.dropIndex(INDEX_NAME);
            Assert.isTrue(result, "索引删除失败");
        }
    }

    @Test
    public void test01_insert() throws IOException {
        Map<String, Object> datum = new HashMap<>();
        datum.put("id", PRIMARY_KEY_VALUE);
        datum.put("name", "张三");
        datum.put("age", 18);
        datum.put("sex", "female");
        datum.put("row_create_time", System.currentTimeMillis());
        datum.put("row_update_time", System.currentTimeMillis());
        boolean result = this.elasticSearchService.insert(INDEX_NAME, PRIMARY_KEY_VALUE, datum);
        Assert.isTrue(result, "插入文档失败");
    }

    @Test
    public void test02_update() throws IOException {
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("name", "貂蝉");
        updateMap.put("description", "Let's do amazing... ...!!!!");
        boolean result = this.elasticSearchService.update(INDEX_NAME, PRIMARY_KEY_VALUE, updateMap);
        Assert.isTrue(result, "修改文档失败");
    }

    @Test
    public void test03_delete() throws IOException {
        boolean result = this.elasticSearchService.delete(INDEX_NAME, PRIMARY_KEY_VALUE);
        Assert.isTrue(result, "修改文档失败");
    }

    @Test
    public void test04_bulk() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();

        Map<String, Object> insertMap1 = new HashMap<>();
        insertMap1.put("id", PRIMARY_KEY_VALUE);
        insertMap1.put("name", "张三");
        insertMap1.put("remark", "用于测试ElasticSearch");
        insertMap1.put("row_create_time", System.currentTimeMillis());
        insertMap1.put("row_update_time", System.currentTimeMillis());
        bulkRequest.add(new IndexRequest(INDEX_NAME).id(PRIMARY_KEY_VALUE).source(insertMap1));

        Map<String, Object> insertMap2 = new HashMap<>();
        insertMap2.put("id", 2);
        insertMap2.put("name", "李四");
        insertMap2.put("remark", "请删除我");
        insertMap2.put("row_create_time", System.currentTimeMillis());
        insertMap2.put("row_update_time", System.currentTimeMillis());
        bulkRequest.add(new IndexRequest(INDEX_NAME).id("2").source(insertMap2));

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("name", "王五");
        updateMap.put("remark", "颚ABCDEFGHIJKLMNOPQRSTUVWXYZ_1234567890987654321");
        updateMap.put("row_update_time", System.currentTimeMillis());
        bulkRequest.add(new UpdateRequest(INDEX_NAME, PRIMARY_KEY_VALUE).doc(updateMap));

        bulkRequest.add(new DeleteRequest(INDEX_NAME, "2"));

        BulkResponse bulkResponse = elasticSearchService.bulk(bulkRequest);
        Assert.isTrue(!bulkResponse.hasFailures(), String.format("批量操作失败 : %s", Arrays.stream(bulkResponse.getItems()).filter(BulkItemResponse::isFailed).collect(Collectors.toList())));
    }

    @Test
    public void test05_getById() throws Exception {
        CommonTool.sleep(1000); //ES 操作数据并不能马上就能查到，需要等待ES刷盘（除非设置ES操作后立马刷盘，但这样会影响并发性能）
        String response = this.elasticSearchService.getById(INDEX_NAME, PRIMARY_KEY_VALUE, String.class);
        System.out.println(response);
        Assert.isTrue(!ObjectUtils.isEmpty(response), "根据_id获取文档失败");
    }

    @Test
    public void test06_search() throws Exception {
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(QueryBuilders.rangeQuery("id").gte(1).lte(20)); //范围查询。must相当于SQL where字句中的AND; should则相当于OR
        boolQueryBuilder.must(QueryBuilders.matchQuery("remark", "颚ABCDEFGHIJKLMNOPQRSTUVWXYZ_1234567890987654321")); //match查询

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.from(0); //获取的起始位置,可用以分页
        searchSourceBuilder.size(10);//获取的document记录数,可用于分页
        searchSourceBuilder.sort("row_create_time", SortOrder.ASC); //排序
        searchSourceBuilder.fetchSource(new String[]{"id", "name", "remark"}, new String[]{});

        searchRequest.source(searchSourceBuilder);

        List<String> response = this.elasticSearchService.select(searchRequest, String.class);
        for (String s : response) {
            System.out.println(s);
        }
        Assert.isTrue(response.size() > 0, "查询失败");
    }

    @Test
    public void test07_selectOne() throws Exception {
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(QueryBuilders.rangeQuery("id").gte(1).lte(20)); //范围查询。must相当于SQL where字句中的AND; should则相当于OR
        boolQueryBuilder.must(QueryBuilders.matchQuery("remark", "颚ABCDEFGHIJKLMNOPQRSTUVWXYZ_1234567890987654321")); //match查询

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.from(0); //获取的起始位置,可用以分页
        searchSourceBuilder.size(10);//获取的document记录数,可用于分页
        searchSourceBuilder.sort("row_create_time", SortOrder.ASC); //排序
        searchSourceBuilder.fetchSource(new String[]{"id", "name", "remark"}, new String[]{});

        searchRequest.source(searchSourceBuilder);
        String response = this.elasticSearchService.selectOne(searchRequest, String.class);
        System.out.println(response);
        Assert.isTrue(!ObjectUtils.isEmpty(response), "根据条件查询单条文档失败");
    }

    @Test
    public void test08_listAllIndex() throws Exception {
        Set<String> indexNameSet = this.elasticSearchService.listIndexNames();
        System.out.println(Arrays.toString(indexNameSet.toArray(new String[]{})));
    }

    @Test
    public void test09_page() throws IOException {
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(QueryBuilders.matchPhraseQuery("id", "1")); //match查询

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);

        IPage<TestEntity> page = this.elasticSearchService.page(
                searchRequest,
                TestEntity.class,
                new Page<>(1, 10));
        System.out.println(page.getRecords());
    }

    @Test
    public void test100_deleteIndex() throws IOException {
        if (this.elasticSearchService.exists(INDEX_NAME)) {
            boolean result = this.elasticSearchService.dropIndex(INDEX_NAME);
            Assert.isTrue(result, "索引删除失败");
        }
    }

    @Data
    public static class TestEntity implements Serializable {
        private static final long serialVersionUID = 1L;

        public Long id;
        private String name;
        private String remark;
        public Date rowCreateTime;
        public Date rowUpdateTime;
    }
}