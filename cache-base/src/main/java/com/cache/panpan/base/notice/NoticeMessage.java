package com.cache.panpan.base.notice;

import java.io.Serializable;

/**
 * @ClassName: Message
 * @Description: 缓存通知消息
 * @Author: panxia
 * @Date: Create in 2019/8/16 5:25 PM
 * @Version:1.0
 */
public class NoticeMessage implements Serializable {

    private Object key;

    private String cacheName;

    public NoticeMessage() {
    }

    public NoticeMessage(Object key, String cacheName) {
        this.key = key;
        this.cacheName = cacheName;
    }

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }


    @Override
    public String toString() {
        return "Message{" +
                "key=" + key +
                ", cacheName='" + cacheName + '\'' +
                '}';
    }
}
