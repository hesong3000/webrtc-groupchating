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
public class AudioResponse implements Interfaces.IAudioResponse{

    private String TAG = AudioResponse.class.getName();

    private HandlerHolder handlerHolder = null;

    public AudioResponse(HandlerHolder handlerHolder){
        this.handlerHolder = handlerHolder;
    }

    @Override
    public int RecvInviteAudioMsg(final IMessage iMessage) {
        Log.d(TAG,iMessage.toString());
        handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
            @Override
            public boolean SendAction(Integer key, Handler handler) {
                Message msg = handler.obtainMessage();
                msg.obj = iMessage;
                msg.what = MessageType.RECVINVITEVOICE_MSG.ordinal();
                handler.sendMessage(msg);
                return true;
            }
        });
        return 0;
    }

    @Override
    public int RecvCancelInviteAudioMsg(final IMessage iMessage) {
        Log.d(TAG,iMessage.toString());
        handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
            @Override
            public boolean SendAction(Integer key, Handler handler) {
                Message msg = handler.obtainMessage();
                msg.obj = iMessage;
                msg.what = MessageType.RECVCANCELVOICE_MSG.ordinal();
                handler.sendMessage(msg);
                return true;
            }
        });
        return 0;
    }

    @Override
    public int RecvAcceptAudioMsg(final IMessage iMessage) {
        Log.d(TAG,iMessage.toString());
        handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
            @Override
            public boolean SendAction(Integer key, Handler handler) {
                Message msg = handler.obtainMessage();
                msg.obj = iMessage;
                msg.what = MessageType.RECVACCEPTVOICE_MSG.ordinal();
                handler.sendMessage(msg);
                return true;
            }
        });
        return 0;
    }

    @Override
    public int RecvRefuseAudioMsg(final IMessage iMessage) {
        Log.d(TAG,iMessage.toString());
        handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
            @Override
            public boolean SendAction(Integer key, Handler handler) {
                Message msg = handler.obtainMessage();
                msg.obj = iMessage;
                msg.what = MessageType.RECVREFUSEVOICE_MSG.ordinal();
                handler.sendMessage(msg);
                return true;
            }
        });
        return 0;
    }

    @Override
    public int RecvCloseAudioMsg(final IMessage iMessage) {
        Log.d(TAG,iMessage.toString());
        handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
            @Override
            public boolean SendAction(Integer key, Handler handler) {
                Message msg = handler.obtainMessage();
                msg.obj = iMessage;
                msg.what = MessageType.RECVCLOSEVOICE_MSG.ordinal();
                handler.sendMessage(msg);
                return true;
            }
        });
        return 0;
    }

    @Override
    public int RecvOpenRemoteAudioDeviceMsg(final IMessage iMessage) {
        Log.d("data",iMessage.toString());
        handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
            @Override
            public boolean SendAction(Integer key, Handler handler) {
                Message msg = handler.obtainMessage();
                msg.obj = iMessage;
                msg.what = MessageType.OPENAUDIODEVICE_MSG.ordinal();
                handler.sendMessage(msg);
                return true;
            }
        });
        return 0;
    }

    @Override
    public int OpenRemoteAudioDeviceResultFailed(IMessage iMessage) {
        return 0;
    }

    @Override
    public int RecvCloseRemoteAudioDeviceMsg(IMessage iMessage) {
        return 0;
    }
}
