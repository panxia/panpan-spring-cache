package com.bj58.chr.common.caffeine;


import com.bj58.chr.base.exception.MessageException;
import com.bj58.chr.base.notice.AbstractNotice;
import com.bj58.chr.base.notice.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheElement;
import org.springframework.data.redis.cache.RedisCacheKey;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @ClassName: CaffeineRedisCache
 * @Description:
 * @Author: panxia
 * @Date: Create in 2019/8/16 4:41 PM
 * @Version:1.0
 */
public class CaffeineRedisCache extends RedisCache {

    private final Logger logger = LoggerFactory.getLogger(CaffeineRedisCache.class);


    private AbstractNotice notice;


    private Cache caffeineCache;


    public CaffeineRedisCache(String name, byte[] prefix,RedisTemplate<Object, Object> redisTemplate, CaffeineCache caffeineCache, long expiration, AbstractNotice notice) {
        super(name,prefix, redisTemplate, expiration);
        this.notice = notice;
        this.caffeineCache = caffeineCache;
    }



    @Override
    public <T> T get(Object key, Class<T> type) {
        T t= caffeineCache.get(key,type);
        if(t!=null){
           return  t;
        }

        t= super.get(key, type);
        if(t!=null){
            caffeineCache.put(key,t);
        }
        return t;
    }

    @Override
    public ValueWrapper get(Object key) {
        ValueWrapper valueWrapper=caffeineCache.get(key);
        if(valueWrapper!=null&&valueWrapper.get()!=null){
            return valueWrapper;
        }

        valueWrapper= super.get(key);
        if(valueWrapper!=null&&valueWrapper.get()!=null){
            caffeineCache.put(key,valueWrapper.get());
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
        caffeineCache.put(key,value);
    }

    @Override
    public void put(RedisCacheElement element) {
        super.put(element);
        caffeineCache.put(element.getKey(),element.get());
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        //先修改远程
        ValueWrapper valueWrapper =super.putIfAbsent(key, value);
        caffeineCache.putIfAbsent(key,value);
        return valueWrapper;
    }

    @Override
    public ValueWrapper putIfAbsent(RedisCacheElement element) {
        //先修改远程
        ValueWrapper valueWrapper= super.putIfAbsent(element);
        caffeineCache.putIfAbsent(element.getKey(),element.get());
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
            caffeineCache.evict(key);

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
            caffeineCache.evict(element);
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
            caffeineCache.clear();
        } else {
            caffeineCache.evict(key);
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

    public Cache getCaffeineCache() {
        return caffeineCache;
    }

    public void setCaffeineCache(Cache caffeineCache) {
        this.caffeineCache = caffeineCache;
    }
}

