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

/**
 * Created by kai on 2016/8/8.
 */
public class VideoCallActivity extends CallActivity implements View.OnClickListener{

    private static final String TAG = "VideoCallActivity";

    private LinearLayout primarySurace;
    private LinearLayout secondarySurface;
    private LinearLayout ll_top_container;
    private LinearLayout ll_surface_baseline;
    private LinearLayout video_outgoing_container;
    private LinearLayout video_incoming_container;
    private LinearLayout video_incalling_container;

    private ImageView user_avartar;
    private TextView user_nickname;
    private TextView videocall_state;

    private ImageView cancelVideoCall_btn;
    private ImageView refuseVideoCall_btn;
    private ImageView answerVideoCall_btn;
    private ImageView dropVideoCall_btn;
    private ImageView switchCamera_btn;
    private ImageView switchToAudio_btn;

    //通话时长
    private Chronometer chronometer;
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
    //视频会话session
    private String sessionID;
    //音频会话ID
    private String audioID;
    //呼出\呼入响铃标识
    private int streamID;
    //通话中控制按键的显示标识
    private boolean isCallingContainerShow = false;
    private boolean bSwitchView = false;

    private Handler videoCallHandler = null;
    private Handler userInfoHandler = null;
    private Handler audioCallHandler = null;
    private Handler outgoingTimeoutHandler = null;
    private Runnable timeoutRunnableRef = null;
    private MessageProvider msgProvider = new MessageProvider();
    private Interfaces.IVideoRequest videoRequest = null;
    private Interfaces.IUserRequest userRequest = null;
    private Interfaces.IAudioRequest audioRequest = null;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        ((ApplicationSession)getApplication()).addActivity(this);
        getSupportActionBar().hide();
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        Log.d("mysurface","VideoCallActivity onCreate");
        bSwitchView = false;
        //图像显示主布局
        primarySurace = (LinearLayout) findViewById(R.id.videocall_primary_surface);
        //图像显示次布局
        secondarySurface = (LinearLayout)findViewById(R.id.videocall_secondray_surface);

        //用户信息显示container（包括头像、用户名、呼叫状态）
        ll_top_container = (LinearLayout)findViewById(R.id.ll_top_container);
        //头像
        user_avartar = (ImageView)findViewById(R.id.user_avartar);
        //用户名
        user_nickname = (TextView)findViewById(R.id.user_nickname);
        //呼叫状态
        videocall_state = (TextView)findViewById(R.id.videocall_state);

        //VideoCallActivity所有控制按键的container
        ll_surface_baseline = (LinearLayout)findViewById(R.id.ll_surface_baseline);
        //通话时长
        chronometer = (Chronometer)findViewById(R.id.chronometer);

        //呼出状态的container
        video_outgoing_container = (LinearLayout)findViewById(R.id.video_outgoing_container);
        //取消呼叫
        cancelVideoCall_btn = (ImageView)findViewById(R.id.btn_cancel_call);

        //呼入状态的container
        video_incoming_container = (LinearLayout)findViewById(R.id.video_incoming_container);
        //拒绝接听
        refuseVideoCall_btn = (ImageView)findViewById(R.id.btn_refuse_call);
        //接听
        answerVideoCall_btn = (ImageView)findViewById(R.id.btn_answer_call);

        //通话状态的container
        video_incalling_container = (LinearLayout)findViewById(R.id.video_calling_container);
        //挂断
        dropVideoCall_btn = (ImageView)findViewById(R.id.btn_drop_call);
        //转换摄像头
        switchCamera_btn = (ImageView)findViewById(R.id.btn_switch_camera);
        //切到语音聊天
        switchToAudio_btn = (ImageView)findViewById(R.id.btn_switchto_audio);

        //增加单击事件监听
        addClickListener();

        //设置通话监听
        addCallStateListener();

        //设置客户端转发组件参数
        setClientMediaParamsRequest();

        //WebRTC资源初始化
        synchronized(WebRTCProxy.getInstance()) {
            WebRTCProxy.getInstance().onWebRTCInit(this, primarySurace, secondarySurface);
        }

        //通话过程中呼叫控制按键是否可见
        isCallingContainerShow = true;

