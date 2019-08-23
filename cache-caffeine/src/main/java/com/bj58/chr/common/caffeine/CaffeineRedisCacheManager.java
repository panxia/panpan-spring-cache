package com.bj58.chr.common.caffeine;

import com.bj58.chr.base.CacheClearManager;
import com.bj58.chr.base.notice.AbstractNotice;
import com.bj58.chr.base.notice.WMBNotice;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.data.redis.cache.DefaultRedisCachePrefix;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName: CaffeineRedisCacheManager
 * @Description:
 * @Author: panxia
 * @Date: Create in 2019/8/19 3:18 PM
 * @Version:1.0
 */
public class CaffeineRedisCacheManager extends AbstractCacheManager implements CacheClearManager {
    private  final Logger logger ;
    private  DefaultRedisCachePrefix cachePrefix;
    private  RedisTemplate template;
    private boolean dynamic;
    private CaffeineRedisCacheConfig caffeineRedisConfig;
    private AbstractNotice notice;

    public CaffeineRedisCacheManager(CaffeineRedisCacheConfig caffeineRedisConfig) {
        this.logger = LoggerFactory.getLogger(CaffeineRedisCacheManager.class);
        this.cachePrefix = new DefaultRedisCachePrefix();
        this.dynamic = true;

        if(Objects.isNull(caffeineRedisConfig)){
            this.caffeineRedisConfig=new CaffeineRedisCacheConfig();
        }else {
            this.caffeineRedisConfig=caffeineRedisConfig;
        }
        this.template = caffeineRedisConfig.getRedisTemplate();
        if(CollectionUtils.isEmpty(caffeineRedisConfig.getLocalCaffeineConfigs())){
            caffeineRedisConfig.setLocalCaffeineConfigs(new ArrayList<>());
        }
        this.setCacheNames(caffeineRedisConfig.getLocalCaffeineConfigs().stream().map(caffeineConfig ->
                caffeineConfig.getCacheName()).collect(Collectors.toList()));
    }

    public void clearLocal(String cacheName, Object key) {
        logger.info("清理本地缓存key:{},cacheName:{} ",key,cacheName);
        Cache cache = getCache(cacheName);
        if(cache == null) {
            return ;
        }
        CaffeineRedisCache caffeineRedisCache = (CaffeineRedisCache) cache;
        caffeineRedisCache.clearLocal(key);
    }


    @Override
    protected Collection<? extends Cache> loadCaches() {
        return  Collections.emptyList();
    }


    @Override
    public Cache getCache(String name) {
        Assert.notNull(this.caffeineRedisConfig, "caffeineRedisConfigs 配置为空");
        Assert.notNull(name, "cacheName 为空");
        Cache cache = super.getCache(name);
        return cache == null && this.dynamic ? this.createAndAddCache(name) : cache;
    }

    protected Cache createAndAddCache(String cacheName) {
        this.addCache(this.createCache(cacheName));
        return super.getCache(cacheName);
    }

    protected Cache createCache(String cacheName) {
        Optional<CaffeineConfig> caffeineRedisConfigOptional= caffeineRedisConfig.getLocalCaffeineConfigs().stream().
                filter(c -> cacheName.equals(c.getCacheName())).findFirst();
        CaffeineConfig caffeineConfig=new CaffeineConfig(cacheName);
        if(caffeineRedisConfigOptional.isPresent()){
            caffeineConfig =caffeineRedisConfigOptional.get();
        }
        long expiration = this.computeExpiration(caffeineRedisConfig);
        if(notice==null&&template!=null){
            try {
                notice=new WMBNotice();
                notice.setCacheClearManager(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            notice.setCacheClearManager(this);
        }
        //没有配置redis;
        if(this.template==null){
            return this.caffeineCache(caffeineConfig);
        }
        return new CaffeineRedisCache(cacheName, this.cachePrefix.prefix(cacheName),this.template,this.caffeineCache(caffeineConfig),expiration,notice);
    }

    protected long computeExpiration(CaffeineRedisCacheConfig caffeineRedisConfig) {
        Long expiration = null;
            if (caffeineRedisConfig.getRedisExpires() != null) {
                expiration = caffeineRedisConfig.getRedisExpires();
            }
        return expiration != null ? expiration : caffeineRedisConfig.getRedisDefaultExpiration();
    }



    public void setCacheNames(Collection<String> cacheNames) {
        Set<String> newCacheNames = CollectionUtils.isEmpty(cacheNames) ? Collections.emptySet() : new HashSet(cacheNames);
        this.dynamic = ((Set)newCacheNames).isEmpty();
    }


    public CaffeineCache caffeineCache(CaffeineConfig caffeineConfig){
        Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder();
        if(caffeineConfig.getCaffeineExpireAfterAccess() > 0) {
            cacheBuilder.expireAfterAccess(caffeineConfig.getCaffeineExpireAfterAccess(), TimeUnit.SECONDS);
        }
        if(caffeineConfig.getCaffeineExpireAfterWrite() > 0) {
            cacheBuilder.expireAfterWrite(caffeineConfig.getCaffeineExpireAfterWrite(), TimeUnit.SECONDS);
        }
        if(caffeineConfig.getCaffeineInitialCapacity() > 0) {
            cacheBuilder.initialCapacity(caffeineConfig.getCaffeineInitialCapacity());
        }
        if(caffeineConfig.getCaffeineMaximumSize() > 0) {
            cacheBuilder.maximumSize(caffeineConfig.getCaffeineMaximumSize());
        }
        if(caffeineConfig.getCaffeineRefreshAfterWrite() > 0) {
            cacheBuilder.refreshAfterWrite(caffeineConfig.getCaffeineRefreshAfterWrite(), TimeUnit.SECONDS);
        }
        return new CaffeineCache(caffeineConfig.getCacheName(),cacheBuilder.build());
    }

    @Override
    public void clearLocal(Object key,String cacheName){
        Cache cache =getCache(cacheName);
        if(cache!=null){
            logger.info("本地缓存开始 cacheName:{} key;{}  ",cacheName,key);
            CaffeineRedisCache caffeineRedisCache= (CaffeineRedisCache) cache;
            caffeineRedisCache.clearLocal(key);
            logger.info("本地缓存结束 cacheName:{} key;{}  ",cacheName,key);
        }
    }

    public void setNotice(AbstractNotice notice) {
        this.notice = notice;
    }

    public RedisTemplate getTemplate() {
        return template;
    }

    public void setTemplate(RedisTemplate template) {
        this.template = template;
    }




}
