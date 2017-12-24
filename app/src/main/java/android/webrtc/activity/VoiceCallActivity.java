package android.webrtc.activity;

import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.sub.Interface.Interfaces;
import android.sub.PubStruct;
import android.sub.ShareData;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webrtc.proxy.WebRTCProxy;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ichat.Interface.HandlerType;
import com.ichat.Interface.MessageType;
import com.ichat.common.ImageUtils;
import com.ichat.utility.ApplicationSession;
import com.ichat.utility.PubHolder;

import java.util.UUID;

import imsub.interfaces.IMessage;
import imsub.interfaces.MessageProvider;

public class VoiceCallActivity extends CallActivity implements View.OnClickListener {

    private static final String TAG = "VoiceCallActivity";

    private LinearLayout voicecall_root_layout;
    private ImageView voicecall_user_avartar;
    private TextView voicecall_nickname;
    private TextView voicecall_state;
    private LinearLayout voice_surface_baseline;
    private Chronometer voicecall_chronometer;

    private LinearLayout voice_incalling_container;
    private ImageView btn_incalling_mute;
    private ImageView btn_incalling_drop;
    private ImageView btn_incalling_handsfree;

    private LinearLayout voice_outgoing_container;
    private ImageView btn_outgoing_mute;
    private ImageView btn_outgoing_cancel;
    private ImageView btn_outgoing_handsfree;

    private LinearLayout voice_incoming_container;
    private ImageView btn_incoming_refuse;
    private ImageView btn_incoming_answer;

    //窗口create依赖入参(主叫UserID)-fromUserID
    private String fromUserID;
    //窗口create依赖入参(被叫UserID)-toUserID
    private String toUserID;
    //窗口create依赖入参(Single/TalkGroup)-toUserID
    private String groupType;
    //通话对方UserID
    private String remoteUserID;
    //本机用户ID
    private String localUserID;
    //会话session
    private String sessionID;
    //呼出\呼入响铃标识
    private int streamID;
    //是否静音
    private boolean isMuteState;
    //是否免提
    private boolean isHandsfreeState;

    private Handler userInfoHandler = null;
    private Handler audioCallHandler = null;
    private MessageProvider msgProvider = new MessageProvider();
    private Interfaces.IUserRequest userRequest = null;
    private Interfaces.IAudioRequest audioRequest = null;

