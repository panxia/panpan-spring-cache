package com.cache.panpan.base.notice;

import com.cache.panpan.base.exception.MessageException;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @ClassName: RedisNotice
 * @Description:
 * @Author: panxia
 * @Date: Create in 2019/8/23 3:52 PM
 * @Version:1.0
 */
public class RedisNotice implements  IClearNotice  {

    private RedisTemplate redisTemplate;
    private String topic;

    public RedisNotice(RedisTemplate redisTemplate,String topic ) {
        this.redisTemplate=redisTemplate;
        this.topic=topic;
    }

    @Override
    public void send(NoticeMessage message) throws MessageException {
        redisTemplate.convertAndSend(topic,message);
    }


    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
