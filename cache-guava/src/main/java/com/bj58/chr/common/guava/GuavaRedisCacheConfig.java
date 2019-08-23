package com.bj58.chr.common.guava;


import com.bj58.chr.base.notice.AbstractNotice;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * @ClassName: GuavaRedisConfig
 * @Description: 缓存香港配置
 * @Author: panxia
 * @Date: Create in 2019/8/16 4:41 PM
 * @Version:1.0
 */
public class GuavaRedisCacheConfig {

    /** redis过期时间，单位秒，默认不过期*/
    private long redisDefaultExpiration = 0;
    /** redis过期时间 */
    private Long redisExpires;
    /**
     * redis缓存配置(二级缓存配置)
     */
    private RedisTemplate redisTemplate;

    /**
     * 本地缓存配置 一级缓存配置
     */
    private List<GuavaConfig>  localGuavaConfigs;

    /**
     * 用于集群环境下缓存清楚使用，如果不配置，默认启用wmb 消息通知， 线上环境需要重新申请，消息key
     */
    private AbstractNotice notice;


    public GuavaRedisCacheConfig() {
    }

    /**
     *
     * @param redisExpires
     * redis过期时间，单位秒，默认不过期
     *
     * @param redisTemplate
     * redis缓存配置(二级缓存配置)
     * @param localGuavaConfigs
     *
     * 本地缓存配置 一级缓存配置
     */
    public GuavaRedisCacheConfig(Long redisExpires, RedisTemplate redisTemplate, List<GuavaConfig> localGuavaConfigs) {
        this.redisExpires = redisExpires;
        this.redisTemplate = redisTemplate;
        this.localGuavaConfigs = localGuavaConfigs;
    }

    public long getRedisDefaultExpiration() {
        return redisDefaultExpiration;
    }

    public void setRedisDefaultExpiration(long redisDefaultExpiration) {
        this.redisDefaultExpiration = redisDefaultExpiration;
    }

    public Long getRedisExpires() {
        return redisExpires;
    }

    public void setRedisExpires(Long redisExpires) {
        this.redisExpires = redisExpires;
    }






    public List<GuavaConfig> getLocalGuavaConfigs() {
        return localGuavaConfigs;
    }

    public void setLocalGuavaConfigs(List<GuavaConfig> localGuavaConfigs) {
        this.localGuavaConfigs = localGuavaConfigs;
    }

    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
}

