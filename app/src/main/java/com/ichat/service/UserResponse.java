package com.ichat.service;

import android.os.Handler;
import android.os.Message;
import android.sub.Interface.Interfaces;
import android.sub.PubStruct;
import android.util.Log;

import com.ichat.Interface.MessageType;
import com.ichat.utility.HandlerHolder;

import java.util.List;

import imsub.interfaces.IMessage;

/**
 * Created by Administrator on 2016/7/19.
 */
public class UserResponse implements Interfaces.IUserResponse {

    private String TAG = UserResponse.class.getName();

    private HandlerHolder handlerHolder = null;

    public UserResponse(HandlerHolder handlerHolder){
        this.handlerHolder = handlerHolder;
    }

    @Override
    public int OnGetFriendListOK(final IMessage iMessage, final List<PubStruct.FriendInfo> list) {
        Log.d(TAG,iMessage.toString());
        handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
            @Override
            public boolean SendAction(Integer key, Handler handler) {
                Message msg = handler.obtainMessage();
                msg.obj = list;
                msg.what = MessageType.FRIENDLIST_MSG_OK.ordinal();
                handler.sendMessage(msg);
                return true;
            }
        });
        return 0;
    }

    @Override
    public int OnGetFriendListFailed(IMessage iMessage) {
        return 0;
    }

    @Override
    public int OnGetUserInfoOK(final IMessage iMessage, final PubStruct.UserInfo userInfo) {
        Log.d(TAG,iMessage.toString());
        handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
            @Override
            public boolean SendAction(Integer key, Handler handler) {
                Message msg = handler.obtainMessage();
                msg.obj = userInfo;
                msg.what =  MessageType.GETUSERINFOR_MSG_OK.ordinal();
                handler.sendMessage(msg);
                return true;
            }
        });
        return 0;
    }

    @Override
    public int OnGetUserInfoFailed(IMessage iMessage) {
        return 0;
    }

    @Override
    public int OnGetUserInfoByUserIDOK(IMessage iMessage, final PubStruct.UserInfo userInfo) {
        Log.d(TAG,iMessage.toString());
        handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
            @Override
            public boolean SendAction(Integer key, Handler handler) {
                Message msg = handler.obtainMessage();
                msg.obj = userInfo;
                msg.what =  MessageType.GETUSERINFOBYUSERID_MSG_OK.ordinal();
                handler.sendMessage(msg);
                return true;
            }
        });
        return 0;
    }

    @Override
    public int OnGetUserInfoByUserIDFailed(final IMessage iMessage) {
        Log.d(TAG,iMessage.toString());
        handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
            @Override
            public boolean SendAction(Integer key, Handler handler) {
                Message msg = handler.obtainMessage();
                msg.obj = iMessage;
                msg.what = MessageType.GETUSERINFOBYUSERID_MSG_FAILED.ordinal();
                handler.sendMessage(msg);
                return true;
            }
        });
        return 0;
    }

    @Override
    public int OnGetUserInfoByAccountOK(IMessage iMessage, final PubStruct.UserInfo userInfo) {
        Log.d(TAG,iMessage.toString());
        handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
            @Override
            public boolean SendAction(Integer key, Handler handler) {
                Message msg = handler.obtainMessage();
                msg.obj = userInfo;
                msg.what =  MessageType.GETUSERINFOBYACCOUNT_MSG_OK.ordinal();
                handler.sendMessage(msg);
                return true;
            }
        });
        return 0;
    }

    @Override
    public int OnGetUserInfoByAccountFailed(final IMessage iMessage) {
        Log.d(TAG,iMessage.toString());
        handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
            @Override
            public boolean SendAction(Integer key, Handler handler) {
                Message msg = handler.obtainMessage();
                msg.obj = iMessage;
                msg.what = MessageType.GETUSERINFOBYACCOUNT_MSG_FAILED.ordinal();
                handler.sendMessage(msg);
                return true;
            }
        });
        return 0;
    }

    @Override
    public int OnIsNotFriend(IMessage iMessage) {
        return 0;
    }

    @Override
    public int OnIsFriend(IMessage iMessage, PubStruct.UserInfo userInfo) {
        return 0;
    }

    @Override
    public int OnReceiveRequestFriendMsg(IMessage iMessage, final PubStruct.UserInfo userInfo) {
        Log.d(TAG,iMessage.toString());
        handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
            @Override
            public boolean SendAction(Integer key, Handler handler) {
                Message msg = handler.obtainMessage();
                msg.obj = userInfo;
                msg.what =  MessageType.RECVREQUESTFRIEDN_MSG.ordinal();
                handler.sendMessage(msg);
                return true;
            }
        });
        return 0;
    }

    @Override
    public int OnRequestFriendTempChat(final IMessage iMessage) {
        Log.d(TAG,iMessage.toString());
        handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
            @Override
            public boolean SendAction(Integer key, Handler handler) {
                Message msg = handler.obtainMessage();
                msg.obj = iMessage;
                msg.what =  MessageType.REQUESTFRIENDTMPCHAT_MSG.ordinal();
                handler.sendMessage(msg);
                return true;
            }
        });
        return 0;
    }

    @Override
    public int OnRequestFriendRefuse(final IMessage iMessage) {
        Log.d(TAG,iMessage.toString());
        handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
            @Override
            public boolean SendAction(Integer key, Handler handler) {
                Message msg = handler.obtainMessage();
                msg.obj = iMessage;
                msg.what =  MessageType.REQUESTFRIENDREFUSE_MSG.ordinal();
                handler.sendMessage(msg);
                return true;
            }
        });
        return 0;
    }

    @Override
    public int OnRequestFriendAccept(final IMessage iMessage) {
        Log.d(TAG,iMessage.toString());
        handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
            @Override
            public boolean SendAction(Integer key, Handler handler) {
                Message msg = handler.obtainMessage();
                msg.obj = iMessage;
                msg.what =  MessageType.REQUESTFRIENNDACCEPT_MSG.ordinal();
                handler.sendMessage(msg);
                return true;
            }
        });
        return 0;
    }

    @Override
    public int OnDeleteFriendOK(IMessage iMessage) {
        return 0;
    }

    @Override
    public int OnDeleteFriendFailed(IMessage iMessage) {
        return 0;
    }

    @Override
    public int OnModifyUserInfoOK(IMessage iMessage, final PubStruct.UserInfo userInfo) {
        Log.d("OnModifyUserInfoOK",iMessage.toString());
        handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
            @Override
            public boolean SendAction(Integer key, Handler handler) {
                Message msg = handler.obtainMessage();
                msg.obj = userInfo;
                msg.what = MessageType.MODIFYUSERINFO_MSG_OK.ordinal();
                handler.sendMessage(msg);
                return true;
            }
        });
        return 0;
    }

    @Override
    public int OnModifyUserInfoFailed(IMessage iMessage) {
        return 0;
    }

    @Override
    public int OnModifyFriendInfoOK(IMessage iMessage, PubStruct.FriendInfo friendInfo) {
        return 0;
    }

    @Override
    public int OnModifyFriendInfoFailed(IMessage iMessage) {
        return 0;
    }

    @Override
    public int OnUserAvatarUploadOK(IMessage iMessage) {
        return 0;
    }

    @Override
    public int OnUserAvatarUploadFailed(IMessage iMessage) {
        Log.d("UserAvatarDownloadFail",iMessage.toString());
        return 0;
    }

    @Override
    public int OnUserAvatarDownloadOK(final IMessage iMessage) {
        Log.d("OnUserAvatarDownloadOK",iMessage.toString());
        handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
            @Override
            public boolean SendAction(Integer key, Handler handler) {
                Message msg = handler.obtainMessage();
                msg.obj = iMessage;
                msg.what = MessageType.AVATARDOWNLOAD_MSG_OK.ordinal();
                handler.sendMessage(msg);
                return true;
            }
        });

        return 0;
    }

    @Override
    public int OnUserAvatarDownloadFailed(IMessage iMessage) {
        return 0;
    }
    @Override
    public int OnAddBlacklistOK(IMessage iMessage) {
        return 0;
    }

    @Override
    public int OnAddBlacklistFailed(IMessage iMessage) {
        return 0;
    }

    @Override
    public int OnDeleteBlacklistOK(IMessage iMessage) {
        return 0;
    }

    @Override
    public int OnDeleteBlacklistFailed(IMessage iMessage) {
        return 0;
    }

    @Override
    public int OnGetBlacklistOK(IMessage iMessage, List<IMessage> list) {
        return 0;
    }

    @Override
    public int OnGetBlacklistFailed(IMessage iMessage) {
        return 0;
    }

    @Override
    public int OnFriendAddThirdResponseOK(IMessage iMessage) {
        return 0;
    }

    @Override
    public int OnFriendAddThirdResponseFailed(IMessage iMessage) {
        return 0;
    }

    @Override
    public int OnModifyPasswordOK(IMessage iMessage) {
        return 0;
    }

    @Override
    public int OnModifyPasswordFailed(IMessage iMessage) {
        return 0;
    }

    @Override
    public int RecvTransferMsg(IMessage iMessage) {
        return 0;
    }
}

