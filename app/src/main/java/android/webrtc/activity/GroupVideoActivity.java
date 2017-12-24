package android.webrtc.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webrtc.avgroupchatproxy.AVGC;
import android.webrtc.avgroupchatproxy.RecvChannelHolder;
import android.webrtc.avgroupchatproxy.Resolution;
import android.webrtc.avgroupchatproxy.SendChannelHolder;
import android.webrtc.avgroupchatproxy.UserInfo;
import android.webrtc.avgroupchatproxy.UserState;
import android.webrtc.core.AVGroupEngine;
import android.webrtc.core.NativeWebRtcContextRegistry;
import android.webrtc.utils.DevicePermission;
import android.webrtc.utils.DisplayUtil;
import android.webrtc.utils.ImageResUtil;
import android.webrtc.widget.NineGridImageView;
import android.webrtc.widget.NineGridImageViewAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class GroupVideoActivity extends AppCompatActivity {
    private String TAG = GroupVideoActivity.class.getSimpleName();
    //private String TAG = "GroupVideoActivity";
    private NineGridImageView gridView;
    private ImageView avgroupchat_addPerson;
    private ImageView avgroupchat_delPerson01;    //测试使用
    private ImageView avgroupchat_delPerson02;    //测试使用
    private String user01 = "user01";             //测试使用
    private int user01_recvPort_A = 2000;         //测试使用
    private int user01_recvPort_V = 2002;         //测试使用
    private String user02 = "user02";             //测试使用
    private int user02_recvPort_A = 3000;         //测试使用
    private int user02_recvPort_V = 3002;         //测试使用
    private ImageView avgroupchat_handfree;
    private ImageView avgroupchat_opencamera;
    private ImageView avgroupchat_micmute;
    private ImageView avgroupchat_dropcall;
    private NineGridImageViewAdapter<String> adapter;
    private float displayRatio;
    private RelativeLayout grid_baselayout;
    private boolean isCallingContainerShow = false;
    private LinearLayout controlArea;
    private Bundle bundleParams = new Bundle();

    private RelativeLayout avgroupchat_inchatting_ui;   //inchating状态下的UI布局

    //AVGroupMediaEngine
    private NativeWebRtcContextRegistry contextRegistry = null;
    private AVGroupEngine avGroupEngine = null;

    //LocalChannel
    private SendChannelHolder localChannellEntity = null;
    //RemoteChannels
    private Map<String, RecvChannelHolder> remoteChannelEntities = new HashMap<String, RecvChannelHolder>();

    private ImageView myImage01 = null;

    public static void StartActivity(Context context, String remoteIP, int remotePort_A_i, int remotePort_V_i,
                                     int SSRC_A, int SSRC_V, int APort_chan1, int VPort_chan1,
                                     int APort_chan2, int VPort_chan2){
        if((remoteIP==null)||(remoteIP.length()==0)||(remotePort_A_i<=0)||(remotePort_V_i<=0)
                ||(APort_chan1<=0)||(VPort_chan1<=0)
                ||(APort_chan2<=0)||(VPort_chan2<=0)){
            Log.d("GroupVideoActivity","StartActivity params invalid");
            return;
        }

        Intent intent = new Intent(context,GroupVideoActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(AVGC.RemoteIP,remoteIP);
        bundle.putInt(AVGC.RemoteRecvPort_A,remotePort_A_i);
        bundle.putInt(AVGC.RemoteRecvPort_V,remotePort_V_i);
        bundle.putInt(AVGC.SSRC_A,SSRC_A);
        bundle.putInt(AVGC.SSRC_V,SSRC_V);
        bundle.putInt(AVGC.APort_chan1,APort_chan1);
        bundle.putInt(AVGC.VPort_chan1,VPort_chan1);
        bundle.putInt(AVGC.APort_chan2,APort_chan2);
        bundle.putInt(AVGC.VPort_chan2,VPort_chan2);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_video);
        getSupportActionBar().hide();
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        Intent intent = this.getIntent();
        bundleParams = intent.getExtras();
        displayRatio = (float)4.0/(float)3.0;
        //防止闪屏
        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        if(isAVDeviceCanUse()==false){
            finish();
            return;
        }

        initAVGroupMediaEngine();
        findViews();
        initGridView();
        setOnListener();
        controlAreaShowProc(false);

        initLocalChannel(bundleParams.getString(AVGC.RemoteIP), bundleParams.getInt(AVGC.RemoteRecvPort_A)
                , bundleParams.getInt(AVGC.RemoteRecvPort_V));
    }

    private boolean isAVDeviceCanUse(){
        if((DevicePermission.isVoicePermission()==false)
                ||(DevicePermission.isCameraPermission(GroupVideoActivity.this)==false)
                ||(DevicePermission.isCameraCanUse()==false)){
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        forceToClearResource();
        avGroupEngine.dispose();
        contextRegistry.unRegister();
        contextRegistry = null;
        avGroupEngine = null;
        setContentView(R.layout.view_null);
        super.onDestroy();
    }

    private void forceToClearResource(){
        if(remoteChannelEntities!=null){
            if(!remoteChannelEntities.isEmpty()){
                for(Map.Entry<String,RecvChannelHolder> map : remoteChannelEntities.entrySet()){
                    map.getValue().dispose();
                }
                remoteChannelEntities.clear();
            }
        }

        if(localChannellEntity!=null){
            localChannellEntity.dispose();
            localChannellEntity = null;
        }
    }

    private void findViews(){
        gridView = (NineGridImageView)findViewById(R.id.gv_gridview);
        avgroupchat_addPerson = (ImageView)findViewById(R.id.add_person);
        avgroupchat_delPerson01 = (ImageView)findViewById(R.id.del_person01);
        avgroupchat_delPerson02 = (ImageView)findViewById(R.id.del_person02);
        avgroupchat_handfree = (ImageView)findViewById(R.id.avgroupchat_handfree);
        avgroupchat_opencamera = (ImageView)findViewById(R.id.avgroupchat_opencamera);
        avgroupchat_dropcall = (ImageView)findViewById(R.id.avgroupchat_dropcall);
        avgroupchat_micmute = (ImageView)findViewById(R.id.avgroupchat_micmute);
        grid_baselayout = (RelativeLayout)findViewById(R.id.grid_baselayout);
        controlArea = (LinearLayout)findViewById(R.id.controlArea);
        avgroupchat_inchatting_ui = (RelativeLayout)findViewById(R.id.avgroupchat_inchatting_ui);
    }

    private void controlAreaShowProc(boolean show){
        if(show==true){
            controlArea.bringToFront();
            controlArea.setVisibility(View.VISIBLE);
        }
        else{
            controlArea.setVisibility(View.GONE);
        }
    }

    private void initGridView(){
        //获得屏幕长宽比
        displayRatio = DisplayUtil.getScreenRate(this);
        gridView.setDisplayRatio(displayRatio);
        adapter = createNineGridImageViewAdapter();
        gridView.setAdapter(adapter);
    }

    private NineGridImageViewAdapter<String> createNineGridImageViewAdapter(){
        return new NineGridImageViewAdapter<String>() {
            @Override
            protected RelativeLayout generateView(Context context) {
                return super.generateView(context);
            }
        };
    }

    private void setOnListener(){
        avgroupchat_addPerson.setOnClickListener(new AVGroupClickListener());
        avgroupchat_handfree.setOnClickListener(new AVGroupClickListener());
        avgroupchat_opencamera.setOnClickListener(new AVGroupClickListener());
        avgroupchat_dropcall.setOnClickListener(new AVGroupClickListener());
        avgroupchat_delPerson01.setOnClickListener(new AVGroupClickListener());
        avgroupchat_delPerson02.setOnClickListener(new AVGroupClickListener());
        grid_baselayout.setOnClickListener(new AVGroupClickListener());
        avgroupchat_micmute.setOnClickListener(new AVGroupClickListener());
    }

    private void initAVGroupMediaEngine(){
        contextRegistry = new NativeWebRtcContextRegistry();
        contextRegistry.register(this);
        avGroupEngine = new AVGroupEngine(this);

    }

    private void initLocalChannel(String remoteIP, int remotePort_A, int remotePort_V){
        localChannellEntity = new SendChannelHolder();
        String userID = "invitor";
        int remoteSSRC_A = bundleParams.getInt(AVGC.SSRC_A);
        int remoteSSRC_V = bundleParams.getInt(AVGC.SSRC_V);
        int audioCodec = avGroupEngine.getIsacIndex();
        int videoCodec = getResources().getInteger(R.integer.video_codec_default);
        int resolutionIndex = Resolution.CODEC_QCIF;

        //创建UserInfo
        UserInfo userInfo = new UserInfo();
        userInfo.setUserID(userID);
        userInfo.setMediaIP(remoteIP);
        userInfo.setAudioPort(remotePort_A);
        userInfo.setVideoPort(remotePort_V);
        userInfo.setUserSSRC_A(remoteSSRC_A);
        userInfo.setUserSSRC_V(remoteSSRC_V);
        userInfo.setUserState(UserState.chatingStat);
        localChannellEntity.setUserInfo(userInfo);

        Toast.makeText(GroupVideoActivity.this,"remoteSSRC_A:"+remoteSSRC_A+" remoteSSRC_V:"
                +remoteSSRC_V,Toast.LENGTH_LONG).show();

        //增加gridView
        int position = addGridView();
        gridView.notifyDataSetChanged();
        RelativeLayout layout = gridView.getView(position);
        Log.d(TAG,"local channel getView position:"+position+" width:"+layout.getWidth()+" height:"+layout.getHeight());
        //初始化音频channel
        localChannellEntity.createVoiceChannel(avGroupEngine,this);
        localChannellEntity.setVoiceParams(audioCodec);

        //初始化视频channel
        localChannellEntity.createVideoChannel(avGroupEngine,this);
        localChannellEntity.setVideoParams(resolutionIndex, videoCodec);
        localChannellEntity.setParentLayout(layout);
        localChannellEntity.setVideoRenderer(this);

        Log.d(TAG,"sssssssss");
        /*
        localChannellEntity.getUserInfo().setUserState(UserState.invitingStat);
        Bitmap bitmap = ImageResUtil.getBitmapFromResources(this,R.drawable.test_avatar);
        localChannellEntity.turnIntoInvitingState(GroupVideoActivity.this,bitmap);
        */


        //开启音频发送

        if(localChannellEntity.getUserInfo()!=null) {
            UserInfo tmpUserInfo = localChannellEntity.getUserInfo();
            if(!tmpUserInfo.getUserState().contains(UserState.withAudioStat)) {
                tmpUserInfo.setUserState(tmpUserInfo.getUserState() + UserState.withAudioStat);
                localChannellEntity.startVoiceSend();
                Log.d(TAG,"user:"+tmpUserInfo.getUserID()+" curStat:"+tmpUserInfo.getUserState());
            }
            else{
                Log.d(TAG,"user:"+tmpUserInfo+" is already in audio state");
            }
        }
        else{
            Log.d(TAG,"could not find userInfo!!");
        }
    }

    private int addGridView(){
        if(gridView.getChildViewCount()>AVGC.MaxLayoutNum){
            Log.d(TAG,"avGroup has enough members!!");
            return -1;
        }
        int position = gridView.addLayout();
        if(position>=0){
            RelativeLayout layout = gridView.getView(position);
            if(layout!=null){
                Log.d(TAG,"gridView addLayout success, layout:"+layout.toString());
                //gridView更新
                gridView.notifyDataSetChanged();
                return position;
            }
            else{
                Log.d(TAG,"gridView addLayout failed, layout is non pointer!!");
                return -1;
            }
        }
        else{
            Log.d(TAG,"gridView addLayout failed!!");
            return -1;
        }
    }

    private void avGroupAddPersonClicked(){
        if(localChannellEntity==null) {
            initLocalChannel(bundleParams.getString(AVGC.RemoteIP), bundleParams.getInt(AVGC.RemoteRecvPort_A)
                    , bundleParams.getInt(AVGC.RemoteRecvPort_V));
        }
    }

    private void avGroupDelPerson01Clicked(){
        if(!remoteChannelEntities.containsKey(user01)) {
            //增加成员user01
            if (gridView.getChildViewCount() <= AVGC.MaxLayoutNum) {
                RecvChannelHolder recvChan = new RecvChannelHolder();
                //创建UserInfo
                UserInfo userInfo = new UserInfo();
                userInfo.setUserID(user01);
                userInfo.setMediaIP("");
                userInfo.setAudioPort(bundleParams.getInt(AVGC.APort_chan1));
                userInfo.setVideoPort(bundleParams.getInt(AVGC.VPort_chan1));
                userInfo.setUserSSRC_A(0);
                userInfo.setUserSSRC_V(0);
                recvChan.setUserInfo(userInfo);
                //增加gridView
                int position = addGridView();
                RelativeLayout layout = gridView.getView(position);
                Log.d(TAG,"getView position:"+position+" width:"+layout.getWidth()+" height:"+layout.getHeight());
                //初始化音频channel
                recvChan.createVoiceChannel(avGroupEngine,this);
                recvChan.setVoiceParams();

                //初始化视频channel
                recvChan.createVideoChannel(avGroupEngine,this);
                recvChan.setVideoParams();
                recvChan.setParentLayout(layout);
                recvChan.setVideoRenderer(this);
                remoteChannelEntities.put(user01,recvChan);

                //开启音频接收
                recvChan.startAudioRecv();
                //开启视频接收
                recvChan.startVideoRecv();
            }
            else{
                Log.d(TAG,"the members in avgroupchat is enough!!");
            }
        }
        else{
            //剔除成员user01
            RecvChannelHolder recvChan = remoteChannelEntities.get(user01);
            gridView.removeLayout(recvChan.getParentLayout());
            recvChan.dispose();
            remoteChannelEntities.remove(user01);
        }
    }

    private void avGroupDelPerson02Clicked(){
        if(!remoteChannelEntities.containsKey(user02)) {
            //增加成员user02
            if (gridView.getChildViewCount() <= AVGC.MaxLayoutNum) {
                RecvChannelHolder recvChan = new RecvChannelHolder();
                //创建UserInfo
                UserInfo userInfo = new UserInfo();
                userInfo.setUserID(user02);
                userInfo.setMediaIP("");
                userInfo.setAudioPort(bundleParams.getInt(AVGC.APort_chan2));
                userInfo.setVideoPort(bundleParams.getInt(AVGC.VPort_chan2));
                userInfo.setUserSSRC_A(0);
                userInfo.setUserSSRC_V(0);
                recvChan.setUserInfo(userInfo);

                //增加gridView
                int position = addGridView();
                RelativeLayout layout = gridView.getView(position);

                //初始化音频channel
                recvChan.createVoiceChannel(avGroupEngine,this);
                recvChan.setVoiceParams();

                //初始化视频channel
                recvChan.createVideoChannel(avGroupEngine,this);
                recvChan.setVideoParams();
                recvChan.setParentLayout(layout);
                recvChan.setVideoRenderer(this);
                remoteChannelEntities.put(user02,recvChan);

                //开启音频接收
                recvChan.startAudioRecv();
                //开启视频接收
                recvChan.startVideoRecv();
            }
            else{
                Log.d(TAG,"the members in avgroupchat is enough!!");
            }
        }
        else{
            //剔除成员user02
            RecvChannelHolder recvChan = remoteChannelEntities.get(user02);
            gridView.removeLayout(recvChan.getParentLayout());
            recvChan.dispose();
            remoteChannelEntities.remove(user02);
        }
    }

    private void avGroupHandFreeClicked(){
        //暂时用作转场测试 turnIntoInvitingState
        if(localChannellEntity!=null) {
            localChannellEntity.getUserInfo().setUserState(UserState.invitingStat);
            Bitmap bitmap = ImageResUtil.getBitmapFromResources(this,R.drawable.test_avatar);
            localChannellEntity.turnIntoInvitingState(GroupVideoActivity.this,bitmap);
        }
    }

    private void avGroupMicMuteClicked(){
        //暂时用作转场测试 turnIntoAudioChatState

        if(localChannellEntity!=null) {
            localChannellEntity.getUserInfo().setUserState(UserState.invitingStat);
            Bitmap bitmap = ImageResUtil.getBitmapFromResources(this,R.drawable.test_avatar02);
            localChannellEntity.turnIntoInvitingState(GroupVideoActivity.this,bitmap);
        }

        /*
        if(localChannellEntity!=null){
            localChannellEntity.getUserInfo().setUserState(UserState.chatingStat+UserState.withAudioStat);
            Bitmap bitmap = ImageResUtil.getBitmapFromResources(this,R.drawable.test_avatar);
            localChannellEntity.turnIntoAudioChatState(GroupVideoActivity.this,bitmap);
        }*/
    }

    private void avGroupCameraSwitchClicked(){
        if(localChannellEntity!=null){
            UserInfo tmpUserInfo = localChannellEntity.getUserInfo();
            if(tmpUserInfo!=null) {
                if (!tmpUserInfo.getUserState().contains(UserState.withVideoStat)) {
                    localChannellEntity.startVideoSend();
                    tmpUserInfo.setUserState(tmpUserInfo.getUserState()+UserState.withVideoStat);
                    avgroupchat_opencamera.setImageResource(R.drawable.avgroupchat_cameraopened);
                    Log.d(TAG,"user:"+tmpUserInfo.getUserID()+" curstat:"+tmpUserInfo.getUserState());
                } else {
                    localChannellEntity.stopVideoSend();
                    tmpUserInfo.setUserState(tmpUserInfo.getUserState().replace(UserState.withVideoStat,""));
                    avgroupchat_opencamera.setImageResource(R.drawable.avgroupchat_cameraclosed);
                    Log.d(TAG,"user:"+tmpUserInfo.getUserID()+" curstat:"+tmpUserInfo.getUserState());
                }
            }
            else{
                Log.d(TAG,"could not find userinfo!!");
            }
        }
    }

    private void avGroupDropCallClicked(){
        if(localChannellEntity!=null){
            gridView.removeLayout(localChannellEntity.getParentLayout());
            localChannellEntity.dispose();
            localChannellEntity = null;
            avgroupchat_opencamera.setImageResource(R.drawable.avgroupchat_cameraclosed);
        }
    }

    private void avGroupBaseGridLayoutClicked(){
        isCallingContainerShow = !isCallingContainerShow;
        controlAreaShowProc(isCallingContainerShow);
    }

    private class AVGroupClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.add_person:
                    avGroupAddPersonClicked();
                    break;
                case R.id.avgroupchat_handfree:
                    avGroupHandFreeClicked();
                    break;
                case R.id.avgroupchat_opencamera:
                    avGroupCameraSwitchClicked();
                    break;
                case R.id.avgroupchat_dropcall:
                    avGroupDropCallClicked();
                    break;
                case R.id.del_person01:
                    avGroupDelPerson01Clicked();
                    break;
                case R.id.del_person02:
                    avGroupDelPerson02Clicked();
                    break;
                case R.id.grid_baselayout:
                    avGroupBaseGridLayoutClicked();
                    break;
                case R.id.avgroupchat_micmute:
                    avGroupMicMuteClicked();
                    break;
                default:break;
            }
        }
    }
}
