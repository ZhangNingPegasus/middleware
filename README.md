中间件包括<b>ShardingSphere</b>分库分表的封装、<b>Redis</b>的封装、<b>Kafka</b>的封装、<b>Elastic-Search</b>的封装、<b>db2es-admin</b>的封装、<b>db2es-client</b>等的封装等。
<li>
sharding-starter分库分表： 内置分库分表算法、SQL语法检测等功能(拦截不合法SQL、审计等功能)
</li>

<li>
redis-starter: 对常用的redis方法进行了封装(读写、分布式锁等功能)
</li>

<li>
kafka-starter: 对常用的kafka方法进行了封装(同步和异步发送、事务等功能)
</li>

<li>
elasticsearch-starter: 对常用的elastic-search方法进行了封装(分页、搜索、写操作等功能)
</li>

<li>
db2es-admin: 对db2es-client进行管控(重建索引、数据对比、数据修复等功能)
</li>

<li>
db2es-client: 以异步的方式，将mysql数据库中的增量数据同步到elastic-search中(高可用、分布式、位点根据时间回溯等功能)
</li>

<hr/>
<h3>sharding-starter 分库分表</h3>
<li>
已知某分表的个数为N, 这些分表所需要的数据库的个数为M, 并且N值和M值都必须是2的n次方, 同时N ≥ M
</li>

<li>
分库命名规则: 物理库名称由[<b>${数据库名}_${数据库下标索引}</b>]构成, 例如: db_0, db_1, db_3, db_4, ...
</li>

<li>
分表命名规则: 物理表名称由[<b>${数据表名}_${数据表下标索引}</b>]构成, 例如: tb_0, tb_2, tb_3, tb_4, ...<br/>
</li>

<li>
以M = 4, N=64作为范例，也就是：使用4个数据库来容纳64张表，每个数据库将容纳16张数据表,那么分库分表信息如下：<br/>
<i>database_0数据库容纳数据表table_0, table_1, …, table_15</i><br/>
<i>database_1数据库容纳数据表table_16, table_17, …, table_31</i><br/>
<i>database_2数据库容纳数据表table_32, table_33, …, table_47</i><br/>
<i>database_3数据库容纳数据表table_48, table_49, …, table_63</i><br/>
</li>

<li>
每个分表必须包含三个字段: <br/>
<b>id</b> : 主键, BIGINT UNSIGNED类型 (分布式主键, 例如:雪花算法)<br/>
<b>row_create_time</b> : 记录创建时间, datetime(3), 必须设置默认值: CURRENT_TIMESTAMP, 业务代码不可修改该字段<br/>
<b>row_update_time</b> : 记录最后一次修改时间, datetime(3), 必须勾选根据当前时间戳更新, 业务代码不可修改该字段<br/>
</li>

<li>
springboot项目中指定父工程:<br/>
<pre>
&lt;parent&gt;
    &lt;groupId&gt;org.wyyt&lt;/groupId&gt;
    &lt;artifactId&gt;middleware&lt;/artifactId&gt;
    &lt;version&gt;${lastest_version}&lt;/version&gt;
    &lt;relativePath/&gt; &lt;!-- lookup parent from repository --&gt;
&lt;/parent&gt;
</pre>

在dependencyManagement中添加springboot官方父工程:<br/>
<pre>
&lt;dependencyManagement&gt;
    &lt;dependencies&gt;
        &lt;dependency&gt;
            &lt;groupId&gt;org.springframework.boot&lt;/groupId&gt;
            &lt;artifactId&gt;spring-boot-starter-parent&lt;/artifactId&gt;
            &lt;version&gt;${springboot}&lt;/version&gt;
            &lt;type&gt;pom&lt;/type&gt;
            &lt;scope&gt;import&lt;/scope&gt;
        &lt;/dependency&gt;
    &lt;/dependencies&gt;
&lt;/dependencyManagement&gt;
</pre>

