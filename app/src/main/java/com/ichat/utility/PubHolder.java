package com.ichat.utility;

import android.os.Handler;
import android.os.Message;
import android.sub.InstanceHolder;

import android.sub.Interface.Interfaces;
import android.util.Log;

import com.ichat.Interface.HandlerType;
import com.ichat.Interface.MessageType;
import com.ichat.service.AudioResponse;
import com.ichat.service.LoginResponse;
import com.ichat.service.UserResponse;
import com.ichat.service.VideoResponse;

import java.io.InputStream;
import imsub.interfaces.LogWriterProvider;

public class PubHolder{
    private String TAG = PubHolder.class.getName();

    private static final PubHolder instance = new PubHolder();
    public static PubHolder getInstance(){
        return instance;
    }

    private Interfaces.ILoginRequest login = null;
    public Interfaces.ILoginRequest  getLoginInstance(){
        return login;
    }

    private Interfaces.IUserRequest userRequest = null;
    public Interfaces.IUserRequest getUserRequest(){
        return userRequest;
    }

    private Interfaces.IChatRequest chat = null;
    public Interfaces.IChatRequest getChatInstacne(){
        return chat;
    }

    private Interfaces.IFileRequest filereq = null;
    public Interfaces.IFileRequest getFileReqInstance(){
        return filereq;
    }

    private Interfaces.IGroupRequest groupreq = null;
    public Interfaces.IGroupRequest getGroupreq(){ return groupreq; }

    private Interfaces.IAudioRequest audioreq = null;
    public Interfaces.IAudioRequest getAudioreq(){ return audioreq; }

    private Interfaces.IVideoRequest videoreq = null;
    public Interfaces.IVideoRequest getVideoreq(){ return videoreq; }

    private InstanceHolder instanceHolder = null;

    //Handler管理相关的实例
    HandlerHolder loginHandlerHolder = new HandlerHolder();
    HandlerHolder chatHandlerHolder = new HandlerHolder();
    HandlerHolder fileHandlerHolder = new HandlerHolder();
    HandlerHolder userHandlerHolder = new HandlerHolder();
    HandlerHolder groupHandlerHolder = new HandlerHolder();
    HandlerHolder audioHandlerHolder = new HandlerHolder();
    HandlerHolder videoHandlerHolder = new HandlerHolder();
    HandlerHolder thisHandlerHolder = new HandlerHolder();

    private boolean isInited = false;
    private String m_path;
    private InputStream m_is;
    public boolean inite(String path, InputStream is,Handler handler){

        addMainHandler(HandlerType.PUBHOLDER_MAINHANDLER.ordinal(),handler);
        m_path = path;
        m_is = is;
        new Thread() {
            public void run() {
                final boolean ret = threadedInite();
                thisHandlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
                    @Override
                    public boolean SendAction(Integer key, Handler handler) {
                        Message msg = handler.obtainMessage();
                        if (ret)
                            msg.obj = "OK";
                        else
                            msg.obj = "Error";
                        msg.what = MessageType.INITE_MSG.ordinal();
                        handler.sendMessage(msg);
                        return true;
                    }
                });
            }
        }.start();
        return true;
    }


    private boolean threadedInite(){
        if(isInited) {
            Log.i("GQTEST","PUBHOLDER IS INITED ALREADY!!!");
            return true;
        }
        try {
            //inite android sub
            LogWriterProvider.setLogWriter(new LogWriter());
            Log.i("GQTEST", "Inite InstanceHolder...");
            instanceHolder = InstanceHolder.getInstance();

            if (instanceHolder.Init(m_path,m_is) == -1) {
                LogWriterProvider.getLogWriter().e("GQTEST", "Inite Failed!!!");
                return false;
            }

            Log.i("GQTEST", "Inite Login module");
            instanceHolder.setLoginResponseInstance(new LoginResponse(loginHandlerHolder));
            login = instanceHolder.getLoginInstance();

            Log.i("GQTEST", "Inite User module");
            instanceHolder.setUserResponse(new UserResponse(userHandlerHolder));
            userRequest = instanceHolder.getUserInstance();

            Log.i("GQTEST", "Inite Audio module");
            instanceHolder.setAudioResponse(new AudioResponse(audioHandlerHolder));
            audioreq = instanceHolder.getAudioRequestInstance();

            Log.i("GQTEST", "Inite Video module");
            instanceHolder.setVideoResponse(new VideoResponse(videoHandlerHolder));
            videoreq = instanceHolder.getVideoRequestInstance();

            Log.i("GQTEST", "Inite complete");
        }catch (Exception e){
            //e.getMessage();
            e.printStackTrace();
            return false;
        }

        isInited = true;
        return true;
    }

    public void addMainHandler(Integer key,Handler handler){
        thisHandlerHolder.registHandler(key,handler);
    }
    public void removeMainHandler(Integer key){
        thisHandlerHolder.unregistHandler(key);
    }

    /**
     * zhangdoudou
     */
    public void addUserHandler(Integer key,Handler handler){
        userHandlerHolder.registHandler(key,handler);
    }
    public void removeUserHandler(Integer key){
        userHandlerHolder.unregistHandler(key);
    }

    public void addFileHandler(Integer key,Handler handler){
        fileHandlerHolder.registHandler(key,handler);
    }
    public void removeFileHandler(Integer key){
        fileHandlerHolder.unregistHandler(key);
    }

    public void addLoginHandler(Integer key,Handler handler){
        loginHandlerHolder.registHandler(key,handler);
    }
    public void removeLoginHandler(Integer key){
        loginHandlerHolder.unregistHandler(key);
    }

    public void addChatHandler(Integer key,Handler handler){
        chatHandlerHolder.registHandler(key,handler);
    }
    public void removeChatHandler(Integer key){
        chatHandlerHolder.unregistHandler(key);
    }

    public void addGroupHandler(Integer key,Handler handler){
        groupHandlerHolder.registHandler(key,handler);
    }

    public void removeGroupHandler(Integer key){ groupHandlerHolder.unregistHandler(key); }

    public void addAudioHandler(Integer key,Handler handler){
        audioHandlerHolder.registHandler(key,handler);
    }

    public void removeAudioHandler(Integer key){ audioHandlerHolder.unregistHandler(key); }

    public void addVideoHandler(Integer key,Handler handler){
        videoHandlerHolder.registHandler(key,handler);
    }

    public void removeVideoHandler(Integer key){ videoHandlerHolder.unregistHandler(key); }
}
