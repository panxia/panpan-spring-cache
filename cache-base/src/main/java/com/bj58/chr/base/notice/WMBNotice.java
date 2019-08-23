package com.bj58.chr.base.notice;

import com.bj58.chr.common.JSONUtils;
import com.bj58.chr.common.ProfileUtils;
import com.bj58.spat.esbclient.*;
import com.bj58.spat.esbclient.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @ClassName: WMBNotice
 * @Description: 默认只支持测试环境消息发送，线上环境请自己制定key 主题 和客户端
 * @Author: panxia
 * @Date: Create in 2019/8/19 6:03 PM
 * @Version:1.0
 */
public class WMBNotice extends AbstractNotice {


    private final static Logger LOGGER = LoggerFactory.getLogger(WMBNotice.class);

    private  ESBClient sendClient;

    private  ESBClient receiveClient;

    private  int  subjectId= 102744;

    private   int clientId = 1;

    private String sendKey;

    private String  receiveKey;

    private static  final String DEFAULT_SEND_KEY= "cache_notice_send.key";

    private static  final String DEFAULT_RECEIVE_KEY= "cache_notice_receive.key";

    public void init() throws Exception {
       initSendWmb();
       initReceiveWmb();
    }

    private void initSendWmb() throws Exception {
        String configPath;
        if(sendKey==null){
            //默认情况
            if (ProfileUtils.online()) {
                configPath = ProfileUtils.path(DEFAULT_SEND_KEY);
            } else {
                configPath =  WMBNotice.class.getClassLoader().getResource(DEFAULT_SEND_KEY).getPath();
            }
        }else{
            //自定义配置情况
            configPath=sendKey;
        }
        sendClient = new ESBClient(configPath , Runtime.getRuntime().availableProcessors()*2);

    }

    private void initReceiveWmb() throws Exception {
        String configPath;
        if(receiveKey==null){
            //默认情况
            if (ProfileUtils.online()) {
                configPath = ProfileUtils.path(DEFAULT_RECEIVE_KEY);
            } else {
                configPath =  WMBNotice.class.getClassLoader().getResource(DEFAULT_RECEIVE_KEY).getPath();
            }
        }else{
            //自定义配置情况
            configPath=receiveKey;
        }

        receiveClient = new ESBClient(configPath , Runtime.getRuntime().availableProcessors()*2);
    }

    public WMBNotice() throws Exception{
        init();
        onClearMessage();
    }

    public WMBNotice(int subjectId, int clientId)throws Exception {
      this(null,null,subjectId,clientId);
    }

    public WMBNotice(String sendKeyPath, String receiveKeyPath, int subjectId, int clientId) throws Exception {
        this.sendKey=sendKeyPath;
        this.receiveKey=receiveKeyPath;
        this.subjectId=subjectId;
        this.clientId=clientId;
        init();
        onClearMessage();
    }

    @Override
    public void send(Message message)  {
        try {
            LOGGER.info("清楚集群缓存发送:{}",message);
            sendClient.send(new ESBMessage(subjectId, 	JSONUtils.writeValue(message).getBytes(), new SendCallback(){
                @Override
                public void onSuccess() {
                    LOGGER.info("清楚集群缓存发送成功:{}",message);
                }
                @Override
                public void onFailed() {
                    LOGGER.error("清楚集群缓存发送失败:{}",message);
                }
            }));
        } catch (Exception e) {
            LOGGER.error("清楚集群缓存发送失败:{}",message);
            e.printStackTrace();
        }
    }

    @Override
    public void onClearMessage() {
        try {
            receiveClient.setReceiveSubject(new ESBSubject(subjectId, clientId, SubMode.BROADCASTING));
            receiveClient.setReceiveHandler(new ESBReceiveHandler(){
                @Override
                public void messageReceived(ESBMessage esbMessage) {
                    Message message =  JSONUtils.readValue(esbMessage.getBody(), Message.class);
                    clear(message);
                }
            });
            receiveClient.startBroadcast();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
        } catch (SerializeException e) {
            e.printStackTrace();
        } catch (CommunicationException e) {
            e.printStackTrace();
        } catch (ClientInitException e) {
            e.printStackTrace();
        } catch (ConsumebackOutOfBoundException e) {
            e.printStackTrace();
        }
    }


}
