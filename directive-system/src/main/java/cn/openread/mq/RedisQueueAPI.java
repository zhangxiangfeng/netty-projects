package cn.openread.mq;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * 操作redis队列
 *
 * @author Simon
 */
@Slf4j
public class RedisQueueAPI {
    private static final String host = "prod-01-redis.frp.openread.cn";
    private static final int port = 10003;
    private static final int database = 0;

    private static volatile JedisPool pool = null;

    private static synchronized JedisPool getPool() {
        if (pool == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            //控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；
            //如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
            config.setMaxTotal(500);
            //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
            config.setMaxIdle(10);
            //表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
            config.setMaxWaitMillis(1000 * 100);
            //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
            config.setTestOnBorrow(true);
            pool = new JedisPool(config, host, port, Protocol.DEFAULT_TIMEOUT, null, database);
        }
        return pool;
    }

    public static void offerQueue(String queueName, String value) {
        try (Jedis jedis = RedisQueueAPI.getPool().getResource()) {
            Long result = jedis.lpush(queueName, value);
            log.trace("offerQueue return => {}", result);
        }
    }

    public static String takeQueue(String queueName) {
        try (Jedis jedis = RedisQueueAPI.getPool().getResource()) {
            String result = jedis.rpop(queueName);
            log.trace("takeQueue return => {}", result);
            return result;
        }
    }
}
