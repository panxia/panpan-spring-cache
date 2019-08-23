# Spring 缓存封装 
#### 主要功能：
1. 支持本地（一级缓存） 和 redis （二级缓存）
2. 支持集群缓存清理（实现redis 订阅删除）
3. 自定义各级缓存失效时间，和失效时间
4. 基于Spring cache 封装，可以使用spring 注解 @Cacheable
5. 支持Spring 4（guava） 和Spring 5（caffeine） 二级缓存都使用redis
  
#### 相关配置  


 ```
  //Spring 4 配置
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
       /** 设置redis失效时间*/
         guavaConfig.setRedisExpires(6l);
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
         GuavaRedisCacheConfig guavaRedisConfig=new GuavaRedisCacheConfig(redisTemplate,guavaConfigs);
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
```

```
  //Spring 5 配置
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
      * 创建基于Caffeine的Cache Manager
      * @return
      */
     @Bean
     @Primary
     public CacheManager caffeineCacheManager() {
 
         /**
          * 本地缓存配置
          */
         CaffeineConfig caffeineConfig=new CaffeineConfig("我的缓存名称");
         /** 访问后过期时间，单位秒 默认5秒失效*/
         caffeineConfig.setCaffeineExpireAfterAccess(5);
         /** 最大缓存对象个数，超过此数量时之前放入的缓存将失效 默认为0*/
         caffeineConfig.setCaffeineMaximumSize(1000);
         /** 初始化大小 默认1000*/
         caffeineConfig.setCaffeineInitialCapacity(1000);
         /** 设置redis失效时间*/
          caffeineConfig.setRedisExpires(6l);
         /**
          * caffeineConfigs 本地缓存对象  可以不配置 支持生成默认配置
          * 需要自定义本地缓存需要配置
          */
         List<CaffeineConfig> caffeineConfigs=new ArrayList<>();
         caffeineConfigs.add(caffeineConfig);
         /**
          * 二级缓存配置 redis 缓存
          * redisExpires 6L  redis 失效时间
          * redisTemplate 对象
          * caffeineConfigs 本地缓存对象  可以不配置 支持生成默认配置
          */
         CaffeineRedisCacheConfig caffeineRedisConfig=new CaffeineRedisCacheConfig(redisTemplate,caffeineConfigs);
         RedisNotice redisNotice=new RedisNotice(redisTemplate,"topic-tes");
         caffeineRedisConfig.setNotice(redisNotice);
         return   new CaffeineRedisCacheManager(caffeineRedisConfig);
     }
 
 
     /**
      * 缓存清理监听
      * @param caffeineRedisCacheManager
      * @param redisTemplate
      * @return
      */
     @Bean
     public MessageClearListener messageClearListener(CacheManager caffeineRedisCacheManager,RedisTemplate redisTemplate) {
         return new MessageClearListener(redisTemplate, (CacheClearManager) caffeineRedisCacheManager);
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
        
```
#### 使用 

1、 相关注解   
1. @Cacheable可以标记在一个方法上，也可以标记在一个类上。   
当标记在一个方法上时表示该方法是支持缓存的，  
当标记在一个类上时则表示该类所有的方法都是支持缓存的
2. @CachePut也可以声明一个方法支持缓存功能。 
与@Cacheable不同的是使用@CachePut标注的方法 
在执行前不会去检查缓存中是否存在之前执行过的结果，  
而是每次都会执行该方法，并将执行结果以键值对的形式存入指定的缓存中。
3. @CacheEvict是用来标注在需要清除缓存元素的方法或类上的。  
当标记在一个类上时表示其中所有的方法的执行都会触发缓存的清除操作。     
***其他具体使用请自己百度️***

```
       //spring 注解 支持Spring SpringEl
   
       @Cacheable(value="users", key="#id")
       public User find(Integer id) {
          return null;
       }
    
       @Cacheable(value="users", key="#p0")
       public User find(Integer id) {
          return null;
       }
    
    
       @Cacheable(value="users", key="#user.id")
       public User find(User user) {
          returnnull;
    
       }
    
     
       @Cacheable(value="users", key="#p0.id")
       public User find(User user) {
    
          returnnull;
    
       }
```


#### 注意事项：
1. redis不配置 一级缓存也生效
2. 消息通知 自己可以重写 