springboot项目引用jar包: <br/>
<pre>
&lt;dependency&gt;
    &lt;groupId&gt;org.wyyt&lt;/groupId&gt;
    &lt;artifactId&gt;elasticsearch-starter&lt;/artifactId&gt;
    &lt;version&gt;${lastest_version}&lt;/version&gt;
&lt;/dependency&gt;
</pre>
</li>

<li>
在springboot项目的application.yml文件中,添加ACM配置,以设置分库分表信息:<br/>
<pre>
sharding:
  # 是否开启ShardingSphere数据源
  enabled: true
  # 分布式集群编号id, 不能重复(取值范围0~1023)
  work-id: 1
  # 是否输出执行的sql(true:打印; false:不打印) 
  show-sql: true
  # ACM配置信息
  acm:
    datasource:
      data-id: scfs.xml.datasource.encrypt
      group: SIJIBAO_ORDER_CENTER_GROUP
    dimenstion:
      data-id: scfs.xml.dimension
      group: SIJIBAO_ORDER_CENTER_GROUP
    table:
      data-id: scfs.xml.table
      group: SIJIBAO_ORDER_CENTER_GROUP
    acmConfigPath: acmConfig.properties
    nacosLocalSnapshotPath: /wyyt/etc/acm/sql_tool
    nacosLogPath: /wyyt/logs/tomcat/sql_tool/
</pre>

其中, scfs.xml.datasource.encrypt数据源配置信息如下:<br/>
<pre>
&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;datasources&gt;
    &lt;!-- name: 数据库的逻辑名称. 必填项, 必须唯一 --&gt;
    &lt;datasource name="finance_center_main_0"&gt;
        &lt;!-- 数据库IP地址. 必填项 --&gt;
        &lt;host&gt;192.168.0.197&lt;/host&gt;
        &lt;!-- 数据库端口. 必填项 --&gt;
        &lt;port&gt;6612&lt;/port&gt;
        &lt;!-- 数据库的真实物理名称. 必填项 --&gt;
        &lt;databaseName&gt;finance_center_main_0&lt;/databaseName&gt;
        &lt;!-- 数据库的账号. 必填项 --&gt;
        &lt;username&gt;root&lt;/username&gt;
        &lt;!-- 数据库的密码. 必填项 --&gt;
        &lt;password&gt;EqkPepuq0FN49w=&lt;/password&gt;
        &lt;!-- 配置连接池中最小可用连接的个数 --&gt;
        &lt;minIdle&gt;10&lt;/minIdle&gt;
        &lt;!-- 配置连接池中最大可用连接的个数 --&gt;
        &lt;maxActive&gt;20&lt;/maxActive&gt;
    &lt;/datasource&gt;

    &lt;datasource name="finance_center_main_1"&gt;
        &lt;host&gt;192.168.0.197&lt;/host&gt;
        &lt;port&gt;6612&lt;/port&gt;
        &lt;databaseName&gt;finance_center_main_1&lt;/databaseName&gt;
        &lt;username&gt;root&lt;/username&gt;
        &lt;password&gt;EqkPepuq0FNoCe49w=&lt;/password&gt;
        &lt;minIdle&gt;10&lt;/minIdle&gt;
        &lt;maxActive&gt;20&lt;/maxActive&gt;
    &lt;/datasource&gt;

    &lt;!--******当SQL所涉及的数据表在以上数据源中查询不到时, 会自动去isDefault=true(该属性默认为false)的数据源中寻找, 最多只能拥有一个isDefault=true的数据源******--&gt;
    &lt;datasource name="finance_other" isDefault="true"&gt;
        &lt;host&gt;192.168.5.110&lt;/host&gt;
        &lt;port&gt;6612&lt;/port&gt;
        &lt;databaseName&gt;finance_dev&lt;/databaseName&gt;
        &lt;username&gt;fin&lt;/username&gt;
        &lt;password&gt;TdAvSNMlMQhNY2MG9pzKY=&lt;/password&gt;
        &lt;minIdle&gt;10&lt;/minIdle&gt;
        &lt;maxActive&gt;20&lt;/maxActive&gt;
    &lt;/datasource&gt;
