package org.wyyt.redis.service;

import io.lettuce.core.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.io.Closeable;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * The service of redis, providing the common methods
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
public final class RedisService {
    private static final Logger LOG = LoggerFactory.getLogger(RedisService.class);
    private static final List<String> WILDCARD = Arrays.asList("*", "?", "[", "]");
    private static final String UNLOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    @Resource(name = "redisTemplateNoTransactional")
    protected RedisTemplate<String, Object> redisTemplateNoTransactional = null;

    // start============================Common=============================start

    /**
     * 指定缓存过期时间
     *
     * @param key              键
     * @param timeInMillSecond 单位:毫秒
     */
    public final void expire(final String key,
                             final long timeInMillSecond) {
        try {
            if (!ObjectUtils.isEmpty(key)) {
                this.redisTemplateNoTransactional.expire(key, timeInMillSecond, TimeUnit.MILLISECONDS);
            }
        } catch (final Exception e) {
            LOG.error(String.format("RedisService: invoke expire method meet error with %s", e.getMessage()), e);
        }
    }

    /**
     * 根据key获取过期时间
     *
     * @param key 根据key 获取过期时间
     * @return 时间(毫秒) 返回0代表为永久有效
     */
    public final Long getExpire(final String key) {
        if (!ObjectUtils.isEmpty(key)) {
            return this.redisTemplateNoTransactional.getExpire(key, TimeUnit.MILLISECONDS);
        }
        return null;
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true:存在; false:不存在
     */
    public final Boolean hasKey(final String key) {
        try {
            return this.redisTemplateNoTransactional.hasKey(key);
        } catch (Exception e) {
            LOG.error(String.format("RedisService: invoke hasKey method meet error with %s", e.getMessage()), e);
            return false;
        }
    }

    /**
     * 清除缓存
     *
     * @param keys 键集合
     */
    public final void del(final String... keys) {
        if (keys == null || keys.length < 1) {
            throw new RuntimeException("KEY must be not null or empty!");
        }
        for (final String key : keys) {
            if (hasWildCard(key)) {
                // 包含通配符
                this.redisTemplateNoTransactional.delete(keys(key));
            } else {
                // 不包含通配符
                this.redisTemplateNoTransactional.delete(Collections.singletonList(key));
            }
        }
    }

    // end============================Common=============================end

    // start============================String=============================start

    /**
     * 根据表达式获取对应的key集合
     *
     * @param pattern 表达式
     * @return 返回符合表达式的键集合
     */
    public final Set<String> keys(final String pattern) {
        return this.redisTemplateNoTransactional.keys(pattern);
    }

    /**
     * 缓存获取
     *
     * @param key 键
     * @return 值
     */
    public final Object get(final String key) {
        return ObjectUtils.isEmpty(key) ? null : this.redisTemplateNoTransactional.opsForValue().get(key);
    }

    /**
     * 缓存获取
     *
     * @param keys 键
     * @return 值
     */
    public final List<Object> mget(final String... keys) {
        return this.redisTemplateNoTransactional.opsForValue().multiGet(Arrays.asList(keys));
    }

    /**
     * 设置缓存
     *
     * @param key   键
     * @param value 值
     */
    public final void set(final String key,
                          final Object value) {
        try {
            this.redisTemplateNoTransactional.opsForValue().set(key, value);
        } catch (final Exception e) {
            LOG.error(String.format("RedisService: invoke set method meet error with %s", e.getMessage()), e);
        }
    }

    public final void mset(final Map<String, Object> kvMap) {
        try {
            this.redisTemplateNoTransactional.opsForValue().multiSet(kvMap);
        } catch (final Exception e) {
            LOG.error(String.format("RedisService: invoke mset method meet error with %s", e.getMessage()), e);
        }
    }

    /**
     * 设置缓存并设置过期时间
     *
     * @param key                   键
     * @param value                 值
     * @param expireTimeMillSeconds 过期时间, 单位毫秒,如果time小于等于0 将设置无限期
     */
    public final void set(final String key,
                          final Object value,
                          final Long expireTimeMillSeconds) {
        try {
            if (expireTimeMillSeconds > 0) {
                this.redisTemplateNoTransactional.opsForValue().set(key, value, expireTimeMillSeconds, TimeUnit.MILLISECONDS);
            } else {
                set(key, value);
            }
        } catch (final Exception e) {
            LOG.error(String.format("RedisService: invoke set method meet error with %s", e.getMessage()), e);
        }
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 增加步长(需大于0)
     * @return 返回减少后的数值
     */
    public final long incr(final String key,
                           final long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return this.redisTemplateNoTransactional.opsForValue().increment(key, delta);
    }

    // end============================String=============================end

    // start============================Map=============================start

    /**
     * 递减
     *
     * @param key   键
     * @param delta 减少步长(需小于0)
     * @return 返回减少后的数值
     */
    public final long decr(final String key,
                           final long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return this.redisTemplateNoTransactional.opsForValue().increment(key, -delta);
    }

    /**
     * 返回Hash的元素个数
     *
     * @param key 键
     * @return 返回Hash的元素个数
     */
    public final Long hlen(final String key) {
        return this.redisTemplateNoTransactional.opsForHash().size(key);
    }

    /**
     * HashGet
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return 值
     */
    public final Object hget(final String key,
                             final String item) {
        return this.redisTemplateNoTransactional.opsForHash().get(key, item);
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    public final Map<Object, Object> hmget(final String key) {
        return this.redisTemplateNoTransactional.opsForHash().entries(key);
    }

    /**
     * HashSet
     *
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public final boolean hmset(final String key,
                               final Map<String, Object> map) {
        try {
            this.redisTemplateNoTransactional.opsForHash().putAll(key, map);
            return true;
        } catch (final Exception e) {
            LOG.error(String.format("RedisService: invoke hmset method meet error with %s", e.getMessage()), e);
            return false;
        }
    }

    /**
     * HashSet 并设置时间
     *
     * @param key        键
     * @param map        对应多个键值
     * @param expireTime 过期时间. 单位秒
     * @return true成功 false失败
     */
    public final boolean hmset(final String key,
                               final Map<String, Object> map,
                               final long expireTime) {
        try {
            this.redisTemplateNoTransactional.opsForHash().putAll(key, map);
            if (expireTime > 0) {
                expire(key, expireTime);
            }
            return true;
        } catch (final Exception e) {
            LOG.error(String.format("RedisService: invoke hmset method meet error with %s", e.getMessage()), e);
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @return true 成功 false失败
     */
    public final boolean hset(final String key,
                              final String item,
                              final Object value) {
        try {
            this.redisTemplateNoTransactional.opsForHash().put(key, item, value);
            return true;
        } catch (final Exception e) {
            LOG.error(String.format("RedisService: invoke hset method meet error with %s", e.getMessage()), e);
            return false;
        }
    }

    /**
     * 删除hash表中的值
     *
     * @param key  键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public final void hdel(final String key,
                           final Object... item) {
        this.redisTemplateNoTransactional.opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public final boolean hexists(final String key,
                                 final String item) {
        return this.redisTemplateNoTransactional.opsForHash().hasKey(key, item);
    }

    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     *
     * @param key   键
     * @param item  项
     * @param delta 增加步长(需大于0)
     * @return 返回当前值
     */
    public final double hincr(final String key,
                              final String item,
                              final double delta) {
        return this.redisTemplateNoTransactional.opsForHash().increment(key, item, delta);
    }

    /**
     * hash递减
     *
     * @param key   键
     * @param item  项
     * @param delta 减少步长(需小于0)
     * @return 返回当前值
     */
    public final double hdecr(final String key,
                              final String item,
                              final double delta) {
        return this.redisTemplateNoTransactional.opsForHash().increment(key, item, -delta);
    }

    // end============================Map=============================end

    // start============================Set=============================start

    public final Cursor<Map.Entry<Object, Object>> hscan(final String key,
                                                         final ScanOptions scanOptions) {
        return this.redisTemplateNoTransactional.opsForHash().scan(key, scanOptions);
    }

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     * @return 返回值
     */
    public final Set<Object> smembers(final String key) {
        try {
            return this.redisTemplateNoTransactional.opsForSet().members(key);
        } catch (final Exception e) {
            LOG.error(String.format("RedisService: invoke smembers method meet error with %s", e.getMessage()), e);
            return null;
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    public final boolean sismember(final String key,
                                   final Object value) {
        try {
            return this.redisTemplateNoTransactional.opsForSet().isMember(key, value);
        } catch (final Exception e) {
            LOG.error(String.format("RedisService: invoke sismember method meet error with %s", e.getMessage()), e);
            return false;
        }
    }

    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public final long sadd(final String key,
                           final Object... values) {
        try {
            return this.redisTemplateNoTransactional.opsForSet().add(key, values);
        } catch (final Exception e) {
            LOG.error(String.format("RedisService: invoke sadd method meet error with %s", e.getMessage()), e);
            return 0;
        }
    }

    /**
     * 将set数据放入缓存
     *
     * @param key        键
     * @param expireTime 过期时间(秒)
     * @param values     值列表
     * @return 成功个数
     */
    public final long sadd(final String key,
                           final long expireTime,
                           final Object... values) {
        try {
            final Long count = this.redisTemplateNoTransactional.opsForSet().add(key, values);
            if (expireTime > 0) {
                this.expire(key, expireTime);
            }
            return count;
        } catch (final Exception e) {
            LOG.error(String.format("RedisService: invoke sadd method meet error with %s", e.getMessage()), e);
            return 0;
        }
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return 返回值
     */
    public final long slen(final String key) {
        try {
            return this.redisTemplateNoTransactional.opsForSet().size(key);
        } catch (final Exception e) {
            LOG.error(String.format("RedisService: invoke key method meet error with %s", e.getMessage()), e);
            return 0;
        }
    }
    // end============================Set=============================end

    // start============================List=============================start

    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public final long srem(final String key,
                           final Object... values) {
        try {
            return this.redisTemplateNoTransactional.opsForSet().remove(key, values);
        } catch (final Exception e) {
            LOG.error(String.format("RedisService: invoke key method meet error with %s", e.getMessage()), e);
            return 0;
        }
    }

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0 到 -1代表所有值
     * @return 返回值
     */
    public final List<Object> lrange(final String key,
                                     final long start,
                                     final long end) {
        try {
            return this.redisTemplateNoTransactional.opsForList().range(key, start, end);
        } catch (final Exception e) {
            LOG.error(String.format("RedisService: invoke lrange method meet error with %s", e.getMessage()), e);
            return null;
        }
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     * @return 返回个数
     */
    public final long llen(final String key) {
        try {
            return this.redisTemplateNoTransactional.opsForList().size(key);
        } catch (final Exception e) {
            LOG.error(String.format("RedisService: invoke llen method meet error with %s", e.getMessage()), e);
            return 0;
        }
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index>=0时， 0 第一个元素，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return 返回值
     */
    public final Object lindex(final String key,
                               final long index) {
        try {
            return this.redisTemplateNoTransactional.opsForList().index(key, index);
        } catch (final Exception e) {
            LOG.error(String.format("RedisService: invoke lindex method meet error with %s", e.getMessage()), e);
            return null;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return 操作成功返回true, 失败返回false
     */
    public final boolean rpush(final String key,
                               final Object value) {
        try {
            this.redisTemplateNoTransactional.opsForList().rightPush(key, value);
            return true;
        } catch (final Exception e) {
            LOG.error(String.format("RedisService: invoke rpush method meet error with %s", e.getMessage()), e);
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return 操作成功返回true, 失败返回false
     */
    public final boolean lset(final String key,
                              final List<Object> value) {
        try {
            this.redisTemplateNoTransactional.opsForList().rightPushAll(key, value);
            return true;
        } catch (final Exception e) {
            LOG.error(String.format("RedisService: invoke lset method meet error with %s", e.getMessage()), e);
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key        键
     * @param value      值
     * @param expireTime 过期时间, 单位秒
     * @return 操作成功返回true, 失败返回false
     */
    public final boolean lset(final String key,
                              final List<Object> value,
                              final long expireTime) {
        try {
            this.redisTemplateNoTransactional.opsForList().rightPushAll(key, value);
            if (expireTime > 0) {
                expire(key, expireTime);
            }
            return true;
        } catch (final Exception e) {
            LOG.error(String.format("RedisService: invoke lset method meet error with %s", e.getMessage()), e);
            return false;
        }
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     * @return 操作成功返回true; 操作失败返回false
     */
    public final boolean lset(final String key,
                              final long index,
                              final Object value) {
        try {
            this.redisTemplateNoTransactional.opsForList().set(key, index, value);
            return true;
        } catch (final Exception e) {
            LOG.error(String.format("RedisService: invoke lset method meet error with %s", e.getMessage()), e);
            return false;
        }
    }
    // end============================List=============================end

    // start============================SortedSet=============================start

    /**
     * 移除N个值为value
     *
     * @param key   键
     * @param count 移除多少个.count > 0 : 从表头开始向表尾搜索，移除与 value 相等的元素; count < 0 :
     *              从表尾开始向表头搜索，移除与 value 相等的元素; count = 0 : 移除表中所有与 value 相等的值
     * @param value 值
     * @return 移除的个数
     */
    public final Long lrem(final String key,
                           final long count,
                           final Object value) {
        try {
            return this.redisTemplateNoTransactional.opsForList().remove(key, count, value);
        } catch (final Exception e) {
            LOG.error(String.format("RedisService: invoke lrem method meet error with %s", e.getMessage()), e);
            return 0L;
        }
    }

    /**
     * 将一个member 元素及其 score 值加入到有序集 key 当中。
     *
     * @param key   键
     * @param value 值
     * @param score 分数
     */
    public final boolean zadd(final String key,
                              final Object value,
                              final double score) {
        return this.redisTemplateNoTransactional.opsForZSet().add(key, value, score);
    }

    /**
     * 返回有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。有序集成员按 score
     * 值递增(从小到大)次序排列。
     *
     * @param key 键
     * @param min 最小值
     * @param max 最大值
     * @return 返回值
     */
    public final Set<Object> zrangebyscore(final String key,
                                           final double min,
                                           final double max) {
        return this.redisTemplateNoTransactional.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * 返回有序集 key 中， score值在min和max之间(默认包括 score值等于min或max)的成员的数量
     *
     * @param key 键
     * @param min 最小值
     * @param max 最大值
     * @return 返回成员个数
     */
    public final long zcount(final String key,
                             final double min,
                             final double max) {
        return this.redisTemplateNoTransactional.opsForZSet().count(key, min, max);
    }

    // end============================SortedSet=============================end


    // start============================Lock=============================start

    /**
     * 移除有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。
     *
     * @param key 键
     * @param min 最小值
     * @param max 最大值
     * @return 返回元素个数
     */
    public final long removeRangeByScore(final String key,
                                         final double min,
                                         final double max) {
        return this.redisTemplateNoTransactional.opsForZSet().removeRangeByScore(key, min, max);
    }

    /**
     * 获取锁
     *
     * @param lockKey              key值
     * @param expireInMillSeconds  过期时间
     * @param timeoutInMillSeconds 获取锁的超时时间
     * @return 获取锁对象(调用hasLock方法确定是否拿到了锁)
     */
    public final Lock getDistributedLock(final String lockKey,
                                         final long expireInMillSeconds,
                                         final long timeoutInMillSeconds) {
        final String requestId = this.lock(lockKey, expireInMillSeconds, timeoutInMillSeconds);
        return new Lock(
                this,
                lockKey,
                requestId,
                !ObjectUtils.isEmpty(requestId)
        );
    }

    /**
     * 获取锁(过期时间:30秒; 获取锁的超时时间: 15秒)
     *
     * @param lockKey key值
     * @return 获取锁对象(调用hasLock方法确定是否拿到了锁)
     */
    public final Lock getDistributedLock(final String lockKey) {
        return this.getDistributedLock(lockKey, 30 * 1000L, 15 * 1000L);
    }

    /**
     * 判断key中是否包含redis的通配符
     *
     * @param key redis的键
     * @return 如果包含通配符则返回true, 否则返回false
     */
    private static boolean hasWildCard(final String key) {
        for (final String wildcard : WILDCARD) {
            if (key.contains(wildcard)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取锁
     *
     * @param lockKey              key值
     * @param expireInMillSeconds  过期时间
     * @param timeoutInMillSeconds 获取锁的超时时间
     * @return 返回requestId, 用于解锁
     */
    private String lock(final String lockKey,
                        final long expireInMillSeconds,
                        final long timeoutInMillSeconds) {
        if (ObjectUtils.isEmpty(lockKey)) {
            throw new RedisException("lockKey is required");
        } else if (expireInMillSeconds < 1) {
            throw new RedisException("expireInMillSeconds must be greater than 0");
        } else if (timeoutInMillSeconds < 1) {
            throw new RedisException("expireInMillSeconds must be greater than 0");
        }

        final String value = UUID.randomUUID().toString();
        final long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start <= timeoutInMillSeconds) {
            if (this.redisTemplateNoTransactional.opsForValue().setIfAbsent(lockKey, value, Duration.ofMillis(expireInMillSeconds))) {
                return value;
            }
            try {
                Thread.sleep(100L);
            } catch (final InterruptedException exception) {
                LOG.error(String.format("RedisService: lock meet error, [%s]", exception.getMessage()), exception);
            }
        }
        return null;
    }

    /**
     * 释放锁
     *
     * @param lockKey   key值
     * @param requestId lock方法的返回值
     */
    private void unlock(final String lockKey,
                        final String requestId) {
        if (ObjectUtils.isEmpty(lockKey)) {
            throw new RedisException("lockKey is required");
        } else if (ObjectUtils.isEmpty(requestId)) {
            throw new RedisException("requestId is required");
        }
        final RedisScript<Long> redisScript = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
        this.redisTemplateNoTransactional.execute(redisScript, Collections.singletonList(lockKey), requestId);
    }
    // end============================Lock=============================end


    public <T> T getOrDefault(final String key,
                              final String distributedLockKey,
                              final HandleDefault<T> handleDefault) {
        T result = (T) this.get(key);
        if (null == result) {
            try (RedisService.Lock lock = this.getDistributedLock(distributedLockKey)) {
                if (lock.hasLock()) {
                    result = (T) this.get(key);
                    if (null == result) {
                        result = handleDefault.getDefault();
                        this.set(key, result);
                    }
                }
            }
        }
        return result;
    }

    public interface HandleDefault<T> {
        T getDefault();
    }


    public static class Lock implements Closeable {
        private final RedisService redisService;
        private final String lockKey;
        private final String requestId;
        private final Boolean hasLock;

        public Lock(final RedisService redisService,
                    final String lockKey,
                    final String requestId,
                    final Boolean hasLock) {
            this.redisService = redisService;
            this.lockKey = lockKey;
            this.requestId = requestId;
            this.hasLock = hasLock;
        }

        public final String lockKey() {
            return lockKey;
        }

        public final String requestId() {
            return requestId;
        }

        public final Boolean hasLock() {
            return this.hasLock;
        }

        public final void unlock() {
            if (this.hasLock) {
                this.redisService.unlock(lockKey, requestId);
            }
        }

        @Override
        public final void close() {
            this.unlock();
        }
    }
}