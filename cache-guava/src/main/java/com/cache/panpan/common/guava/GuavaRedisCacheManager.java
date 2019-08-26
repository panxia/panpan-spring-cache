package com.cache.panpan.common.guava;

import com.cache.panpan.base.CacheClearManager;
import com.cache.panpan.base.notice.IClearNotice;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.guava.GuavaCache;
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
 * @ClassName: GuavaRedisCacheManager
 * @Description:
 * @Author: panxia
 * @Date: Create in 2019/8/19 3:18 PM
 * @Version:1.0
 */
public class GuavaRedisCacheManager extends AbstractCacheManager implements CacheClearManager {
    private  final Logger logger ;
    private  DefaultRedisCachePrefix cachePrefix;
    private  RedisTemplate template;
    private boolean dynamic;
    private GuavaRedisCacheConfig guavaRedisConfig;
    private IClearNotice notice;

    public GuavaRedisCacheManager(GuavaRedisCacheConfig guavaRedisConfig) {
        this.logger = LoggerFactory.getLogger(GuavaRedisCacheManager.class);
        this.cachePrefix = new DefaultRedisCachePrefix();
        this.dynamic = true;

        if(Objects.isNull(guavaRedisConfig)){
            this.guavaRedisConfig=new GuavaRedisCacheConfig();
        }else {
            this.guavaRedisConfig=guavaRedisConfig;
        }
        this.template = guavaRedisConfig.getRedisTemplate();
        if(CollectionUtils.isEmpty(guavaRedisConfig.getLocalGuavaConfigs())){
            guavaRedisConfig.setLocalGuavaConfigs(new ArrayList<>());
        }
        this.notice=guavaRedisConfig.getNotice();
        this.setCacheNames(guavaRedisConfig.getLocalGuavaConfigs().stream().map(guavaConfig ->
                guavaConfig.getCacheName()).collect(Collectors.toList()));
    }

    public void clearLocal(String cacheName, Object key) {
        logger.info("清理本地缓存key:{},cacheName:{} ",key,cacheName);
        Cache cache = getCache(cacheName);
        if(cache == null) {
            return ;
        }
        GuavaRedisCache guaveRedisCache = (GuavaRedisCache) cache;
        guaveRedisCache.clearLocal(key);
    }


    @Override
    protected Collection<? extends Cache> loadCaches() {
        return  Collections.emptyList();
    }


    @Override
    public Cache getCache(String name) {
        Assert.notNull(this.guavaRedisConfig, "guavaRedisConfig 配置为空");
        Assert.notNull(name, "cacheName 为空");
        List<String> nameAndConfig=Arrays.asList(name.split("#"));
        Cache cache = super.getCache(nameAndConfig.get(0))
                ;
        return cache == null && this.dynamic ? this.createAndAddCache(nameAndConfig) : cache;
    }

    protected Cache createAndAddCache(List<String> nameAndConfig) {
        this.addCache(this.createCache(nameAndConfig));
        return super.getCache(nameAndConfig.get(0));
    }

    protected Cache createCache(List<String> nameAndConfig) {
        Optional<GuavaConfig> guavaRedisConfigOptional= guavaRedisConfig.getLocalGuavaConfigs().stream().
                filter(c -> nameAndConfig.get(0).equals(c.getCacheName())).findFirst();
        GuavaConfig guavaConfig=new GuavaConfig( nameAndConfig.get(0));
        if(guavaRedisConfigOptional.isPresent()){
            guavaConfig =guavaRedisConfigOptional.get();
        }else{
            parseExpression(nameAndConfig,guavaConfig);
        }
        long expiration = this.computeExpiration(guavaConfig);

        //没有配置redis;
        if(this.template==null){
            return this.guavaCache(guavaConfig);
        }
        return new GuavaRedisCache( nameAndConfig.get(0), this.cachePrefix.prefix( nameAndConfig.get(0)),this.template,this.guavaCache(guavaConfig),expiration,notice);
    }

    protected long computeExpiration(GuavaConfig guavaConfig) {
        Long expiration = null;
            if (guavaConfig.getRedisExpires() != null) {
                expiration = guavaConfig.getRedisExpires();
            }
        return expiration != null ? expiration : guavaConfig.getRedisDefaultExpiration();
    }



    public void setCacheNames(Collection<String> cacheNames) {
        Set<String> newCacheNames = CollectionUtils.isEmpty(cacheNames) ? Collections.emptySet() : new HashSet(cacheNames);
        this.dynamic = ((Set)newCacheNames).isEmpty();
    }


    public GuavaCache guavaCache(GuavaConfig guavaConfig){
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
        if(guavaConfig.getExpireAfterAccess() > 0) {
            cacheBuilder.expireAfterAccess(guavaConfig.getExpireAfterAccess(), TimeUnit.SECONDS);
        }
        if(guavaConfig.getExpireAfterWrite() > 0) {
            cacheBuilder.expireAfterWrite(guavaConfig.getExpireAfterWrite(), TimeUnit.SECONDS);
        }
        if(guavaConfig.getInitialCapacity() > 0) {
            cacheBuilder.initialCapacity(guavaConfig.getInitialCapacity());
        }
        if(guavaConfig.getMaximumSize() > 0) {
            cacheBuilder.maximumSize(guavaConfig.getMaximumSize());
        }
        if(guavaConfig.getRefreshAfterWrite() > 0) {
            cacheBuilder.refreshAfterWrite(guavaConfig.getRefreshAfterWrite(), TimeUnit.SECONDS);
        }
        return new GuavaCache(guavaConfig.getCacheName(),cacheBuilder.build());
    }

    @Override
    public void clearLocal(Object key,String cacheName){
        Cache cache =getCache(cacheName);
        if(cache!=null){
            logger.info("本地缓存开始 cacheName:{} key;{}  ",cacheName,key);
            GuavaRedisCache guavaRedisCache= (GuavaRedisCache) cache;
            guavaRedisCache.clearLocal(key);
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

    private void parseExpression(List<String> nameAndConfig, GuavaConfig guavaConfig) {
        StandardEvaluationContext context = new StandardEvaluationContext(guavaConfig);
        ExpressionParser parser = new SpelExpressionParser();
        for (int i = 1; i <nameAndConfig.size() ; i++) {
            parser.parseExpression(nameAndConfig.get(i)).getValue(context);
        }
    }


}