&lt;/datasources&gt;
</pre>

scfs.xml.dimension维度配置信息如下:<br/>
<pre>
&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;dimensions&gt;
    &lt;!--
        name: 维度名称，必须唯一。不允许为空
        priority: 当多个拆分键在同一条SQL中出现时，维度的优先级，数值越低，优先级越高。 不允许为空。
                  当priority="0"时，优先级最高，被视为是主维度，多个维度之间只能有一个主维度
        description: 当前维度的描述信息，不允许为空
     --&gt;
    &lt;dimension name="order-no" priority="0" description="订单维度"&gt;
        &lt;!-- ref: 数据库的逻辑名称。不允许为空 --&gt;
        &lt;datasource ref="finance_center_main_0"/&gt;
        &lt;datasource ref="finance_center_main_1"/&gt;
    &lt;/dimension&gt;
&lt;/dimensions&gt;
</pre>

scfs.xml.table数据表配置信息如下:<br/>
<pre>
&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;tables&gt;
    &lt;!--
        name: 数据表的逻辑名称，必须唯一。不允许为空
        pkName: 主键。 可以为空，为空默认为id
        rowCreateTime: 记录创建时间字段(时间精确到毫秒)，为空默认为row_create_time
        rowUpdateTime: 记录最后一次修改时间字段(时间精确到毫秒)，为空默认为row_update_time
        bindingName: 具有相同绑定名称的表为一组绑定表, 为空表示不和任何表组成绑定表
        broadcast: 是否是广播表(true: 是广播表; false: 不是)。为空表示false
    --&gt;
    &lt;table name="fin_pay_fund_flow_out_fund" pkName="id"&gt;
        &lt;!--
            ref: 维度信息xml配置中的维度名称name
            tableCountNum: 逻辑表在该维度下的分表总个数
            shardingColumn: 逻辑表在该维度下的拆分键字段
            tableNameFormat: 逻辑表与物理表之间的映射关系表达式, 为空默认是:{逻辑名称}_%s
        --&gt;
        &lt;dimension ref="order-no" tableCountNum="64" shardingColumn="order_no"/&gt;
    &lt;/table&gt;

    &lt;table name="fin_sjb_order_out_fund" pkName="id"&gt;
        &lt;dimension ref="order-no" tableCountNum="64" shardingColumn="order_no"/&gt;
    &lt;/table&gt;

    &lt;table name="fin_sjb_order" pkName="id"&gt;
        &lt;dimension ref="order-no" tableCountNum="64" shardingColumn="sjb_stock_no"/&gt;
    &lt;/table&gt;

    &lt;table name="fin_sjb_order_sub_line" pkName="id"&gt;
        &lt;dimension ref="order-no" tableCountNum="64" shardingColumn="sjb_stock_no"/&gt;
    &lt;/table&gt;

    &lt;table name="fin_sjb_order_feerate_content" pkName="id"&gt;
        &lt;dimension ref="order-no" tableCountNum="64" shardingColumn="sjb_stock_no"/&gt;
    &lt;/table&gt;

    &lt;table name="fin_payment_days_info" pkName="id"&gt;
        &lt;dimension ref="order-no" tableCountNum="64" shardingColumn="sjb_stock_no"/&gt;
    &lt;/table&gt;

    &lt;table name="fin_sjb_order_pay_line" pkName="id"&gt;
        &lt;dimension ref="order-no" tableCountNum="64" shardingColumn="sjb_stock_no"/&gt;
    &lt;/table&gt;

    &lt;table name="fin_pay_fund_flow_detail" pkName="id"&gt;
        &lt;dimension ref="order-no" tableCountNum="64" shardingColumn="stock_no"/&gt;
    &lt;/table&gt;

    &lt;table name="fin_external_capital_change_wide" pkName="id"&gt;
        &lt;dimension ref="order-no" tableCountNum="64" shardingColumn="order_no"/&gt;
    &lt;/table&gt;

    &lt;table name="fin_ac_out_fund_chg" pkName="id"&gt;
        &lt;dimension ref="order-no" tableCountNum="64" shardingColumn="trade_no"/&gt;
    &lt;/table&gt;
