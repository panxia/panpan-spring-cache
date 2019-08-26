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
    private long expireAfterAccess=5;


    /** 写入后过期时间，单位秒*/
    private long expireAfterWrite;

    /** 写入后刷新时间，单位秒*/
    private long refreshAfterWrite;

    /** 初始化大小*/
    private int initialCapacity=1000;

    /** 最大缓存对象个数，超过此数量时之前放入的缓存将失效*/
    private long maximumSize;


    public CaffeineConfig(String cacheName) {
        this.cacheName = cacheName;
    }

    public long getExpireAfterAccess() {
        return expireAfterAccess;
    }

    public void setExpireAfterAccess(long expireAfterAccess) {
        this.expireAfterAccess = expireAfterAccess;
    }

    public long getExpireAfterWrite() {
        return expireAfterWrite;
    }

    public void setExpireAfterWrite(long expireAfterWrite) {
        this.expireAfterWrite = expireAfterWrite;
    }

    public long getRefreshAfterWrite() {
        return refreshAfterWrite;
    }

    public void setRefreshAfterWrite(long refreshAfterWrite) {
        this.refreshAfterWrite = refreshAfterWrite;
    }

    public int getInitialCapacity() {
        return initialCapacity;
    }

    public void setInitialCapacity(int initialCapacity) {
        this.initialCapacity = initialCapacity;
    }

    public long getMaximumSize() {
        return maximumSize;
    }

    public void setMaximumSize(long maximumSize) {
        this.maximumSize = maximumSize;
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
