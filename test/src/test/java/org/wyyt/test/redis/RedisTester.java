package org.wyyt.test.redis;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import org.wyyt.redis.service.RedisService;
import org.wyyt.test.TestApplication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * the test of Redis
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
public class RedisTester {
    private static final String KEY = "a";
    @Autowired
    private RedisService redisService;

    @Test
    public void test00_lock() {
        Object v = this.redisService.get("full_sync_success_fin_pay_fund_flow_detail");
        System.out.println(v);

        try (RedisService.Lock lock = this.redisService.getDistributedLock(KEY, 10000L, 6000L)) {
            if (lock.hasLock()) {
                System.out.println("拿到锁了: " + lock.lockKey() + " " + lock.requestId());
            } else {
                System.err.println("没有拿到锁");
            }
        }
        Assert.isNull(this.redisService.get(KEY), "lock失败");
    }

    @Test
    public void test01_setAndGet() {
        this.redisService.set(KEY, System.currentTimeMillis());
        Assert.notNull(this.redisService.get(KEY), "set & get 失败");
    }

    @Test
    public void test02_expire() throws InterruptedException {
        this.redisService.expire(KEY, 100);
        Thread.sleep(101);
        Assert.isNull(this.redisService.get(KEY), "expire失败");
    }

    @Test
    public void test03_hasKey() {
        this.redisService.set(KEY, String.valueOf(System.currentTimeMillis()), 60000L);
        Assert.isTrue(redisService.hasKey(KEY), "hasKey失败");
    }

    @Test
    public void test04_del() {
        this.redisService.del(KEY);
        Assert.isTrue(!this.redisService.hasKey(KEY), "del失败");
    }

    @Test
    public void test05_mset() {
        Map<String, Object> kvMap = new HashMap<>();
        kvMap.put("A", 3.14);
        kvMap.put("B", "π");
        kvMap.put("C", true);
        this.redisService.mset(kvMap);

        this.redisService.expire("A", 60000L);
        this.redisService.expire("B", 60000L);
        this.redisService.expire("C", 60000L);
        this.redisService.expire("D", 60000L);
        Assert.notNull(this.redisService.get("A"), "mset失败");
        Assert.notNull(this.redisService.get("B"), "mset失败");
        Assert.notNull(this.redisService.get("C"), "mset失败");
    }

    @Test
    public void test06_mget() {
        List<Object> mget = this.redisService.mget("A", "B", "C");
        for (Object o : mget) {
            Assert.notNull(o, "mget失败");
        }
    }
}