package com.bj58.chr.common.guava;

/**
 * @ClassName: GuavaConfig
 * @Description:
 * @Author: panxia
 * @Date: Create in 2019/8/22 2:19 PM
 * @Version:1.0
 */
public class GuavaConfig {

    private String cacheName;


    /** 访问后过期时间，单位秒*/
    private long guavaExpireAfterAccess=5;


    /** 写入后过期时间，单位秒*/
    private long guavaExpireAfterWrite;

    /** 写入后刷新时间，单位秒*/
    private long guavaRefreshAfterWrite;

    /** 初始化大小*/
    private int guavaInitialCapacity=1;

    /** 最大缓存对象个数，超过此数量时之前放入的缓存将失效*/
    private long guavaMaximumSize;


    public GuavaConfig(String cacheName) {
        this.cacheName = cacheName;
    }

    public long getGuavaExpireAfterAccess() {
        return guavaExpireAfterAccess;
    }

    public void setGuavaExpireAfterAccess(long guavaExpireAfterAccess) {
        this.guavaExpireAfterAccess = guavaExpireAfterAccess;
    }

    public long getGuavaExpireAfterWrite() {
        return guavaExpireAfterWrite;
    }

    public void setGuavaExpireAfterWrite(long guavaExpireAfterWrite) {
        this.guavaExpireAfterWrite = guavaExpireAfterWrite;
    }

    public long getGuavaRefreshAfterWrite() {
        return guavaRefreshAfterWrite;
    }

    public void setGuavaRefreshAfterWrite(long guavaRefreshAfterWrite) {
        this.guavaRefreshAfterWrite = guavaRefreshAfterWrite;
    }

    public int getGuavaInitialCapacity() {
        return guavaInitialCapacity;
    }

    public void setGuavaInitialCapacity(int guavaInitialCapacity) {
        this.guavaInitialCapacity = guavaInitialCapacity;
    }

    public long getGuavaMaximumSize() {
        return guavaMaximumSize;
    }

    public void setGuavaMaximumSize(long guavaMaximumSize) {
        this.guavaMaximumSize = guavaMaximumSize;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }
}
