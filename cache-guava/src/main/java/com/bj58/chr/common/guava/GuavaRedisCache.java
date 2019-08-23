package com.bj58.chr.common.guava;

import com.bj58.chr.base.exception.MessageException;
import com.bj58.chr.base.notice.AbstractNotice;
import com.bj58.chr.base.notice.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.guava.GuavaCache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheElement;
import org.springframework.data.redis.cache.RedisCacheKey;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @ClassName: guaveRedisCache
 * @Description:
 * @Author: panxia
 * @Date: Create in 2019/8/16 4:41 PM
 * @Version:1.0
 */
public class GuavaRedisCache extends RedisCache {

    private final Logger logger = LoggerFactory.getLogger(GuavaRedisCache.class);


    private AbstractNotice notice;


    private Cache guaveCache;


    public GuavaRedisCache(String name, byte[] prefix, RedisTemplate<Object, Object> redisTemplate, GuavaCache guaveCache, long expiration, AbstractNotice notice) {
        super(name,prefix, redisTemplate, expiration);
        this.notice = notice;
        this.guaveCache = guaveCache;
    }



    @Override
    public <T> T get(Object key, Class<T> type) {
        T t= guaveCache.get(key,type);
        if(t!=null){
           return  t;
        }

        t= super.get(key, type);
        if(t!=null){
            guaveCache.put(key,t);
        }
        return t;
    }

    @Override
    public ValueWrapper get(Object key) {
        ValueWrapper valueWrapper=guaveCache.get(key);
        if(valueWrapper!=null&&valueWrapper.get()!=null){
            return valueWrapper;
        }

        valueWrapper= super.get(key);
        if(valueWrapper!=null&&valueWrapper.get()!=null){
            guaveCache.put(key,valueWrapper.get());
        }
        return valueWrapper;
    }

    @Override
    public RedisCacheElement get(RedisCacheKey cacheKey) {
        return super.get(cacheKey);
    }

    @Override
    public void put(Object key, Object value) {
        super.put(key, value);
        guaveCache.put(key,value);
    }

    @Override
    public void put(RedisCacheElement element) {
        super.put(element);
        guaveCache.put(element.getKey(),element.get());
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        //先修改远程
        ValueWrapper valueWrapper =super.putIfAbsent(key, value);
        guaveCache.putIfAbsent(key,value);
        return valueWrapper;
    }

    @Override
    public ValueWrapper putIfAbsent(RedisCacheElement element) {
        //先修改远程
        ValueWrapper valueWrapper= super.putIfAbsent(element);
        guaveCache.putIfAbsent(element.getKey(),element.get());
        return valueWrapper;
    }

    @Override
    public void evict(Object key) {
        try {
            super.evict(key);
            if(notice!=null){
                //先通知其他local缓存删除
                notice.send(new Message(key, getName()));
            }
            //在删除本地缓存
            guaveCache.evict(key);

        } catch (MessageException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void evict(RedisCacheElement element) {
        try {
            super.evict(element);
            //先通知其他local缓存删除
            if(notice!=null){
                notice.send(new Message(element.getKey(), getName()));
            }
            //在删除本地缓存
            guaveCache.evict(element);
        } catch (MessageException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除本地缓存
     * 用于消息删除缓存
     */
    public void clearLocal(Object key) {
        //在删除本地缓存
        logger.info("clear local cache, the key is : {}", key);
        if(key == null) {
            guaveCache.clear();
        } else {
            guaveCache.evict(key);
        }

    }

    @Override
    public void clear() {
        super.clear();
    }


    public AbstractNotice getMessageSend() {
        return notice;
    }

    public void setMessageSend(AbstractNotice messageSend) {
        this.notice = messageSend;
    }

    public Cache getguaveCache() {
        return guaveCache;
    }

    public void setguaveCache(Cache guaveCache) {
        this.guaveCache = guaveCache;
    }
}