&lt;/tables&gt;
</pre>
</li>

<li>
在SpringBoot项目中的@Configuration类中，使用ShardingDataSource作为当前项目的主数据源，示例代码如下:
<pre>
@Configuration
public class DataSourceConfig {
    @Autowired
    private ShardingDataSource shardingDataSource;
    @Bean
    public DataSource dataSource() {
        return this.shardingDataSource;
    }
}
</pre>
</li>

<li>
配置完成, 接下来就跟平常mybatis一样进行操作即可。
</li>

<hr/>

<h3>sql-tool查询工具 - 已内置分库分表算法, 像访问一个数据源一样访问所有分库分表, 操作类似navicat</h3>

<li>
下载最新的sql-tool包，并修改config/application.yml文件<br>
<pre>
acm:
  data-id: scfs.tool
  group: SIJIBAO_ORDER_CENTER_GROUP
  acmConfigPath: acmConfig.properties
  nacosLocalSnapshotPath: /wyyt/etc/acm/db2es/
  nacosLogPath: /wyyt/logs/sql_tool/

sharding:
  enabled: true
  work-id: 300
  show-sql: false  
  acm:
    datasource:
      data-id: scfs.xml.datasource.encrypt
      group: SIJIBAO_ORDER_CENTER_GROUP
    dimenstion:
      data-id: scfs.xml.dimension
      group: SIJIBAO_ORDER_CENTER_GROUP
    table:
      data-id: scfs.xml.table
      group: SIJIBAO_ORDER_CENTER_GROUP
    acmConfigPath: acmConfig.properties
    nacosLocalSnapshotPath: /wyyt/etc/acm/sql_tool
    nacosLogPath: /wyyt/logs/tomcat/sql_tool/
</pre>
其中, scfs.tool配置如下:
<pre>
#sql tool工具端口
sql.tool.port=10086
#sql tool数据库配置
db.host=192.168.0.197
db.port=3306
db.username=root
encrypt.db.password=Xzl9H5z0zWOGu5nh=
db.dbName=scfs_sql_developer
</pre>
</li>

<li>
创建sql-tool工具所需要的数据库, 建表语句在sql/sql-tool.sql中
</li>

<li>
配置完毕, 直接运行bin/start.sh即可
</li>

<hr/>

<h3>redis-starter</h3>

<li>
springboot项目中指定父工程:<br/>
<pre>
&lt;parent&gt;
    &lt;groupId&gt;org.wyyt&lt;/groupId&gt;
    &lt;artifactId&gt;middleware&lt;/artifactId&gt;
    &lt;version&gt;${lastest_version}&lt;/version&gt;
    &lt;relativePath/&gt; &lt;!-- lookup parent from repository --&gt;
&lt;/parent&gt;
</pre>

在dependencyManagement中添加springboot官方父工程:<br/>
<pre>
&lt;dependencyManagement&gt;
    &lt;dependencies&gt;
        &lt;dependency&gt;
            &lt;groupId&gt;org.springframework.boot&lt;/groupId&gt;
            &lt;artifactId&gt;spring-boot-starter-parent&lt;/artifactId&gt;
            &lt;version&gt;${springboot}&lt;/version&gt;
            &lt;type&gt;pom&lt;/type&gt;
            &lt;scope&gt;import&lt;/scope&gt;
        &lt;/dependency&gt;
    &lt;/dependencies&gt;
&lt;/dependencyManagement&gt;
</pre>

springboot项目引用jar包: <br/>
<pre>
&lt;dependency&gt;
    &lt;groupId&gt;org.wyyt&lt;/groupId&gt;
    &lt;artifactId&gt;redis-starter&lt;/artifactId&gt;
    &lt;version&gt;${lastest_version}&lt;/version&gt;
&lt;/dependency&gt;
</pre>
</li>

