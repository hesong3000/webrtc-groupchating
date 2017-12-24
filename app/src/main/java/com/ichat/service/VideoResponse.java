package com.ichat.service;

import android.os.Handler;
import android.os.Message;
import android.sub.Interface.Interfaces;
import android.util.Log;

import com.ichat.Interface.MessageType;
import com.ichat.utility.HandlerHolder;

import imsub.interfaces.IMessage;

/**
 * Created by gaoqi on 2016/8/5.
 */
public class VideoResponse implements Interfaces.IVideoResponse{

    private String TAG = VideoResponse.class.getName();

    private HandlerHolder handlerHolder = null;

    public VideoResponse(HandlerHolder handlerHolder){
        this.handlerHolder = handlerHolder;
    }

    @Override
    public int RecvInviteVideoMsg(final IMessage iMessage) {
        Log.d(TAG,iMessage.toString());
        handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
            @Override
            public boolean SendAction(Integer key, Handler handler) {
                Message msg = handler.obtainMessage();
                msg.obj = iMessage;
                msg.what = MessageType.RECVINVITEVIDEO_MSG.ordinal();
                handler.sendMessage(msg);
                return true;
            }
        });
        return 0;
    }

    @Override
    public int RecvCancelInviteVideoMsg(final IMessage iMessage) {
        Log.d(TAG,iMessage.toString());
        handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
            @Override
            public boolean SendAction(Integer key, Handler handler) {
                Message msg = handler.obtainMessage();
                msg.obj = iMessage;
                msg.what = MessageType.RECVCANCELVIDEO_MSG.ordinal();
                handler.sendMessage(msg);
                return true;
            }
        });
        return 0;
    }

    @Override
    public int RecvAcceptVideoMsg(final IMessage iMessage) {
        Log.d(TAG,iMessage.toString());
        handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
            @Override
            public boolean SendAction(Integer key, Handler handler) {
                Message msg = handler.obtainMessage();
                msg.obj = iMessage;
                msg.what = MessageType.RECVACCEPTVIDEO_MSG.ordinal();
                handler.sendMessage(msg);
                return true;
            }
        });
        return 0;
    }

    @Override
    public int RecvRefuseVideoMsg(final IMessage iMessage) {
        Log.d(TAG,iMessage.toString());
        handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
            @Override
            public boolean SendAction(Integer key, Handler handler) {
                Message msg = handler.obtainMessage();
                msg.obj = iMessage;
                msg.what = MessageType.RECVREFUSEVIDEO_MSG.ordinal();
                handler.sendMessage(msg);
                return true;
            }
        });
        return 0;
    }

    @Override
    public int RecvCloseVideoMsg(final IMessage iMessage) {
        Log.d(TAG,iMessage.toString());
        handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
            @Override
            public boolean SendAction(Integer key, Handler handler) {
                Message msg = handler.obtainMessage();
                msg.obj = iMessage;
                msg.what = MessageType.RECVCLOSEVIDEO_MSG.ordinal();
                handler.sendMessage(msg);
                return true;
            }
        });
        return 0;
    }

    @Override
    public int RecvOpenRemoteVideoDeviceMsg(final IMessage iMessage) {
        Log.d("data",iMessage.toString());
        handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
            @Override
            public boolean SendAction(Integer key, Handler handler) {
                Message msg = handler.obtainMessage();
                msg.obj = iMessage;
                msg.what = MessageType.OPENVIDEODEVICE_MSG.ordinal();
                handler.sendMessage(msg);
                return true;
            }
        });
        return 0;
    }

    @Override
    public int RecvOpenRemoteVIdeoDeviceResultMsg(IMessage iMessage) {
        return 0;
    }

    @Override
    public int RecvCloseRemoteVideoDeviceMsg(IMessage iMessage) {
        return 0;
    }

    @Override
    public int OpenRemoteVideoDeviceResultFailed(IMessage iMessage) {
        return 0;
    }

    @Override
    public int RecvSwitchVideoToAudioMsg(IMessage iMessage) {
        return 0;
    }
}
