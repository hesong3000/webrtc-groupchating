package android.webrtc.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webrtc.avgroupchatproxy.UserInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class GroupChatSettingActivity extends AppCompatActivity {
    private final String TAG = "GroupChatSetting";
    private EditText avRemoteIP;
    private EditText audioRecvPort;
    private EditText videoRecvPort;
    private Button AVGroupChatActivity;
    private Button startLocalCapture;
    private Button invitingUIDemo;
    private EditText audioSendSSRC;
    private EditText videoSendSSRC;
    private EditText channel1_audioRecvPort;
    private EditText channel1_videoRecvPort;
    private EditText channel2_audioRecvPort;
    private EditText channel2_videoRecvPort;
    private Button gifOverLayout;

    private AudioManager audoManager;
    private HomeWatcherReceiver mHomeKeyReceiver = null;

    public static void myStartActivity(Context context, UserInfo userInfo, Intent myintent){
        Intent intent = new Intent(context,GroupChatSettingActivity.class);
        intent.putExtra("intent1","test");
        intent.putExtras(myintent);
        intent.putExtra("intent2","test");
        context.startActivity(intent);
    }

    private void registerHomeKeyReceiver(Context context) {
        Log.i(TAG, "registerHomeKeyReceiver");
        mHomeKeyReceiver = new HomeWatcherReceiver();
        final IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

        context.registerReceiver(mHomeKeyReceiver, homeFilter);
    }

    private void unregisterHomeKeyReceiver(Context context) {
        Log.i(TAG, "unregisterHomeKeyReceiver");
        if (null != mHomeKeyReceiver) {
            context.unregisterReceiver(mHomeKeyReceiver);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_setting);

        audoManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        boolean isHeadPlugIn = audoManager.isWiredHeadsetOn();
        Log.d(TAG,"isHeadPlugIn:"+isHeadPlugIn);

        Bundle bundle = this.getIntent().getExtras();
        for(String key:bundle.keySet()){
            Log.d(TAG,"key:"+key);
        }

        UserInfo userInfo = (UserInfo)this.getIntent().getSerializableExtra("myUserInfo");
        Log.d(TAG,"userName:"+userInfo.getUserName());

        Log.d(TAG,"intent2:"+this.getIntent().getStringExtra("intent2"));

        initUI();
        initParams();
        initClickListen();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG,"onKeyDown keyCode:"+keyCode);
        if((KeyEvent.KEYCODE_BACK==keyCode)){
            Log.d(TAG,"onKeyDown keyCode:"+keyCode);
            AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatSettingActivity.this);
            builder.setIcon(R.drawable.ic_launcher);
            builder.setTitle("你确定要退出会议吗？");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // 这里添加点击确定后的逻辑
                    Toast.makeText(GroupChatSettingActivity.this,"点击了确定",Toast.LENGTH_SHORT).show();
                    GroupChatSettingActivity.this.finish();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // 这里添加点击取消后的逻辑
                    Toast.makeText(GroupChatSettingActivity.this,"点击了取消",Toast.LENGTH_SHORT).show();
                }
            });
            builder.create().show();
        }
        else if((KeyEvent.KEYCODE_HOME==keyCode)||(KeyEvent.KEYCODE_ASSIST==keyCode)){
            Toast.makeText(GroupChatSettingActivity.this,"禁用了Home键",Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        //unregisterHomeKeyReceiver(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        //registerHomeKeyReceiver(this);
        super.onResume();
    }

    private void initUI(){
        avRemoteIP = (EditText)findViewById(R.id.avremoteIP);
        audioRecvPort = (EditText)findViewById(R.id.audioRecvPort);
        videoRecvPort = (EditText)findViewById(R.id.videoRecvPort);
        AVGroupChatActivity = (Button)findViewById(R.id.AVGroupChatActivity);
        startLocalCapture = (Button)findViewById(R.id.startLocalCapture);
        invitingUIDemo = (Button)findViewById(R.id.invitingUIDemo);
        audioSendSSRC = (EditText)findViewById(R.id.audioSendSSRC);
        videoSendSSRC = (EditText)findViewById(R.id.videoSendSSRC);
        channel1_audioRecvPort = (EditText)findViewById(R.id.channel1_audioRecvPort);
        channel1_videoRecvPort = (EditText)findViewById(R.id.channel1_videoRecvPort);
        channel2_audioRecvPort = (EditText)findViewById(R.id.channel2_audioRecvPort);
        channel2_videoRecvPort = (EditText)findViewById(R.id.channel2_videoRecvPort);

        gifOverLayout = (Button)findViewById(R.id.gifOverLayout);
    }

    private void initParams(){
        avRemoteIP.setText("192.168.0.142");
        audioRecvPort.setText("6006");
        videoRecvPort.setText("6004");
        audioSendSSRC.setText("1039FB19");
        videoSendSSRC.setText("7A4E08FE");
        channel1_audioRecvPort.setText("20000");
        channel1_videoRecvPort.setText("30000");
        channel2_audioRecvPort.setText("20002");
        channel2_videoRecvPort.setText("30002");
    }

    private void initClickListen(){
        AVGroupChatActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String remoteIP = avRemoteIP.getText().toString();
                String remoteAudioPort = audioRecvPort.getText().toString();
                String remoteVideoPort = videoRecvPort.getText().toString();
                String ASSRC_sendchan = audioSendSSRC.getText().toString();
                String VSSRC_sendchan = videoSendSSRC.getText().toString();
                String ARecvPort_chan1 = channel1_audioRecvPort.getText().toString();
                String VRecvPort_chan1 = channel1_videoRecvPort.getText().toString();
                String ARecvPort_chan2 = channel2_audioRecvPort.getText().toString();
                String VRecvPort_chan2 = channel2_videoRecvPort.getText().toString();
                Toast.makeText(GroupChatSettingActivity.this,"ASSRC:"+ASSRC_sendchan+" VSSRC:"+VSSRC_sendchan,
                        Toast.LENGTH_SHORT).show();

                int remotePort_A_i = Integer.parseInt(remoteAudioPort,10);
                int remotePort_V_i = Integer.parseInt(remoteVideoPort,10);
                int ASSRC_sendchan_i = ssrc_str2int(ASSRC_sendchan);
                int VSSRC_sendchan_i = ssrc_str2int(VSSRC_sendchan);
                int ARecvPort_chan1_i = Integer.parseInt(ARecvPort_chan1,10);
                int VRecvPort_chan1_i = Integer.parseInt(VRecvPort_chan1,10);
                int ARecvPort_chan2_i = Integer.parseInt(ARecvPort_chan2,10);
                int VRecvPort_chan2_i = Integer.parseInt(VRecvPort_chan2,10);

                GroupVideoActivity.StartActivity(GroupChatSettingActivity.this,remoteIP,remotePort_A_i,remotePort_V_i,
                        ASSRC_sendchan_i, VSSRC_sendchan_i, ARecvPort_chan1_i, VRecvPort_chan1_i,
                        ARecvPort_chan2_i,VRecvPort_chan2_i);
            }
        });

        startLocalCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupChatSettingActivity.this,LocalCaptureActivity.class);
                startActivity(intent);
            }
        });

        invitingUIDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupChatSettingActivity.this,InviteUIDemo.class);
                startActivity(intent);
            }
        });

        gifOverLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupChatSettingActivity.this,GifOverLayout.class);
                startActivity(intent);
            }
        });
    }

    private int ssrc_str2int(String ssrc){
        char[] chars = ssrc.toUpperCase().toCharArray();
        int ret = 0;
        char step = ('A' - '9') - 1;
        for(int j = 0; j < chars.length; j++){
            ret <<= 4;
            ret += chars[j] > '9' ? chars[j]-'0' - step : chars[j] - '0';
        }

        return ret;
    }

    class HomeWatcherReceiver extends BroadcastReceiver {

        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";
        private static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                Log.d(TAG,"onReceive ACTION_CLOSE_SYSTEM_DIALOGS");
                Log.d(TAG,"reason:"+reason);
                if ((SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason))
                        ||(SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason))
                        ||(SYSTEM_DIALOG_REASON_LOCK.equals(reason))
                        ||(SYSTEM_DIALOG_REASON_ASSIST.equals(reason))) {
                    Log.d(TAG,"new AlertDialog.Builder");
                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatSettingActivity.this);
                    builder.setIcon(R.drawable.ic_launcher);
                    builder.setTitle("你确定要退出会议吗？");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // 这里添加点击确定后的逻辑
                            Toast.makeText(GroupChatSettingActivity.this,"点击了确定",Toast.LENGTH_SHORT).show();
                            GroupChatSettingActivity.this.finish();
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // 这里添加点击取消后的逻辑
                            Toast.makeText(GroupChatSettingActivity.this,"点击了取消",Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.create().show();
                }
            }
        }
    }
}
