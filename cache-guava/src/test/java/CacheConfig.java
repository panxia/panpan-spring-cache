import com.cache.panpan.base.CacheClearManager;
import com.cache.panpan.base.notice.MessageClearListener;
import com.cache.panpan.base.notice.RedisNotice;
import com.cache.panpan.common.guava.GuavaConfig;
import com.cache.panpan.common.guava.GuavaRedisCacheConfig;
import com.cache.panpan.common.guava.GuavaRedisCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: CacheConfig
 * @Description:
 * @Author: panxia
 * @Date: Create in 2019/8/23 4:32 PM
 * @Version:1.0
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private final static Logger LOGGER= LoggerFactory.getLogger(CacheConfig.class);

    @Resource
    RedisTemplate redisTemplate;


    /**
     * 创建基于Guava的Cache Manager
     * @return
     */
    @Bean
    @Primary
    public CacheManager guavaCacheManager() {

        /**
         * 本地缓存配置
         */
        GuavaConfig guavaConfig=new GuavaConfig("我的缓存名称");
        /** 访问后过期时间，单位秒 默认5秒失效*/
        guavaConfig.setGuavaExpireAfterAccess(5);
        /** 最大缓存对象个数，超过此数量时之前放入的缓存将失效 默认为0*/
        guavaConfig.setGuavaMaximumSize(1000);
        /** 初始化大小 默认1000*/
        guavaConfig.setGuavaInitialCapacity(1000);
        /**
         * guavaConfigs 本地缓存对象  可以不配置 支持生成默认配置
         * 需要自定义本地缓存需要配置
         */
        List<GuavaConfig> guavaConfigs=new ArrayList<>();
        guavaConfigs.add(guavaConfig);
        /**
         * 二级缓存配置 redis 缓存
         * redisExpires 6L  redis 失效时间
         * redisTemplate 对象
         * guavaConfigs 本地缓存对象  可以不配置 支持生成默认配置
         */
        GuavaRedisCacheConfig guavaRedisConfig=new GuavaRedisCacheConfig(6L,redisTemplate,guavaConfigs);
        /**
         * 消息通知发送配置
         */
        RedisNotice redisNotice=new RedisNotice(redisTemplate,"topic-tes");
        guavaRedisConfig.setNotice(redisNotice);
        return   new GuavaRedisCacheManager(guavaRedisConfig);
    }


    /**
     * 消息缓存清理监听
     * @param guavaRedisCacheManager
     * @param redisTemplate
     * @return
     */
    @Bean
    public MessageClearListener messageClearListener(CacheManager guavaRedisCacheManager,RedisTemplate redisTemplate) {
        return new MessageClearListener(redisTemplate, (CacheClearManager) guavaRedisCacheManager);
    }

    /**
     * redis相关配置
     * @param redisConnectionFactory
     * @param messageClearListener
     * @return
     */
    @Bean
    public RedisMessageListenerContainer reedisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory,
                                                                          MessageClearListener messageClearListener) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(Runtime.getRuntime().availableProcessors()*2);
        threadPoolTaskExecutor.setThreadNamePrefix("taskExecutor-");
        threadPoolTaskExecutor.setQueueCapacity(10000);
        //线程空闲存活最大时间
        threadPoolTaskExecutor.setKeepAliveSeconds(20);
        threadPoolTaskExecutor.initialize();
        redisMessageListenerContainer.setTaskExecutor(threadPoolTaskExecutor);

        ChannelTopic channelTopic = new ChannelTopic("topic-tes");
        redisMessageListenerContainer.addMessageListener(messageClearListener, channelTopic);
        redisMessageListenerContainer.setErrorHandler((Throwable var) ->LOGGER.error(var.getMessage()));

        return redisMessageListenerContainer;
    }








}