<li>
在springboot项目的application.yml文件中配置redis信息:<br/>
<pre>
spring:
  redis:
    host: 192.168.6.167
    port: 6379
    password: ********
    timeout: 2000
    database: 0
    jedis:
      pool:
        max-idle: 1000
        max-wait: -1
        min-idle: 0
</pre>
</li>

<li>
配置完成。使用示例如下(所有方法名和redis官方提供的api尽量保持一致，以减少学习成本，示例仅提供最简单的两个方法，仅供参考)：
<pre>
@Autowired
private RedisService redisService;
//读写
public void setAndGet() {
    this.redisService.set(KEY, System.currentTimeMillis());
    Assert.notNull(this.redisService.get(KEY), "set & get 失败");
}

//分布式锁
public void lock() {
    try (RedisService.Lock lock = this.redisService.getLock(KEY, 10000L, 6000L)) {
        if (lock.hasLock()) {
            System.out.println("拿到锁了: " + lock.lockKey() + " " + lock.requestId());
        } else {
            System.err.println("没有拿到锁");
        }
    }
    Assert.isNull(this.redisService.get(KEY), "lock失败");
}
</pre>
</li>


<hr/>
<h3>kafka-starter</h3>
<li>
springboot项目中指定父工程:<br/>
<pre>
&lt;parent&gt;
    &lt;groupId&gt;org.wyyt&lt;/groupId&gt;
    &lt;artifactId&gt;middleware&lt;/artifactId&gt;
    &lt;version&gt;${lastest_version}&lt;/version&gt;
    &lt;relativePath/&gt; &lt;!-- lookup parent from repository --&gt;
&lt;/parent&gt;
</pre>

在dependencyManagement中添加springboot官方父工程:<br/>
<pre>
&lt;dependencyManagement&gt;
    &lt;dependencies&gt;
        &lt;dependency&gt;
            &lt;groupId&gt;org.springframework.boot&lt;/groupId&gt;
            &lt;artifactId&gt;spring-boot-starter-parent&lt;/artifactId&gt;
            &lt;version&gt;${springboot}&lt;/version&gt;
            &lt;type&gt;pom&lt;/type&gt;
            &lt;scope&gt;import&lt;/scope&gt;
        &lt;/dependency&gt;
    &lt;/dependencies&gt;
&lt;/dependencyManagement&gt;
</pre>
springboot项目引用jar包: <br/>
<pre>
&lt;dependency&gt;
    &lt;groupId&gt;org.wyyt&lt;/groupId&gt;
    &lt;artifactId&gt;kafka-starter&lt;/artifactId&gt;
    &lt;version&gt;${lastest_version}&lt;/version&gt;
&lt;/dependency&gt;
</pre>
</li>

<li>
在springboot项目的application.yml文件中配置kafka信息:<br/>
<pre>
spring:
  kafka:
    bootstrap-servers: 192.168.6.164:9092,192.168.6.165:9092,192.168.6.166:9092
    listener:
      missing-topics-fatal: false
    producer:
      retries: 3
      batch-size: 1024
      buffer-memory: 33554432
      acks: all
      compression-type: lz4
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
</pre>
</li>

<li>
配置完成。使用示例如下(示例仅提供最简单的三个方法，仅供参考)：
<pre>
@Autowired
private KafkaTest kafkaTest;
//同步发送
public void send() throws Exception {
    this.kafkaService.send(TOPIC_NAME, "KEY", String.valueOf(System.currentTimeMillis()));
}

//同步发送(带事务，当方法体失败时，该消息不会被消费)
@TranKafka
public void sendTran() throws Exception {
    this.kafkaService.send(TOPIC_NAME, "KEY", String.valueOf(System.currentTimeMillis()));
}

//异步发送(带事务)
@TranKafka
public void sendTranAsync() {
    this.kafkaService.sendAsync(TOPIC_NAME, "KEY", String.valueOf(System.currentTimeMillis()), (sendResult, throwable) -> {
        log.info(sendResult.toString());
        Assert.isTrue(false, "回调方法中的异常是不会回滚的");
    });
    Assert.isTrue(false, "能够正常回滚");
}
</pre>
</li>


