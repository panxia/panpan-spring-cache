package com.cache.panpan.base.notice;

import com.cache.panpan.base.CacheClearManager;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @ClassName: MessageClearListener
 * @Description:
 * @Author: panxia
 * @Date: Create in 2019/8/23 4:52 PM
 * @Version:1.0
 */
public class MessageClearListener implements MessageListener {

   private RedisTemplate redisTemplate;
   private CacheClearManager cacheClearManager;


    public MessageClearListener(RedisTemplate redisTemplate, CacheClearManager cacheClearManager) {
        this.redisTemplate=redisTemplate;
    }

    @Override
    public void onMessage(Message message, byte[] bytes) {
       NoticeMessage noticeMessage= (NoticeMessage) redisTemplate.getValueSerializer().deserialize(message.getBody());
       cacheClearManager.clearLocal(noticeMessage.getKey(),noticeMessage.getCacheName());
    }
}
