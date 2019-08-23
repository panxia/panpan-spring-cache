package com.bj58.chr.base.notice;

import com.bj58.chr.base.CacheClearManager;
import com.bj58.chr.base.exception.MessageException;


/**
 * @ClassName: IMessageSend
 * @Description:
 * @Author: panxia
 * @Date: Create in 2019/8/16 5:32 PM
 * @Version:1.0
 */
public abstract class AbstractNotice {

    private CacheClearManager cacheClearManager=null;

    /**
     * 用于消息通知发送
     * @param message
     * @throws MessageException
     */
   public abstract   void  send(Message message) throws MessageException;



    /**
     * 用于接收清除缓存消息
     */
    public abstract   void  onClearMessage();

    /**
     * 用于清楚缓存
     * @param message
     * @throws MessageException
     */
    public  void   clear(Message message ){
        cacheClearManager.clearLocal(message.getKey(),message.getCacheName());
    }


    public CacheClearManager getCacheClearManager() {
        return cacheClearManager;
    }

    public void setCacheClearManager(CacheClearManager cacheClearManager) {
        this.cacheClearManager = cacheClearManager;
    }
}