<hr/>
<h3>elasticsearch-starter</h3>
<li>
springboot项目中指定父工程:<br/>
<pre>
&lt;parent&gt;
    &lt;groupId&gt;org.wyyt&lt;/groupId&gt;
    &lt;artifactId&gt;middleware&lt;/artifactId&gt;
    &lt;version&gt;${lastest_version}&lt;/version&gt;
    &lt;relativePath/&gt; &lt;!-- lookup parent from repository --&gt;
&lt;/parent&gt;
</pre>

在dependencyManagement中添加springboot官方父工程:<br/>
<pre>
&lt;dependencyManagement&gt;
    &lt;dependencies&gt;
        &lt;dependency&gt;
            &lt;groupId&gt;org.springframework.boot&lt;/groupId&gt;
            &lt;artifactId&gt;spring-boot-starter-parent&lt;/artifactId&gt;
            &lt;version&gt;${springboot}&lt;/version&gt;
            &lt;type&gt;pom&lt;/type&gt;
            &lt;scope&gt;import&lt;/scope&gt;
        &lt;/dependency&gt;
    &lt;/dependencies&gt;
&lt;/dependencyManagement&gt;
</pre>
springboot项目引用jar包: <br/>
<pre>
&lt;dependency&gt;
    &lt;groupId&gt;org.wyyt&lt;/groupId&gt;
    &lt;artifactId&gt;elaticsearch-starter&lt;/artifactId&gt;
    &lt;version&gt;${lastest_version}&lt;/version&gt;
&lt;/dependency&gt;
</pre>
</li>

<li>
在springboot项目的application.yml文件中配置elastic-search信息:<br/>
<pre>
elasticsearch:
  enabled: true
  hostnames: 192.168.6.165:9900,192.168.6.166:9900,192.168.6.167:9900
  username: elastic
  password: ******
  max-conn-total: 100
  max-conn-per-route: 20
</pre>
</li>

<li>
配置完成。使用示例如下(示例仅提供最简单的三个方法，仅供参考)：
<pre>
@Autowired
private ElasticSearchService elasticSearchService;

//根据主键查询
public void getById() throws Exception {
    String response = this.elasticSearchService.getById(INDEX_NAME, PRIMARY_KEY_VALUE, String.class);
    System.out.println(response);
}

//条件查询
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
}

//分页查询
public void page() throws IOException {
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

</pre>
</li>

<hr/>

<h3>db2es-admin</h3>
<li>下载最新的db2es-admin包</li>

<li>
根据需要修改config/application.yml文件, 该配置文件中的ACM(scfs-refactoring-db2es)配置说明如下:
<pre>
## kafka集群所使用的zookeeper集群地址, 多个用逗号隔开
zookeeper.servers=192.168.6.166:2181,192.168.6.167:2181
##
## 目标ElasticSearch的地址, 多个用逗号隔开
elasticsearch.hostnames=192.168.6.165:9900,192.168.6.166:9900,192.168.6.167:9900
## ElasticSearch的用户名
elasticsearch.username=finance
## ElasticSearch的密码
encrypt.elasticsearch.password=AQZRHONdKs=
##
## db2es数据库的地址
db.host=192.168.0.197
## db2es数据库的端口
db.port=3306
## db2es数据库的库名
db.databaseName=scfs_db2es
## db2es数据库的用户名
db.username=root
## db2es数据库的密码
encrypt.db.password=APgXwToHDGFNOz0=
</pre>
</li>

<li>
创建db2es数据库，并使用config/sql目录下的脚本进行建表
</li>

<li>
配置完毕, 直接运行bin/start.sh即可
</li>

<hr/>

<h3>db2es-client</h3>
<li>下载最新的db2es-client包</li>

<li>
根据需要修改config/db2es.propertes文件, 该配置文件中的ACM(scfs-refactoring-db2es)和上面的db2es-admin保持一样，共享即可
</li>

<li>
配置完毕, 直接运行bin/start.sh即可
</li>