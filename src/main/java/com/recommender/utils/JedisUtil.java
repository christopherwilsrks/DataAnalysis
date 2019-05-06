package com.recommender.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.PostConstruct;

@Component
public class JedisUtil {

    private static JedisUtil jedisUtil;

    private final JedisPool jedisPool;

    @Autowired
    public JedisUtil(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @PostConstruct
    public void init() {
        jedisUtil = this;
    }

    // 获取一个jedis实例
    public static Jedis getJedis() {
        Jedis jedis = null;
        try {
            jedis = jedisUtil.jedisPool.getResource();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return jedis;
    }
}
