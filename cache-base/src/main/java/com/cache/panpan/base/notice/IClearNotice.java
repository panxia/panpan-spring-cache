package com.cache.panpan.base.notice;

import com.cache.panpan.base.exception.MessageException;


/**
 * @ClassName: IMessageSend
 * @Description:
 * @Author: panxia
 * @Date: Create in 2019/8/16 5:32 PM
 * @Version:1.0
 */
public  interface IClearNotice {


    /**
     * 用于消息通知发送
     * @param message
     * @throws MessageException
     */
      void  send(NoticeMessage message) throws MessageException;




}
