package com.cache.panpan.common.caffeine;


import com.cache.panpan.base.notice.IClearNotice;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * @ClassName: CaffeineRedisConfig
 * @Description: 缓存香港配置
 * @Author: panxia
 * @Date: Create in 2019/8/16 4:41 PM
 * @Version:1.0
 */
public class CaffeineRedisCacheConfig {

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
    private List<CaffeineConfig>  localCaffeineConfigs;

    /**
     * 用于集群环境下缓存清楚使用，如果不配置，默认启用wmb 消息通知， 线上环境需要重新申请，消息key
     */
    private IClearNotice notice;


    public CaffeineRedisCacheConfig() {
    }

    /**
     *
     * @param redisExpires
     * redis过期时间，单位秒，默认不过期
     *
     * @param redisTemplate
     * redis缓存配置(二级缓存配置)
     * @param localCaffeineConfigs
     *
     * 本地缓存配置 一级缓存配置
     */
    public CaffeineRedisCacheConfig(Long redisExpires, RedisTemplate redisTemplate, List<CaffeineConfig> localCaffeineConfigs) {
        this.redisExpires = redisExpires;
        this.redisTemplate = redisTemplate;
        this.localCaffeineConfigs = localCaffeineConfigs;
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






    public List<CaffeineConfig> getLocalCaffeineConfigs() {
        return localCaffeineConfigs;
    }

    public void setLocalCaffeineConfigs(List<CaffeineConfig> localCaffeineConfigs) {
        this.localCaffeineConfigs = localCaffeineConfigs;
    }

    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public IClearNotice getNotice() {
        return notice;
    }

    public void setNotice(IClearNotice notice) {
        this.notice = notice;
    }
}

