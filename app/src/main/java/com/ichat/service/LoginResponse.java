package com.ichat.service;

import android.os.Handler;
import android.os.Message;
import android.sub.Interface.Interfaces.ILoginResponse;
import android.sub.PubStruct;
import android.util.Log;

import com.ichat.Interface.MessageType;
import com.ichat.utility.HandlerHolder;

import imsub.interfaces.IMessage;

public class LoginResponse implements ILoginResponse {
	private String TAG = LoginResponse.class.getName();

	private HandlerHolder handlerHolder = null;

	public LoginResponse(HandlerHolder handlerHolder){
		this.handlerHolder = handlerHolder;
	}

	@Override
	public int OnRegisterOK(IMessage iMessage, PubStruct.UserInfo userInfo) {
		return 0;
	}

	@Override
	public int OnRegisterFailed(IMessage iMessage) {
		return 0;
	}

	@Override
	public int OnPreLoginOK(IMessage iMessage) {
		return 0;
	}

	@Override
	public int OnPreLoginFailed(IMessage iMessage) {
		return 0;
	}

	@Override
	public int OnEncryptLoginOK(final IMessage iMessage, final PubStruct.UserInfo userInfo) {
		Log.d(TAG,"OnEncryptLoginOK-->"+iMessage.toString());

		//inite sqlite
		/*
		String userid = userInfo.get(PubStruct.UserInfo.UserID);
		if(userid.length() == 0 || userid != null){
			PubRequestHolder.getInstance().initSqlite(userid);
		}

		Cursor friendlist_cur = PubRequestHolder.getInstance().getDBOperator().getFriendListDao().query(null,null,null,null);
		if(friendlist_cur.isNull(-1)){
			Log.d(TAG,"frendlist table is blank");
		}else{
			Log.d(TAG,"frendlist table is not blank");
		}
		*/

		//notify ui
		handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
			@Override
			public boolean SendAction(Integer key, Handler handler) {
				Message msg = handler.obtainMessage();
				msg.obj = userInfo;
				msg.what = MessageType.LOGIN_MSG_OK.ordinal();
				handler.sendMessage(msg);
				return true;
			}
		});
		return 0;
	}

	@Override
	public int OnEncryptLoginFailed(final IMessage iMessage) {
		Log.d(TAG,iMessage.toString());
		handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
			@Override
			public boolean SendAction(Integer key, Handler handler) {
				Message msg = handler.obtainMessage();
				msg.obj = iMessage;
				msg.what = MessageType.LOGIN_MSG_FAILED.ordinal();
				handler.sendMessage(msg);
				return true;
			}
		});
		return 0;
	}

	//该方法已废用
	@Override
	public int LoginResponse(final IMessage params) {
		Log.d(TAG,params.toString());
		return 0;
	}

	@Override
	public int OnLogOutOK(final IMessage iMessage) {
		Log.d(TAG,iMessage.toString());
		handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
			@Override
			public boolean SendAction(Integer key, Handler handler) {
				Message msg = handler.obtainMessage();
				msg.obj = iMessage;
				msg.what = MessageType.LOGOUT_MSG_OK.ordinal();
				handler.sendMessage(msg);
				msg = null;
				return true;
			}
		});
		return 0;
	}

	@Override
	public int OnLogOutFailed(final IMessage iMessage) {
		Log.d(TAG,iMessage.toString());
		handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
			@Override
			public boolean SendAction(Integer key, Handler handler) {
				Message msg = handler.obtainMessage();
				msg.obj = iMessage;
				msg.what = MessageType.LOGOUT_MSG_FAILED.ordinal();
				handler.sendMessage(msg);
				msg = null;
				return true;
			}
		});
		return 0;
	}

	@Override
	public int OnRecvKickMsg(final IMessage iMessage){
		Log.d(TAG,iMessage.toString());
		handlerHolder.sendMsgToAll(new HandlerHolder.ISendAction() {
			@Override
			public boolean SendAction(Integer key, Handler handler) {
				Message msg = handler.obtainMessage();
				msg.obj = iMessage;
				msg.what = MessageType.RECVKICK_MSG.ordinal();
				handler.sendMessage(msg);
				msg = null;
				return true;
			}
		});
		return 0;
	}

	@Override
	public int ReLoginResponse(IMessage iMessage) {
		return 0;
	}

	@Override
	public int LoginStatusResponse(IMessage iMessage) {
		return 0;
	}

}
