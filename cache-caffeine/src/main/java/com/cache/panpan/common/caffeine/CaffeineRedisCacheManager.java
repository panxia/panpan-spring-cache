package com.cache.panpan.common.caffeine;


import com.cache.panpan.base.CacheClearManager;
import com.cache.panpan.base.notice.IClearNotice;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.data.redis.cache.DefaultRedisCachePrefix;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
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
    private IClearNotice notice;

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
        this.notice=caffeineRedisConfig.getNotice();
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
        //判断cache 是不是配置超时信息
        List<String> nameAndConfig=Arrays.asList(name.split("#"));

        Cache cache = super.getCache(nameAndConfig.get(0));
        return cache == null && this.dynamic ? this.createAndAddCache(nameAndConfig) : cache;
    }

    protected Cache createAndAddCache(List<String> nameAndConfig) {
        this.addCache(this.createCache(nameAndConfig));
        return super.getCache(nameAndConfig.get(0));
    }

    protected Cache createCache(List<String> nameAndConfig) {
        Optional<CaffeineConfig> caffeineRedisConfigOptional= caffeineRedisConfig.getLocalCaffeineConfigs().stream().
                filter(c -> nameAndConfig.get(0).equals(c.getCacheName())).findFirst();
        CaffeineConfig caffeineConfig=new CaffeineConfig( nameAndConfig.get(0));
        if(caffeineRedisConfigOptional.isPresent()){
            caffeineConfig =caffeineRedisConfigOptional.get();
        }else if(nameAndConfig.size()>=2) {//Spring el 配置执行
            parseExpression(nameAndConfig,caffeineConfig);
        }
        long expiration = this.computeExpiration(caffeineConfig);

        //没有配置redis;
        if(this.template==null){
            return this.caffeineCache(caffeineConfig);
        }
        return new CaffeineRedisCache( nameAndConfig.get(0), this.cachePrefix.prefix( nameAndConfig.get(0)),this.template,this.caffeineCache(caffeineConfig),expiration,notice);
    }

    private void parseExpression(List<String> nameAndConfig, CaffeineConfig caffeineConfig) {
        StandardEvaluationContext context = new StandardEvaluationContext(caffeineConfig);
        ExpressionParser parser = new SpelExpressionParser();
        for (int i = 1; i <nameAndConfig.size() ; i++) {
            parser.parseExpression(nameAndConfig.get(i)).getValue(context);
        }
    }

    protected long computeExpiration(CaffeineConfig caffeineConfig) {
        Long expiration = null;
            if (caffeineConfig.getRedisExpires() != null) {
                expiration = caffeineConfig.getRedisExpires();
            }
        return expiration != null ? expiration : caffeineConfig.getRedisDefaultExpiration();
    }



    public void setCacheNames(Collection<String> cacheNames) {
        Set<String> newCacheNames = CollectionUtils.isEmpty(cacheNames) ? Collections.emptySet() : new HashSet(cacheNames);
        this.dynamic = ((Set)newCacheNames).isEmpty();
    }


    public CaffeineCache caffeineCache(CaffeineConfig caffeineConfig){
        Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder();
        if(caffeineConfig.getExpireAfterAccess() > 0) {
            cacheBuilder.expireAfterAccess(caffeineConfig.getExpireAfterAccess(), TimeUnit.SECONDS);
        }
        if(caffeineConfig.getExpireAfterWrite() > 0) {
            cacheBuilder.expireAfterWrite(caffeineConfig.getExpireAfterWrite(), TimeUnit.SECONDS);
        }
        if(caffeineConfig.getInitialCapacity() > 0) {
            cacheBuilder.initialCapacity(caffeineConfig.getInitialCapacity());
        }
        if(caffeineConfig.getMaximumSize() > 0) {
            cacheBuilder.maximumSize(caffeineConfig.getMaximumSize());
        }
        if(caffeineConfig.getRefreshAfterWrite() > 0) {
            cacheBuilder.refreshAfterWrite(caffeineConfig.getRefreshAfterWrite(), TimeUnit.SECONDS);
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

    public void setNotice(IClearNotice notice) {
        this.notice = notice;
    }

    public RedisTemplate getTemplate() {
        return template;
    }

    public void setTemplate(RedisTemplate template) {
        this.template = template;
    }




}
