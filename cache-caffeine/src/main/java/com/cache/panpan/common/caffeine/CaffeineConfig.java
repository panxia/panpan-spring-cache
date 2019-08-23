package com.cache.panpan.common.caffeine;

/**
 * @ClassName: CaffeineConfig
 * @Description:
 * @Author: panxia
 * @Date: Create in 2019/8/22 2:19 PM
 * @Version:1.0
 */
public class CaffeineConfig {


    /** redis过期时间，单位秒，默认不过期*/
    private long redisDefaultExpiration = 0;
    /** redis过期时间 */
    private Long redisExpires;

    private String cacheName;


    /** 访问后过期时间，单位秒*/
    private long caffeineExpireAfterAccess=5;


    /** 写入后过期时间，单位秒*/
    private long caffeineExpireAfterWrite;

    /** 写入后刷新时间，单位秒*/
    private long caffeineRefreshAfterWrite;

    /** 初始化大小*/
    private int caffeineInitialCapacity=1000;

    /** 最大缓存对象个数，超过此数量时之前放入的缓存将失效*/
    private long caffeineMaximumSize;


    public CaffeineConfig(String cacheName) {
        this.cacheName = cacheName;
    }

    public long getCaffeineExpireAfterAccess() {
        return caffeineExpireAfterAccess;
    }

    public void setCaffeineExpireAfterAccess(long caffeineExpireAfterAccess) {
        this.caffeineExpireAfterAccess = caffeineExpireAfterAccess;
    }

    public long getCaffeineExpireAfterWrite() {
        return caffeineExpireAfterWrite;
    }

    public void setCaffeineExpireAfterWrite(long caffeineExpireAfterWrite) {
        this.caffeineExpireAfterWrite = caffeineExpireAfterWrite;
    }

    public long getCaffeineRefreshAfterWrite() {
        return caffeineRefreshAfterWrite;
    }

    public void setCaffeineRefreshAfterWrite(long caffeineRefreshAfterWrite) {
        this.caffeineRefreshAfterWrite = caffeineRefreshAfterWrite;
    }

    public int getCaffeineInitialCapacity() {
        return caffeineInitialCapacity;
    }

    public void setCaffeineInitialCapacity(int caffeineInitialCapacity) {
        this.caffeineInitialCapacity = caffeineInitialCapacity;
    }

    public long getCaffeineMaximumSize() {
        return caffeineMaximumSize;
    }

    public void setCaffeineMaximumSize(long caffeineMaximumSize) {
        this.caffeineMaximumSize = caffeineMaximumSize;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
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
}