    private Handler outgoingTimeoutHandler = null;
    private Runnable timeoutRunnableRef = null;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);

        ((ApplicationSession)getApplication()).addActivity(this);
        getSupportActionBar().hide();
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        Log.d("mysurface","VoiceCallActivity onCreate");

        //活动显示主布局
        voicecall_root_layout = (LinearLayout)findViewById(R.id.voicecall_root_layout);
        //头像
        voicecall_user_avartar = (ImageView)findViewById(R.id.voicecall_user_avartar);
        //通话人昵称
        voicecall_nickname = (TextView)findViewById(R.id.voicecall_nickname);
        //呼叫状态
        voicecall_state = (TextView)findViewById(R.id.voicecall_state);
        //VoiceCallActivity所有控制按键的container
        voice_surface_baseline = (LinearLayout)findViewById(R.id.voice_surface_baseline);
        //呼叫时长
        voicecall_chronometer = (Chronometer)findViewById(R.id.voicecall_chronometer);

        //通话时的控制按键container
        voice_incalling_container = (LinearLayout)findViewById(R.id.voice_incalling_container);
        //通话中的静音
        btn_incalling_mute = (ImageView)findViewById(R.id.btn_incalling_mute);
        //通话中的挂断
        btn_incalling_drop = (ImageView)findViewById(R.id.btn_incalling_drop);
        //通话中的免提
        btn_incalling_handsfree = (ImageView)findViewById(R.id.btn_incalling_handsfree);

        //呼出时的控制按键container
        voice_outgoing_container = (LinearLayout)findViewById(R.id.voice_outgoing_container);
        //呼出时的静音
        btn_outgoing_mute = (ImageView)findViewById(R.id.btn_outgoing_mute);
        //呼出时的取消
        btn_outgoing_cancel = (ImageView)findViewById(R.id.btn_outgoing_cancel);
        //呼出时的免提
        btn_outgoing_handsfree = (ImageView)findViewById(R.id.btn_outgoing_handsfree);

        //呼入时的控制按键container
        voice_incoming_container = (LinearLayout)findViewById(R.id.voice_incoming_container);
        //呼入时的拒绝接听
        btn_incoming_refuse = (ImageView)findViewById(R.id.btn_incoming_refuse);
        //呼入时的接听
        btn_incoming_answer = (ImageView)findViewById(R.id.btn_incoming_answer);
        //是否静音
        isMuteState = false;
        //是否免提
        isHandsfreeState = false;

        //增加单击事件监听
        addClickListener();

        //设置通话监听
        addCallStateListener();

        //设置客户端转发组件参数
        setClientMediaParamsRequest();

        //WebRTC资源初始化
        synchronized(WebRTCProxy.getInstance()) {
            WebRTCProxy.getInstance().onWebRTCInit(this);
        }

        //窗口create依赖入参b---------
        fromUserID = this.getIntent().getStringExtra("FromUserID");
        toUserID = this.getIntent().getStringExtra("ToUserID");
        groupType = this.getIntent().getStringExtra("GroupType");
        sessionID = this.getIntent().getStringExtra("SessionID");
        //窗口create依赖入参e---------

        //判断呼叫方向
        ApplicationSession session = (ApplicationSession) getApplication();
        if(session.getUser().getUserID().equals(fromUserID)){
            isIncomingCall = false;
            remoteUserID = toUserID;
            localUserID = fromUserID;
        }
        else{
            isIncomingCall = true;
            remoteUserID = fromUserID;
            localUserID = toUserID;
        }

        if(isIncomingCall==true){
            callingDuration = CallingDuration.INCOMING;
        }
        else{
            callingDuration = CallingDuration.OUTGOING;
        }

        if(callingDuration==CallingDuration.INCOMING){
            incomingProcess(fromUserID);
        }
        else{
            outgoingProcess(toUserID);
        }
    }

    //私有方法b--------
    //OUTGOING方向处理过程
    private void outgoingProcess(String toUserID){
        if((ShareData.getInstance().getPublicIP()==null)||(ShareData.getInstance().getPublicIP().length()==0)
                ||(ShareData.getInstance().getPublicPort()==null)||(ShareData.getInstance().getPublicPort().length()==0)){
            Toast.makeText(VoiceCallActivity.this,getResources().getString(R.string.trunkServer_connection_fail_toast)
                    ,Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //标识呼叫状态
        callingDuration = CallingDuration.OUTGOING;
        //设置通话状态
        voicecall_state.setText(getResources().getString(R.string.audioCallOutgoing));
        isHandsfreeState = true;
        btn_outgoing_handsfree.setImageResource(R.drawable.icon_speaker_on);
        //切换呼叫视图场景
        changeContainerState(callingDuration);
        //开启回铃音效
        soundPool = new SoundPool(1, AudioManager.STREAM_RING, 0);
        outgoing = soundPool.load(this, R.raw.outgoing, 1);
        handler.postDelayed(new Runnable() {
            public void run() {
                streamID = playMakeCallSounds();
            }
        }, 300);
        //根据UserID查询通话人
        getUserInfoRequest(toUserID);
        //产生会话ID
        sessionID = UUID.randomUUID().toString();
        //发起invite
        inviteVoiceCallRequest(toUserID, groupType, sessionID);
        Toast.makeText(VoiceCallActivity.this,getResources().getString(R.string.headsetPluggedInToast)
                ,Toast.LENGTH_SHORT).show();

        //呼叫超时提醒
        outgoingTimeoutHandler = new Handler();
        timeoutRunnableRef = new Runnable() {
            @Override
            public void run() {
                timeoutToast();
            }
        };

        outgoingTimeoutHandler.postDelayed(timeoutRunnableRef
                ,getResources().getInteger(R.integer.outgoing_call_timeout));
    }

    private void timeoutToast(){
        if(callingDuration==CallingDuration.OUTGOING){
            Toast.makeText(VoiceCallActivity.this,getResources().getString(R.string.outgoing_call_timeout_toast)
                    ,Toast.LENGTH_SHORT).show();
        }
    }

    //INCOMING方向处理过程
    private void incomingProcess(String fromUserID){
        if((ShareData.getInstance().getPublicIP()==null)||(ShareData.getInstance().getPublicIP().length()==0)
                ||(ShareData.getInstance().getPublicPort()==null)||(ShareData.getInstance().getPublicPort().length()==0)){
            Toast.makeText(VoiceCallActivity.this,getResources().getString(R.string.trunkServer_connection_fail_toast2)
                    ,Toast.LENGTH_SHORT).show();
            refuseVoiceRequest(remoteUserID,groupType,sessionID);
            finish();
            return;
        }

        //标识呼叫状态
        callingDuration = CallingDuration.INCOMING;
        //设置通话状态
        voicecall_state.setText(getResources().getString(R.string.audioCallIncoming));
        //切换呼叫视图场景
        changeContainerState(callingDuration);
        //开启振铃音效
        Uri ringUri = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        audioManager.setMode(AudioManager.MODE_RINGTONE);
        audioManager.setSpeakerphoneOn(true);
        ringtone = RingtoneManager.getRingtone(this, ringUri);
        ringtone.play();
        //根据UserID查询通话人
        getUserInfoRequest(toUserID);

        Toast.makeText(VoiceCallActivity.this,getResources().getString(R.string.videoSwitchAudioToast)
                ,Toast.LENGTH_SHORT).show();
    }

    //根据呼叫状态切换视图场景
    private void changeContainerState(CallingDuration duration){
        if(duration==CallingDuration.OUTGOING){
            //通话时长隐藏
            voicecall_chronometer.setVisibility(View.GONE);
            //呼出状态container显示
            voice_outgoing_container.setVisibility(View.VISIBLE);
            //呼入状态container隐藏
            voice_incoming_container.setVisibility(View.GONE);
            //通话状态container隐藏
            voice_incalling_container.setVisibility(View.GONE);
            //静音键无效
            btn_outgoing_mute.setEnabled(false);
        }
        else if(duration==CallingDuration.INCOMING){
            //通话时长隐藏
            voicecall_chronometer.setVisibility(View.GONE);
            //呼出状态container隐藏
            voice_outgoing_container.setVisibility(View.GONE);
            //呼入状态container显示
            voice_incoming_container.setVisibility(View.VISIBLE);
            //通话状态container隐藏
            voice_incalling_container.setVisibility(View.GONE);
        }
        else if(duration==CallingDuration.INCALLING){
            //通话时长显示
            voicecall_chronometer.setVisibility(View.VISIBLE);
            //呼出状态container隐藏
            voice_outgoing_container.setVisibility(View.GONE);
            //呼入状态container隐藏
            voice_incoming_container.setVisibility(View.GONE);
            //通话状态container显示
            voice_incalling_container.setVisibility(View.VISIBLE);
        }
        else if(duration==CallingDuration.ESTABLISHING){
            //通话时长隐藏
            voicecall_chronometer.setVisibility(View.GONE);
            //呼出状态container隐藏
            voice_outgoing_container.setVisibility(View.GONE);
            //呼入状态container隐藏
            voice_incoming_container.setVisibility(View.GONE);
            //通话状态container隐藏
            voice_incalling_container.setVisibility(View.GONE);
        }
    }

    //开启Webrtc音频通道
    private synchronized int toggleStartAudioCall(Bundle bundle){
        Log.d("webrtc","toggleStartAudioCall");
        if(WebRTCProxy.getInstance().startAudioCallAction(bundle).equals("StateOK")){
            Log.d("webrtc","toggleStartAudioCall StateOK");
            return 0;
        }
        else{
            Log.d("webrtc","toggleStartAudioCall StateFail");
            return -1;
        }
    }

    //关闭Webrtc音视频通道
    public synchronized void toggleStopAudioCall() {
        WebRTCProxy.getInstance().stopAudioCallAction();
    }

    //WebRTC资源析构
    public synchronized void onWebRTCDepose() {
        WebRTCProxy.getInstance().onWebRTCDepose();
    }

    //呼叫状态监听
    private void addCallStateListener(){
        userInfoHandler = new Handler(){
            public void handleMessage(final Message msg){
                MessageType type = MessageType.values()[msg.what];
                switch (type){
                    case GETUSERINFOBYUSERID_MSG_OK:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onGetUserInfoByID(msg);
                            }
                        });
                        break;
                    case GETUSERINFOBYUSERID_MSG_FAILED:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onGetUserInfoByID(msg);
                            }
                        });
                        break;
                    case AVATARDOWNLOAD_MSG_OK:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onDownloadImage(msg);
                            }
                        });
                        break;
                    case AVATARDOWNLOAD_MSG_FAILED:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onDownloadImage(msg);
                            }
                        });
                        break;
                    default:break;
                }
            }
        };
        PubHolder.getInstance().addUserHandler(HandlerType.VOICECALLACTIVITY_USERHANDLER.ordinal()
                ,userInfoHandler);
        userRequest = PubHolder.getInstance().getUserRequest();

        audioCallHandler = new Handler(){
            public void handleMessage(final Message msg){
                MessageType type = MessageType.values()[msg.what];
                switch(type){
                    case RECVACCEPTVOICE_MSG:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onRecvAcceptVoice(msg);
                            }
                        });
                        break;
                    case RECVREFUSEVOICE_MSG:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onRecvRefuseVoice(msg);
                            }
                        });
                        break;
                    case RECVCANCELVOICE_MSG:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onRecvCancelVoice(msg);
                            }
                        });
                        break;
                    case RECVCLOSEVOICE_MSG:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onRecvCloseVoice(msg);
                            }
                        });
                        break;
                    case OPENAUDIODEVICE_MSG:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onOpenAudioDevice(msg);
                            }
                        });
                        break;
                    default:break;
                }
            }
        };
        PubHolder.getInstance().addAudioHandler(HandlerType.VOICECALLACTIVITY_AUDIOHANDLER.ordinal()
                ,audioCallHandler);
        audioRequest = PubHolder.getInstance().getAudioreq();
    }

    //单击事件监听
    private void addClickListener(){
        btn_incalling_mute.setOnClickListener(this);
        btn_incalling_drop.setOnClickListener(this);
        btn_incalling_handsfree.setOnClickListener(this);
        btn_outgoing_mute.setOnClickListener(this);
        btn_outgoing_cancel.setOnClickListener(this);
        btn_outgoing_handsfree.setOnClickListener(this);
        btn_incoming_refuse.setOnClickListener(this);
        btn_incoming_answer.setOnClickListener(this);
    }

    //异常退出处理
    private void illegalQuitProcess(){
        if(callingDuration!=CallingDuration.IDLE){
            if(callingDuration==CallingDuration.INCOMING){
                //拒绝接听
                refuseVoiceRequest(remoteUserID, groupType, sessionID);
                //停止振铃音效
                if (ringtone != null)
                    ringtone.stop();
                //关闭扩音器
                closeSpeakerOn();
                //切换呼叫状态
                callingDuration = CallingDuration.IDLE;
            }
            else if(callingDuration==CallingDuration.OUTGOING){
                //取消邀请
                cancelVoiceRequest(remoteUserID, groupType, sessionID);
                //停止拨打电话音效
                try {
                    if (soundPool != null)
                        soundPool.stop(streamID);
                } catch (Exception e) {
                }
            }
            else if(callingDuration==CallingDuration.INCALLING){
                //挂断
                //发送结束视频聊天请求
                closeVoiceRequest(remoteUserID, groupType, sessionID);
                //切换呼叫状态
                callingDuration = CallingDuration.IDLE;
                //关闭音视频通道
                toggleStopAudioCall();
                //停止计时
                voicecall_chronometer.stop();
            }
        }
    }
    //私有方法e--------

    //点击事件响应b-------
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_incalling_mute:
                voiceMuteClickAction();
                break;
            case R.id.btn_incalling_drop:
                dropCallClickAction();
                break;
            case R.id.btn_incalling_handsfree:
                handsFreeClickAction();
                break;
            case R.id.btn_outgoing_mute:
                voiceMuteClickAction();
                break;
            case R.id.btn_outgoing_cancel:
                cancelCallClickAction();
                break;
            case R.id.btn_outgoing_handsfree:
                handsFreeClickAction();
                break;
            case R.id.btn_incoming_refuse:
                refuseCallClickAction();
                break;
            case R.id.btn_incoming_answer:
                answerCallClickAction();
                break;
        }
    }
    //点击事件响应e-------

    //点击事件实现b-------
    private void voiceMuteClickAction(){
        if(callingDuration==CallingDuration.INCALLING){
            if (isMuteState) {
                // 关闭静音
                btn_incalling_mute.setImageResource(R.drawable.icon_mute_normal);
                audioManager.setMicrophoneMute(false);
                isMuteState = false;
            } else {
                // 打开静音
                btn_incalling_mute.setImageResource(R.drawable.icon_mute_on);
                audioManager.setMicrophoneMute(true);
                isMuteState = true;
            }
        }
    }

    private void dropCallClickAction(){
        if(callingDuration==CallingDuration.INCALLING){
            //发送结束音频聊天请求
            closeVoiceRequest(remoteUserID, groupType, sessionID);
            //切换呼叫状态
            callingDuration = CallingDuration.IDLE;
            //关闭音频通道
            toggleStopAudioCall();
            //停止计时
            voicecall_chronometer.stop();
            callDruationText = voicecall_chronometer.getText()
                    .toString();
            callingState = CallingState.NORMAL;
            saveCallRecord(CallingMode.VIDEO.ordinal());
            Toast.makeText(VoiceCallActivity.this,getResources().getString(R.string.drop_voicecall_toast),Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void handsFreeClickAction(){
        if((callingDuration==CallingDuration.INCALLING)||(callingDuration==CallingDuration.OUTGOING)){
            if (isHandsfreeState) {
                // 关闭免提
                if(callingDuration==CallingDuration.INCALLING)
                    btn_incalling_handsfree.setImageResource(R.drawable.icon_speaker_normal);
                else if(callingDuration==CallingDuration.OUTGOING)
                    btn_outgoing_handsfree.setImageResource(R.drawable.icon_speaker_normal);
                closeSpeakerOn();
                isHandsfreeState = false;
            } else {
                //打开免提
                if(callingDuration==CallingDuration.INCALLING)
                    btn_incalling_handsfree.setImageResource(R.drawable.icon_speaker_on);
                else if(callingDuration==CallingDuration.OUTGOING)
                    btn_outgoing_handsfree.setImageResource(R.drawable.icon_speaker_on);
                openSpeakerOn();
                isHandsfreeState = true;
            }
        }
    }

    private void cancelCallClickAction(){
        if(callingDuration==CallingDuration.OUTGOING){

            if((outgoingTimeoutHandler!=null)&&(timeoutRunnableRef!=null)) {
                outgoingTimeoutHandler.removeCallbacks(timeoutRunnableRef);
            }

            cancelVoiceRequest(remoteUserID, groupType, sessionID);

            //停止拨打电话音效
            try {
                if (soundPool != null)
                    soundPool.stop(streamID);
            } catch (Exception e) {
            }

            Toast.makeText(VoiceCallActivity.this,getResources().getString(R.string.cancel_invitevoicecall_toast),
                    Toast.LENGTH_SHORT).show();

            //保存通话记录
            callingState = CallingState.CANCED;
            saveCallRecord(CallingMode.AUDIO.ordinal());
            //关闭活动
            finish();
        }
    }

    private void refuseCallClickAction(){
        if(callingDuration==CallingDuration.INCOMING){
            //发送拒绝邀请
            refuseVoiceRequest(remoteUserID, groupType, sessionID);
            //停止振铃音效
            if (ringtone != null)
                ringtone.stop();
            //关闭扩音器
            closeSpeakerOn();
            //切换呼叫状态
            callingDuration = CallingDuration.IDLE;
            //保存呼叫记录
            callingState = CallingState.REFUESD;
            saveCallRecord(CallingMode.VIDEO.ordinal());
            Toast.makeText(VoiceCallActivity.this,getResources().getString(R.string.Refused),Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void answerCallClickAction(){
        if(callingDuration==CallingDuration.INCOMING){
            //发送接受视频聊天请求
            acceptVoiceRequest(remoteUserID, groupType, sessionID);
            //停止振铃音效
            if (ringtone != null)
                ringtone.stop();
            //关闭扩音器
            closeSpeakerOn();
            //显示正在连接
            callingDuration = CallingDuration.ESTABLISHING;
            voicecall_state.setText(getResources().getString(R.string.CallEstablishing));
            changeContainerState(callingDuration);
        }
    }
    //点击事件实现e-------

    //服务器请求b-------
    private void setClientMediaParamsRequest(){
        IMessage msg = msgProvider.createMessage();
        msg.setString("WebRTCAudioIP", getResources().getString(R.string.webrtc_transIP_default));
        msg.setString("WebRTCAudioPort", String.valueOf(getResources().getInteger(R.integer.webrtc_audio_recv_port_default)));
        msg.setString("WebRTCVideoIP", getResources().getString(R.string.webrtc_transIP_default));
        msg.setString("WebRTCVideoPort", String.valueOf(getResources().getInteger(R.integer.webrtc_video_recv_port_default)));
        msg.setString("ListenAPort", String.valueOf(getResources().getInteger(R.integer.client_audio_exchange_port_default)));
        msg.setString("ListenVPort", String.valueOf(getResources().getInteger(R.integer.client_video_exchange_port_default)));
        userRequest.SetMediaParamsRequest(msg);
    }

    private void getUserInfoRequest(String userID){
        IMessage msg = msgProvider.createMessage();
        msg.setString("UserID", userID);
        userRequest.GetUserInfoByUserID(msg);
    }

    private void downloadImageRequest(String userID, String avatarName){
        IMessage msg = msgProvider.createMessage();
        msg.setString("UserID",userID);
        msg.setString("Avatar",avatarName);
        userRequest.DownLoadAvatarRequest(msg);
    }

    private void inviteVoiceCallRequest(String ToUser, String GroupType, String SessionID){
        IMessage msg = msgProvider.createMessage();
        msg.setString("ToUser",ToUser);
        msg.setString("GroupType",GroupType);
        msg.setString("SessionID",SessionID);
        audioRequest.InviteAudioRequest(msg);
    }

    private void closeVoiceRequest(String ToUser, String GroupType, String SessionID){
        IMessage msg = msgProvider.createMessage();
        msg.setString("ToUser",ToUser);
        msg.setString("GroupType",GroupType);
        msg.setString("SessionID",SessionID);
        audioRequest.CloseAudioRequest(msg);
    }

    private void acceptVoiceRequest(String ToUser, String GroupType, String SessionID){
        IMessage msg = msgProvider.createMessage();
        msg.setString("ToUser",ToUser);
        msg.setString("GroupType",GroupType);
        msg.setString("SessionID",SessionID);
        audioRequest.AcceptAudioRequest(msg);
    }

    private void refuseVoiceRequest(String ToUser, String GroupType, String SessionID){
        IMessage msg = msgProvider.createMessage();
        msg.setString("ToUser",ToUser);
        msg.setString("GroupType",GroupType);
        msg.setString("SessionID",SessionID);
        audioRequest.RefuseAudioRequest(msg);
    }

    private void cancelVoiceRequest(String ToUser, String GroupType, String SessionID){
        IMessage msg = msgProvider.createMessage();
        msg.setString("ToUser",remoteUserID);
        msg.setString("GroupType","Single");
        msg.setString("SessionID",sessionID);
        audioRequest.CancelInviteAudioRequest(msg);
    }
    //服务器请求e-------

    //底层消息响应回调b------
    private void onGetUserInfoByID(Message msg){
        MessageType type = MessageType.values()[msg.what];
        if(type==MessageType.GETUSERINFOBYUSERID_MSG_OK){
            PubStruct.UserInfo myMsg = (PubStruct.UserInfo) msg.obj;
            //判断呼叫状态是否在OUTGOING或INCOMING
            if((callingDuration==CallingDuration.INCOMING)||(callingDuration==CallingDuration.OUTGOING)){

                //设置通话人text
                if((myMsg.checkKey(PubStruct.UserInfo.Name)==true)&&
                        (myMsg.get(PubStruct.UserInfo.Name).length()>0)){
                    voicecall_nickname.setText(myMsg.get(PubStruct.UserInfo.Name));
                    remoteName = myMsg.get(PubStruct.UserInfo.Name);
                }
                else{
                    voicecall_nickname.setText(getResources().getString(R.string.remoteUserNickName_default));
                    remoteName = getResources().getString(R.string.remoteUserNickName_default);
                }

                //设置通话人头像
                if((myMsg.checkKey(PubStruct.UserInfo.Avatar)==true)&&
                        (myMsg.get(PubStruct.UserInfo.Avatar).length()>0)){
                    String avatarFullPath = userRequest.GetLocalFile(myMsg.get(PubStruct.UserInfo.UserID),
                            myMsg.get(PubStruct.UserInfo.Avatar));
                    if(avatarFullPath!=null){
                        Bitmap bitmap = ImageUtils.getPhotoByFullPath(avatarFullPath);
                        if(bitmap!=null){
                            voicecall_user_avartar.setImageBitmap(bitmap);
                        }
                        else{
                            voicecall_user_avartar.setImageResource(R.drawable.head);
                        }
                    }
                    else{
                        //需要向服务器下载头像
                        downloadImageRequest(myMsg.get(PubStruct.UserInfo.UserID),
                                myMsg.get(PubStruct.UserInfo.Avatar));
                    }
                }
                else{
                    voicecall_user_avartar.setImageResource(R.drawable.head);
                }
            }
        }
        else{
            voicecall_nickname.setText(getResources().getString(R.string.remoteUserNickName_default));
            remoteName = getResources().getString(R.string.remoteUserNickName_default);
            voicecall_user_avartar.setImageResource(R.drawable.head);
            Toast.makeText(VoiceCallActivity.this,getResources().getString(R.string.videocall_getuserinfo_fail),Toast.LENGTH_SHORT).show();
        }
    }

    private void onDownloadImage(Message msg){
        if((callingDuration==CallingDuration.INCOMING)||(callingDuration==CallingDuration.OUTGOING)) {
            MessageType type = MessageType.values()[msg.what];
            IMessage myMsg = (IMessage)msg.obj;
            if (type == MessageType.AVATARDOWNLOAD_MSG_OK) {
                Log.d("data","download image ok");
                String userid = myMsg.getString("UserID");
                if(userid.equals(remoteUserID)){
                    Bitmap bitmap = ImageUtils.getPhotoByFullPath(myMsg.getString("FileFullName"));
                    if(bitmap!=null){
                        voicecall_user_avartar.setImageBitmap(bitmap);
                    }
                    else{
                        voicecall_user_avartar.setImageResource(R.drawable.head);
                    }
                }
            } else {
                Log.d("data","download image error");
                voicecall_user_avartar.setImageResource(R.drawable.head);
                Toast.makeText(VoiceCallActivity.this, getResources().getString(R.string.videocall_downloadavatar_fail)
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onRecvAcceptVoice(Message msg){
        if(callingDuration==CallingDuration.OUTGOING){
            IMessage myMsg = (IMessage)msg.obj;
            if((myMsg.checkKey("ToUser")==true)&&(myMsg.getString("ToUser").equals(localUserID))) {
                if((outgoingTimeoutHandler!=null)&&(timeoutRunnableRef!=null)) {
                    outgoingTimeoutHandler.removeCallbacks(timeoutRunnableRef);
                }

                //停止振铃/回铃音效
                try {
                    if (soundPool != null)
                        soundPool.stop(streamID);
                } catch (Exception e) {
                }

                //关闭扩音器
                closeSpeakerOn();

                //显示正在连接
                callingDuration = CallingDuration.ESTABLISHING;
                voicecall_state.setText(getResources().getString(R.string.CallEstablishing));
                changeContainerState(callingDuration);
            }
        }
    }

    private void onRecvRefuseVoice(Message msg){
        if(callingDuration==CallingDuration.OUTGOING) {

            if((outgoingTimeoutHandler!=null)&&(timeoutRunnableRef!=null)) {
                outgoingTimeoutHandler.removeCallbacks(timeoutRunnableRef);
            }

            //停止回铃音效
            try {
                if (soundPool != null)
                    soundPool.stop(streamID);
            } catch (Exception e) {
            }

            //关闭扩音器
            closeSpeakerOn();
            //切换呼叫状态
            callingDuration = CallingDuration.IDLE;
            //保存呼叫记录
            callingState = CallingState.BEREFUESD;
            saveCallRecord(CallingMode.AUDIO.ordinal());
            Toast.makeText(VoiceCallActivity.this,getResources().getString(R.string.BeRefused),Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void onRecvCancelVoice(Message msg){
        if(callingDuration==CallingDuration.INCOMING) {
            //停止振铃音效
            if (ringtone != null)
                ringtone.stop();
            //关闭扩音器
            closeSpeakerOn();
            //切换呼叫状态
            callingDuration = CallingDuration.IDLE;
            //保存呼叫记录
            callingState = CallingState.CANCED;
            saveCallRecord(CallingMode.AUDIO.ordinal());
            Toast.makeText(VoiceCallActivity.this,getResources().getString(R.string.Canceled),Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void onRecvCloseVoice(Message msg){
        //仅在通话过程中响应
        if(callingDuration==CallingDuration.INCALLING){
            //切换呼叫状态
            callingDuration = CallingDuration.IDLE;
            //关闭音视频通道
            toggleStopAudioCall();
            //停止计时
            voicecall_chronometer.stop();
            callDruationText = voicecall_chronometer.getText()
                    .toString();
            callingState = CallingState.NORMAL;
            saveCallRecord(CallingMode.AUDIO.ordinal());
            Toast.makeText(VoiceCallActivity.this,getResources().getString(R.string.drop_videocall_toast),Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void onOpenAudioDevice(Message msg){
        Log.d("data","onOpenAudioDevice:"+callingDuration);
        if(callingDuration!=CallingDuration.IDLE) {
            //读取呼叫参数
            MessageType type = MessageType.values()[msg.what];
            IMessage myMsg = (IMessage) msg.obj;
            if (myMsg.checkKey("SSRC") == true) {
                String tmpSSRC_A = myMsg.getString("SSRC");

                Bundle bundle = new Bundle();
                bundle.putString("remoteIP", getResources().getString(R.string.mpclient_transIP_default));
                bundle.putInt("localRecvPort_A", getResources().getInteger(R.integer.webrtc_audio_recv_port_default));
                bundle.putInt("remoteSendPort_A", getResources().getInteger(R.integer.client_audio_exchange_port_default));
                bundle.putInt("ssrc_A", ssrc2uint(tmpSSRC_A));

                Log.d("data","ssrc_A:"+ssrc2uint(tmpSSRC_A));

                closeSpeakerOn();
                isHandsfreeState = false;
                //发起音频呼叫
                toggleStartAudioCall(bundle);

                voicecall_state.setText(" ");
                //转移呼叫状态
                callingDuration = CallingDuration.INCALLING;
                changeContainerState(callingDuration);

                //开始计时
                voicecall_chronometer.setBase(SystemClock
                        .elapsedRealtime());
                voicecall_chronometer.start();

                Toast.makeText(VoiceCallActivity.this,getResources().getString(R.string.audioCallToast),Toast.LENGTH_SHORT).show();
            } else {
                Log.d("data", "onOpenAudioDevice lack of SSRC param");
                toggleStopAudioCall();
                finish();
            }
        }
    }
    //底层消息响应回调e------

    //活动的生命周期b-----------
    @Override
    protected void onStop() {
        super.onStop();
        Log.d("mysurface","VoiceCallActivity onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("mysurface","VoiceCallActivity onDestroy");

        //处理异常退出
        illegalQuitProcess();
        //析构webrtc资源
        onWebRTCDepose();
        //移出Handler
        PubHolder.getInstance().removeUserHandler(HandlerType.VOICECALLACTIVITY_USERHANDLER.ordinal());
        PubHolder.getInstance().removeAudioHandler(HandlerType.VOICECALLACTIVITY_AUDIOHANDLER.ordinal());
        //移出活动
        ((ApplicationSession)getApplication()).removeActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("mysurface","VoiceCallActivity onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("mysurface","VoiceCallActivity onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("mysurface","VoiceCallActivity onStart");
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        //屏蔽BACK按键消息
        if(KeyEvent.KEYCODE_BACK==keyCode&&event.getAction()==KeyEvent.ACTION_DOWN){
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
    //活动的生命周期e-----------
}