        //窗口create依赖入参b---------
        fromUserID = this.getIntent().getStringExtra("FromUserID");
        toUserID = this.getIntent().getStringExtra("ToUserID");
        groupType = this.getIntent().getStringExtra("GroupType");
        sessionID = this.getIntent().getStringExtra("SessionID");
        audioID = this.getIntent().getStringExtra("AudioID");
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

    //私有方法b----------
    //OUTGOING方向处理过程
    private void outgoingProcess(String toUserID){
        if((ShareData.getInstance().getPublicIP()==null)||(ShareData.getInstance().getPublicIP().length()==0)
                ||(ShareData.getInstance().getPublicPort()==null)||(ShareData.getInstance().getPublicPort().length()==0)){
            Toast.makeText(VideoCallActivity.this,getResources().getString(R.string.trunkServer_connection_fail_toast)
                    ,Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //标识呼叫状态
        callingDuration = CallingDuration.OUTGOING;
        //设置通话状态
        videocall_state.setText(getResources().getString(R.string.videoCallOutgoing));
        //切换呼叫视图场景
        changeContainerState(callingDuration);
        //采集本地画面
        synchronized(WebRTCProxy.getInstance()) {
            WebRTCProxy.getInstance().startLocalCapture();
        }
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
        //产生音频ID
        audioID = UUID.randomUUID().toString();
        //发起invite
        inviteVideoCallRequest(toUserID, groupType, sessionID, audioID);

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
            Toast.makeText(VideoCallActivity.this,getResources().getString(R.string.outgoing_call_timeout_toast)
                    ,Toast.LENGTH_SHORT).show();
        }
    }

    //INCOMING方向处理过程
    private void incomingProcess(String fromUserID){
        if((ShareData.getInstance().getPublicIP()==null)||(ShareData.getInstance().getPublicIP().length()==0)
                ||(ShareData.getInstance().getPublicPort()==null)||(ShareData.getInstance().getPublicPort().length()==0)){
            Toast.makeText(VideoCallActivity.this,getResources().getString(R.string.trunkServer_connection_fail_toast2)
                    ,Toast.LENGTH_SHORT).show();
            refuseVideoRequest(remoteUserID,groupType,sessionID);
            finish();
            return;
        }

        //标识呼叫状态
        callingDuration = CallingDuration.INCOMING;
        //设置通话状态
        videocall_state.setText(getResources().getString(R.string.videoCallIncoming));
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
        getUserInfoRequest(fromUserID);
    }

    //根据呼叫状态切换视图场景
    private void changeContainerState(CallingDuration duration){
        if(duration==CallingDuration.OUTGOING){
            //通话时长隐藏
            chronometer.setVisibility(View.GONE);
            //呼出状态container显示
            video_outgoing_container.setVisibility(View.VISIBLE);
            //呼入状态container隐藏
            video_incoming_container.setVisibility(View.GONE);
            //通话状态container隐藏
            video_incalling_container.setVisibility(View.GONE);
            //通话人信息显示
            ll_top_container.setVisibility(View.VISIBLE);
        }
        else if(duration==CallingDuration.INCOMING){
            //通话时长隐藏
            chronometer.setVisibility(View.GONE);
            //呼出状态container隐藏
            video_outgoing_container.setVisibility(View.GONE);
            //呼入状态container显示
            video_incoming_container.setVisibility(View.VISIBLE);
            //通话状态container隐藏
            video_incalling_container.setVisibility(View.GONE);
            //通话人信息显示
            ll_top_container.setVisibility(View.VISIBLE);
        }
        else if(duration==CallingDuration.INCALLING){
            //通话时长显示
            chronometer.setVisibility(View.VISIBLE);
            //呼出状态container隐藏
            video_outgoing_container.setVisibility(View.GONE);
            //呼入状态container隐藏
            video_incoming_container.setVisibility(View.GONE);
            //通话状态container显示
            video_incalling_container.setVisibility(View.VISIBLE);
            //通话人信息隐藏
            ll_top_container.setVisibility(View.GONE);
        }
        else if(duration==CallingDuration.ESTABLISHING){
            //通话时长隐藏
            chronometer.setVisibility(View.GONE);
            //呼出状态container隐藏
            video_outgoing_container.setVisibility(View.GONE);
            //呼入状态container隐藏
            video_incoming_container.setVisibility(View.GONE);
            //通话状态container隐藏
            video_incalling_container.setVisibility(View.GONE);
            //通话人信息显示
            ll_top_container.setVisibility(View.VISIBLE);
        }
    }

    //开启Webrtc视频通道
    private synchronized int toggleStartVideoCall(Bundle bundle) {
        //停止本地图像采集
        Log.d("webrtc","toggleStartVideoCall");
        WebRTCProxy.getInstance().stopLocalCapture();
        if(WebRTCProxy.getInstance().startVideoCallAction(bundle).equals("StateOK")){
            Log.d("webrtc","startVideoCallAction StateOK");
            return 0;
        }
        else{
            Log.d("webrtc","startVideoCallAction StateFail");
            return -1;
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
    public synchronized void toggleStopCall() {
        WebRTCProxy.getInstance().stopVideoCallAction();
    }

    //WebRTC资源析构
    public synchronized void onWebRTCDepose() {
        WebRTCProxy.getInstance().onWebRTCDepose();
    }

    //异常退出处理
    private void illegalQuitProcess(){
        if(callingDuration!=CallingDuration.IDLE){
            if(callingDuration==CallingDuration.INCOMING){
                //拒绝接听
                refuseVideoRequest(remoteUserID, groupType, sessionID);
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
                cancelVideoRequest(remoteUserID, groupType, sessionID);
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
                closeVideoRequest(remoteUserID, groupType, sessionID);
                //切换呼叫状态
                callingDuration = CallingDuration.IDLE;
                //关闭音视频通道
                toggleStopCall();
                //停止计时
                chronometer.stop();
            }
        }
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
        PubHolder.getInstance().addUserHandler(HandlerType.VIDEOCALLACTIVITY_USERHANDLER.ordinal()
                ,userInfoHandler);
        userRequest = PubHolder.getInstance().getUserRequest();

        videoCallHandler = new Handler(){
            public void handleMessage(final Message msg){
                MessageType type = MessageType.values()[msg.what];
                switch (type){
                    case RECVACCEPTVIDEO_MSG:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onRecvAcceptVideo(msg);
                            }
                        });
                        break;
                    case RECVREFUSEVIDEO_MSG:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onRecvRefuseVideo(msg);
                            }
                        });
                        break;
                    case OPENVIDEODEVICE_MSG:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onOpenVideoDevice(msg);
                            }
                        });
                        break;
                    case RECVCANCELVIDEO_MSG:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onRecvCancelVideo(msg);
                            }
                        });
                        break;
                    case RECVCLOSEVIDEO_MSG:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onRecvCloseVideo(msg);
                            }
                        });
                        break;
                    default:break;
                }
            }
        };
        PubHolder.getInstance().addVideoHandler(HandlerType.VIDEOCALLACTIVITY_VIDEOHANDLER.ordinal()
                ,videoCallHandler);
        videoRequest = PubHolder.getInstance().getVideoreq();

        audioCallHandler = new Handler(){
            public void handleMessage(final Message msg) {
                MessageType type = MessageType.values()[msg.what];
                switch (type){
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
        PubHolder.getInstance().addAudioHandler(HandlerType.VIDEOCALLACTIVITY_AUDIOHANDLER.ordinal()
                ,audioCallHandler);
        audioRequest = PubHolder.getInstance().getAudioreq();
    }

    private void addClickListener(){
        //点击图像显示主布局控制呼叫控制按键是否可见
        primarySurace.setOnClickListener(this);
        //点击图像显示次布局通话过程中切换本地\远端显示位置
        secondarySurface.setOnClickListener(this);
        //呼叫OUTGOING过程中，取消呼叫
        cancelVideoCall_btn.setOnClickListener(this);
        //呼叫INCOMING过程中，拒绝接听
        refuseVideoCall_btn.setOnClickListener(this);
        //呼叫INCOMING过程中，接听
        answerVideoCall_btn.setOnClickListener(this);
        //正在通话INCALLING过程中，挂断
        dropVideoCall_btn.setOnClickListener(this);
        //正在通话INCALLING过程中，转换摄像头
        switchCamera_btn.setOnClickListener(this);
        //正在通话INCALLING过程中，切到语音聊天
        switchToAudio_btn.setOnClickListener(this);
    }
    //私有方法e----------

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
                    user_nickname.setText(myMsg.get(PubStruct.UserInfo.Name));
                    remoteName = myMsg.get(PubStruct.UserInfo.Name);
                }
                else{
                    user_nickname.setText(getResources().getString(R.string.remoteUserNickName_default));
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
                            user_avartar.setImageBitmap(bitmap);
                        }
                        else{
                            user_avartar.setImageResource(R.drawable.head);
                        }
                    }
                    else{
                        //需要向服务器下载头像
                        downloadImageRequest(myMsg.get(PubStruct.UserInfo.UserID),
                                myMsg.get(PubStruct.UserInfo.Avatar));
                    }
                }
                else{
                    user_avartar.setImageResource(R.drawable.head);
                }
            }
        }
        else{
            user_nickname.setText(getResources().getString(R.string.remoteUserNickName_default));
            remoteName = getResources().getString(R.string.remoteUserNickName_default);
            user_avartar.setImageResource(R.drawable.head);
            Toast.makeText(VideoCallActivity.this,getResources().getString(R.string.videocall_getuserinfo_fail),Toast.LENGTH_SHORT).show();
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
                        user_avartar.setImageBitmap(bitmap);
                    }
                    else{
                        user_avartar.setImageResource(R.drawable.head);
                    }
                }
            } else {
                Log.d("data","download image error");
                user_avartar.setImageResource(R.drawable.head);
                Toast.makeText(VideoCallActivity.this, getResources().getString(R.string.videocall_downloadavatar_fail)
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onRecvAcceptVideo(Message msg){
        if(callingDuration==CallingDuration.OUTGOING){
            IMessage myMsg = (IMessage)msg.obj;
            if((myMsg.checkKey("ToUser")==true)&&(myMsg.getString("ToUser").equals(localUserID))){
                if((outgoingTimeoutHandler!=null)&&(timeoutRunnableRef!=null)) {
                    outgoingTimeoutHandler.removeCallbacks(timeoutRunnableRef);
                }

                //停止振铃/回铃音效
                try {
                    if (soundPool != null)
                        soundPool.stop(streamID);
                } catch (Exception e) {
                }

                if (ringtone != null)
                    ringtone.stop();

                //关闭扩音器
                closeSpeakerOn();

                //显示正在连接
                callingDuration = CallingDuration.ESTABLISHING;
                videocall_state.setText(getResources().getString(R.string.CallEstablishing));
                changeContainerState(callingDuration);

                //发送开启音视频通道请求
                //openRemoteVideoDeviceRequest(remoteUserID,groupType,sessionID);
                //openRemoteAudioDeviceRequest(remoteUserID,groupType,audioID);
            }
        }
    }

    private void onRecvRefuseVideo(Message msg){
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
            saveCallRecord(CallingMode.VIDEO.ordinal());
            Toast.makeText(VideoCallActivity.this,getResources().getString(R.string.BeRefused),Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void onRecvCancelVideo(Message msg){
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
            saveCallRecord(CallingMode.VIDEO.ordinal());
            Toast.makeText(VideoCallActivity.this,getResources().getString(R.string.Canceled),Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void onRecvCloseVideo(Message msg){
        //仅在通话过程中响应
        if(callingDuration==CallingDuration.INCALLING){
            //切换呼叫状态
            callingDuration = CallingDuration.IDLE;
            //关闭音视频通道
            toggleStopCall();
            //停止计时
            chronometer.stop();
            callDruationText = chronometer.getText()
                    .toString();
            callingState = CallingState.NORMAL;
            saveCallRecord(CallingMode.VIDEO.ordinal());
            Toast.makeText(VideoCallActivity.this,getResources().getString(R.string.drop_videocall_toast),Toast.LENGTH_SHORT).show();
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
                //发起音频呼叫
                toggleStartAudioCall(bundle);

                //发送开启音频通道结果请求
                //openRemoteAudioDeviceResultRequest(remoteUserID,groupType,sessionID);
                Toast.makeText(VideoCallActivity.this, getResources().getString(R.string.audioCallToast), Toast.LENGTH_SHORT).show();
            } else {
                Log.d("data", "onOpenAudioDevice lack of SSRC param");
            }
        }
    }

    private void onOpenVideoDevice(Message msg){
        Log.d("data","onOpenVideoDevice:"+callingDuration);
        if(callingDuration == CallingDuration.ESTABLISHING){
            //保证video先、audio后的严格时序，在OpenVideoDevice回调跟新呼叫状态
            //读取呼叫参数
            MessageType type = MessageType.values()[msg.what];
            IMessage myMsg = (IMessage)msg.obj;
            if(myMsg.checkKey("SSRC")==true) {
                String tmpSSRC_V = myMsg.getString("SSRC");

                Bundle bundle = new Bundle();
                bundle.putString("remoteIP", getResources().getString(R.string.mpclient_transIP_default));
                bundle.putInt("localRecvPort_V", getResources().getInteger(R.integer.webrtc_video_recv_port_default));
                bundle.putInt("remoteSendPort_V", getResources().getInteger(R.integer.client_video_exchange_port_default));
                bundle.putInt("ssrc_V", ssrc2uint(tmpSSRC_V));

                Log.d("data","ssrc_V:"+ssrc2uint(tmpSSRC_V));
                //发起视频呼叫
                toggleStartVideoCall(bundle);

                //转移呼叫状态
                callingDuration = CallingDuration.INCALLING;
                changeContainerState(callingDuration);

                //开始计时
                chronometer.setBase(SystemClock
                        .elapsedRealtime());
                chronometer.start();

                //发送开启视频通道结果请求
                //openRemoteVideoDeviceResultRequest(remoteUserID,groupType,sessionID);
            }
            else{
                Log.d("data","onOpenAudioDevice lack of SSRC param");
                toggleStopCall();
                finish();
                return;
            }
        }
    }
    //底层消息响应回调e------

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

    private void inviteVideoCallRequest(String ToUser, String GroupType, String SessionID, String AudioID){
        IMessage msg = msgProvider.createMessage();
        msg.setString("ToUser",ToUser);
        msg.setString("GroupType",GroupType);
        msg.setString("SessionID",SessionID);
        msg.setString("AudioID",AudioID);
        videoRequest.InviteVideoRequest(msg);
    }

    private void closeVideoRequest(String ToUser, String GroupType, String SessionID){
        IMessage msg = msgProvider.createMessage();
        msg.setString("ToUser",ToUser);
        msg.setString("GroupType",GroupType);
        msg.setString("SessionID",SessionID);
        videoRequest.CloseVideoRequest(msg);
    }

    private void acceptVideoRequest(String ToUser, String GroupType, String SessionID){
        IMessage msg = msgProvider.createMessage();
        msg.setString("ToUser",ToUser);
        msg.setString("GroupType",GroupType);
        msg.setString("SessionID",SessionID);
        Log.d("data","ToUser"+ToUser+" SessionID"+SessionID);
        videoRequest.AcceptVideoRequest(msg);
    }

    private void refuseVideoRequest(String ToUser, String GroupType, String SessionID){
        IMessage msg = msgProvider.createMessage();
        msg.setString("ToUser",ToUser);
        msg.setString("GroupType",GroupType);
        msg.setString("SessionID",SessionID);
        videoRequest.RefuseVideoRequest(msg);
    }

    private void cancelVideoRequest(String ToUser, String GroupType, String SessionID){
        IMessage msg = msgProvider.createMessage();
        msg.setString("ToUser",ToUser);
        msg.setString("GroupType",GroupType);
        msg.setString("SessionID",SessionID);
        videoRequest.CancelInviteVideoRequest(msg);
    }

    private int openRemoteVideoDeviceRequest(String ToUser, String GroupType, String SessionID){
        IMessage msg = msgProvider.createMessage();
        msg.setString("ToUser",ToUser);
        msg.setString("GroupType",GroupType);
        msg.setString("SessionID",SessionID);
        return videoRequest.OpenRemoteVideoDeviceRequest(msg);
    }

    private int openRemoteAudioDeviceRequest(String ToUser, String GroupType, String AudioID){
        IMessage msg = msgProvider.createMessage();
        msg.setString("ToUser",ToUser);
        msg.setString("GroupType",GroupType);
        msg.setString("SessionID",AudioID);
        return audioRequest.OpenRemoteAudioDeviceRequest(msg);
    }

    private void openRemoteVideoDeviceResultRequest(String ToUser, String GroupType, String SessionID){
        IMessage msg = msgProvider.createMessage();
        msg.setString("ToUser",ToUser);
        msg.setString("GroupType",GroupType);
        msg.setString("SessionID",SessionID);
        msg.setString("OpenResult","Accept");
        videoRequest.OpenRemoteVideoDeviceResultRequest(msg);
    }

    private void openRemoteAudioDeviceResultRequest(String ToUser, String GroupType, String AudioID){
        IMessage msg = msgProvider.createMessage();
        msg.setString("ToUser",ToUser);
        msg.setString("GroupType",GroupType);
        msg.setString("SessionID",AudioID);
        msg.setString("OpenResult","Accept");
        audioRequest.OpenRemoteAudioDeviceResultRequest(msg);
    }
    //服务器请求e-------

    //点击事件响应b-------
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.videocall_primary_surface:
                primarySurfaceClickAction();
                break;
            case R.id.videocall_secondray_surface:
                secondarySurfaceClickAction();
                break;
            case R.id.btn_cancel_call:
                cancelCallClickAction();
                break;
            case R.id.btn_refuse_call:
                refuseCallClickAction();
                break;
            case R.id.btn_answer_call:
                answerCallClickAction();
                break;
            case R.id.btn_drop_call:
                dropCallClickAction();
                break;
            case R.id.btn_switch_camera:
                switchCameraClickAction();
                break;
            case R.id.btn_switchto_audio:
                switchToAudioClickAction();
                break;
        }
    }
    //点击事件响应e-------

    //点击事件实现b-------
    private void primarySurfaceClickAction(){
        //仅在通话过程中响应
        if(callingDuration==CallingDuration.INCALLING){
            isCallingContainerShow = !isCallingContainerShow;
            if(isCallingContainerShow==true) {
                ll_surface_baseline.bringToFront();
                ll_surface_baseline.setVisibility(View.VISIBLE);
            }
            else{
                ll_surface_baseline.setVisibility(View.GONE);
            }
        }
    }

    //暂时屏蔽
    private synchronized void secondarySurfaceClickAction(){
        //仅在通话过程中响应
        if(callingDuration==CallingDuration.INCALLING){
            bSwitchView = !bSwitchView;
            WebRTCProxy.getInstance().switchRenderView();
        }
    }

    private void cancelCallClickAction(){
        //仅在OUTGOING过程中响应
        if(callingDuration==CallingDuration.OUTGOING){

            if((outgoingTimeoutHandler!=null)&&(timeoutRunnableRef!=null)) {
                outgoingTimeoutHandler.removeCallbacks(timeoutRunnableRef);
            }

            cancelVideoRequest(remoteUserID, groupType, sessionID);

            //停止拨打电话音效
            try {
                if (soundPool != null)
                    soundPool.stop(streamID);
            } catch (Exception e) {
            }

            Toast.makeText(VideoCallActivity.this,getResources().getString(R.string.cancel_invitevideocall_toast),
                    Toast.LENGTH_SHORT).show();

            //保存通话记录
            callingState = CallingState.CANCED;
            saveCallRecord(CallingMode.VIDEO.ordinal());
            //关闭VideoCall活动
            finish();
        }
    }

    private void refuseCallClickAction(){
        //仅在INCOMING过程中响应
        if(callingDuration==CallingDuration.INCOMING){
            //发送拒绝邀请
            refuseVideoRequest(remoteUserID, groupType, sessionID);
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
            Toast.makeText(VideoCallActivity.this,getResources().getString(R.string.Refused),Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void answerCallClickAction(){
        //仅在INCOMING过程中响应
        if(callingDuration==CallingDuration.INCOMING){
            //停止振铃音效
            if (ringtone != null)
                ringtone.stop();
            //关闭扩音器
            closeSpeakerOn();
            //显示正在连接
            callingDuration = CallingDuration.ESTABLISHING;
            videocall_state.setText(getResources().getString(R.string.CallEstablishing));
            changeContainerState(callingDuration);
            //发送接受视频聊天请求
            acceptVideoRequest(remoteUserID, groupType, sessionID);
            //发送开启音视频通道请求
            //openRemoteVideoDeviceRequest(remoteUserID,groupType,sessionID);
            //openRemoteAudioDeviceRequest(remoteUserID,groupType,audioID);
        }
    }

    private void dropCallClickAction(){
        //仅在通话过程中响应
        Log.d("mysurface1","dropCallClickAction begin");

        if(callingDuration==CallingDuration.INCALLING){
            //发送结束视频聊天请求
            closeVideoRequest(remoteUserID, groupType, sessionID);
            //切换呼叫状态
            callingDuration = CallingDuration.IDLE;
            //关闭音视频通道
            toggleStopCall();
            //停止计时
            chronometer.stop();
            callDruationText = chronometer.getText()
                    .toString();
            callingState = CallingState.NORMAL;
            saveCallRecord(CallingMode.VIDEO.ordinal());
            Toast.makeText(VideoCallActivity.this,getResources().getString(R.string.drop_videocall_toast),Toast.LENGTH_SHORT).show();
            finish();
        }

        Log.d("mysurface1","dropCallClickAction end");
    }

    private synchronized void switchCameraClickAction(){
        //仅在通话过程中响应
        if(callingDuration==CallingDuration.INCALLING) {
            WebRTCProxy.getInstance().switchCameraFacing();
        }
    }

    private synchronized void switchToAudioClickAction(){
        //仅在通话过程中响应
        if(callingDuration==CallingDuration.INCALLING) {
            WebRTCProxy.getInstance().switchToAudioCall();
        }
    }
    //点击事件实现e-------

    @Override
    protected void onDestroy() {
        Log.d("mysurface1","VideoCallActivity onDestroy");
        //处理异常退出
        illegalQuitProcess();

        Log.d("mysurface1","removeView begin");
        if(bSwitchView==false) {
            if(WebRTCProxy.getInstance().getState_context().getEngine().getRemoteSurfaceView()!=null)
                primarySurace.removeView(WebRTCProxy.getInstance().getState_context().getEngine().getRemoteSurfaceView());
            if(WebRTCProxy.getInstance().getState_context().getEngine().getLocalSurfaceView()!=null)
                secondarySurface.removeView(WebRTCProxy.getInstance().getState_context().getEngine().getLocalSurfaceView());
        }
        else{
            if(WebRTCProxy.getInstance().getState_context().getEngine().getRemoteSurfaceView()!=null)
                secondarySurface.removeView(WebRTCProxy.getInstance().getState_context().getEngine().getRemoteSurfaceView());
            if(WebRTCProxy.getInstance().getState_context().getEngine().getLocalSurfaceView()!=null)
                primarySurace.removeView(WebRTCProxy.getInstance().getState_context().getEngine().getLocalSurfaceView());
        }
        Log.d("mysurface1","removeView end");
        //WebRTC资源析构
        onWebRTCDepose();
        //移除Handler
        PubHolder.getInstance().removeUserHandler(HandlerType.VIDEOCALLACTIVITY_USERHANDLER.ordinal());
        PubHolder.getInstance().removeVideoHandler(HandlerType.VIDEOCALLACTIVITY_VIDEOHANDLER.ordinal());
        PubHolder.getInstance().removeAudioHandler(HandlerType.VIDEOCALLACTIVITY_AUDIOHANDLER.ordinal());
        //移除活动
        ((ApplicationSession)getApplication()).removeActivity(this);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Log.d("mysurface","VideoCallActivity onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d("mysurface","VideoCallActivity onStop");
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        Log.d("mysurface","VideoCallActivity onBackPressed");
        super.onBackPressed();
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

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("webrtc","VideoCallActivity onStart()");
    }
}
